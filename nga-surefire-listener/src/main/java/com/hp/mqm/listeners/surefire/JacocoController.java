package com.hp.mqm.listeners.surefire;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by vaingart on 14/12/2015.
 */
public class JacocoController {
	private static final String ERROR = "Unable to access JaCoCo Agent - make sure that you use JaCoCo and version not lower than 0.6.2.";
	private final IAgent agent;
	private boolean m_bTestStarted;

	private static JacocoController singleton;

	public static synchronized JacocoController getInstance() {
		if (singleton == null) {
			singleton = new JacocoController();
		}
		return singleton;
	}

	private JacocoController() {
		try {
			this.agent = RT.getAgent();
		} catch (NoClassDefFoundError e) {
			throw new JacocoControllerError(ERROR, e);
		} catch (Exception e) {
			throw new JacocoControllerError(ERROR, e);
		}
	}

	public synchronized void onTestStart(String name) {
		if (m_bTestStarted) {
			throw new JacocoControllerError("Looks like several tests executed in parallel in the same JVM, thus coverage per test can't be recorded correctly.");
		}
		m_bTestStarted = true;
	}

	public synchronized void onTestFinish(String name) {
		dump(name);
		m_bTestStarted = false;
	}

	private void dump(String sessionId) {
		agent.setSessionId(sessionId);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(Paths.REPORT_DIRECTORY_PATH + File.separator + sessionId);
			byte[] executionData = agent.getExecutionData(true);
			out.write(executionData);
		} catch (Exception e) {
			throw new JacocoControllerError(e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static class JacocoControllerError extends Error {
		public JacocoControllerError(String message) {
			super(message);
		}

		public JacocoControllerError(String message, Throwable cause) {
			super(message, cause);
		}

		public JacocoControllerError(Throwable cause) {
			super(cause);
		}
	}
}
