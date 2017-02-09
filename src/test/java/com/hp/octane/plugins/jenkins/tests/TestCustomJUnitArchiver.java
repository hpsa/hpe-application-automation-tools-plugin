package com.hp.octane.plugins.jenkins.tests;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import hudson.tasks.test.AbstractTestResultAction;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class TestCustomJUnitArchiver extends Recorder {

	private String resultFile;

	public TestCustomJUnitArchiver(String resultFile) {
		this.resultFile = resultFile;
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		build.addAction(new TestResultAction());
		InputStream is = build.getWorkspace().child(resultFile).read();
		String junitResults = IOUtils.toString(is, "UTF-8");
		junitResults = junitResults
				.replaceAll("%%%WORKSPACE%%%", build.getWorkspace().getRemote())
				.replaceAll("%%%SEPARATOR%%%", File.separator.equals("\\") ? "\\\\" : File.separator);
		IOUtils.closeQuietly(is);
		new FilePath(build.getRootDir()).child("junitResult.xml").write(junitResults, "UTF-8");
		return true;
	}

	private static class TestResultAction extends AbstractTestResultAction {

		@Override
		public int getFailCount() {
			return 0;
		}

		@Override
		public int getTotalCount() {
			return 0;
		}

		@Override
		public Object getResult() {
			return new Object();
		}
	}
}
