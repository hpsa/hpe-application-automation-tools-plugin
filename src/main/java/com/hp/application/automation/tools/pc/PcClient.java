package com.hp.application.automation.tools.pc;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;

import com.hp.application.automation.tools.common.PcException;
import com.hp.application.automation.tools.model.PcModel;
import com.hp.application.automation.tools.run.PcBuilder;

public class PcClient {

    private PcModel model;
	private PcRestProxy restProxy;
	private boolean loggedIn;
	private PrintStream logger;
	
    public PcClient(PcModel pcModel, PrintStream logger) {
        model = pcModel;
        restProxy = new PcRestProxy(model.getPcServerName(), model.getAlmDomain(), model.getAlmProject());
        this.logger = logger;
    }

    public <T extends PcRestProxy> PcClient(PcModel pcModel, PrintStream logger, T proxy) {
        model = pcModel;
        restProxy = proxy;        
        this.logger = logger;
    }

    public boolean login() {
        try {
            String user = model.getAlmUserName();
            logger.println(String.format("Trying to login\n[PCServer='%s', User='%s']", model.getPcServerName(), user));
            loggedIn = restProxy.authenticate(user, model.getAlmPassword().toString());
        } catch (PcException e) {
            logger.println(e.getMessage());
        } catch (Exception e) {
            logger.println(e);
        }
        logger.println(String.format("Login %s", loggedIn ? "succeeded" : "failed"));
        return loggedIn;
    }

	public boolean isLoggedIn() {
	    
        return loggedIn;
    }

    public int startRun() throws NumberFormatException, ClientProtocolException, PcException, IOException {
	    
	        logger.println("Sending run request\n" + model.runParamsToString());
	        PcRunResponse response = restProxy.startRun(Integer.parseInt(model.getTestId()), 
                            	            Integer.parseInt(model.getTestInstanceId()),  
                            	            model.getTimeslotDuration(), 
                            	            model.getPostRunAction().getValue(), 
                            	            model.isVudsMode());
	        logger.println(String.format("\nRun started (TestID: %s, RunID: %s, TimeslotID: %s)\n", 
                        response.getTestID(), response.getID(), response.getTimeslotID()));
	        return response.getID();            
	}
	
    public PcRunResponse waitForRunCompletion(int runId) throws InterruptedException, ClientProtocolException, PcException, IOException {
        
        return waitForRunCompletion(runId, 5000);
    }

    public PcRunResponse waitForRunCompletion(int runId, int interval) throws InterruptedException, ClientProtocolException, PcException, IOException {
        RunState state = RunState.UNDEFINED;
        switch (model.getPostRunAction()) {
        case DO_NOTHING:
            state = RunState.BEFORE_COLLATING_RESULTS;
            break;
        case COLLATE:
            state = RunState.BEFORE_CREATING_ANALYSIS_DATA;
            break;
        case COLLATE_AND_ANALYZE:
            state = RunState.FINISHED;
            break;
        }       
        return waitForRunState(runId, state, interval);
    }
    
    private PcRunResponse waitForRunState(int runId, RunState completionState, int interval) throws InterruptedException,
            ClientProtocolException, PcException, IOException {
        
        PcRunResponse response = null;
        RunState lastState = RunState.UNDEFINED;
        do {
            response = restProxy.getRunData(runId);
            RunState currentState = RunState.get(response.getRunState());
            if (lastState.ordinal() < currentState.ordinal()) {
                lastState = currentState;
                logger.println(String.format("RunID: %s - State = %s", runId, currentState.value()));
            }
            Thread.sleep(interval);
        } while (lastState.ordinal() < completionState.ordinal());
        return response;
    }

    public FilePath publishRunReport(int runId, String reportDirectory) throws IOException, PcException, InterruptedException {
        PcRunResults runResultsList = restProxy.getRunResults(runId);
        if (runResultsList.getResultsList() != null){
            for (PcRunResult result : runResultsList.getResultsList()) {
                if (result.getName().equals(PcBuilder.pcReportArchiveName)) {
                    File dir = new File(reportDirectory);
                    dir.mkdirs();
                    String reportArchiveFullPath = dir.getCanonicalPath() + IOUtils.DIR_SEPARATOR + PcBuilder.pcReportArchiveName;
                    logger.println("Publishing run report");
                    restProxy.GetRunResultData(runId, result.getID(), reportArchiveFullPath);
                    FilePath fp = new FilePath(new File(reportArchiveFullPath));
                    fp.unzip(fp.getParent());
                    fp.delete();
                    FilePath reportFile = fp.sibling(PcBuilder.pcReportFileName);
                    if (reportFile.exists())
                        return reportFile;
                }
            }
        }
        logger.println("Failed to get run report");
        return null;
    }
        
    public boolean logout() {
        if (!loggedIn)
            return true;

        boolean logoutSucceeded = false;
        try {
            logoutSucceeded = restProxy.logout();
            loggedIn = !logoutSucceeded;
        } catch (PcException e) {
            logger.println(e.getMessage());
        } catch (Exception e) {
            logger.println(e);
        }
        logger.println(String.format("Logout %s", logoutSucceeded ? "succeeded" : "failed"));
        return logoutSucceeded;
    }
    
    public boolean stopRun(int runId) {
        boolean stopRunSucceeded = false;
        try {
            logger.println("Stopping run");
            stopRunSucceeded = restProxy.stopRun(runId, "stop");
        } catch (PcException e) {
            logger.println(e.getMessage());
        } catch (Exception e) {
            logger.println(e);
        }
        logger.println(String.format("Stop run %s", stopRunSucceeded ? "succeeded" : "failed"));
        return stopRunSucceeded;
    }
    
    public PcRunEventLog getRunEventLog(int runId){
        try {
            return restProxy.getRunEventLog(runId);
        } catch (PcException e) {
            logger.println(e.getMessage());
        } catch (Exception e) {
            logger.println(e);
        }
        return null;
    }
}
