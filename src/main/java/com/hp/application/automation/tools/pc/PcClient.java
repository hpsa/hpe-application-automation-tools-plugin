/*
 * MIT License
 *
 * Copyright (c) 2016 Hewlett-Packard Development Company, L.P.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.hp.application.automation.tools.pc;

import hudson.FilePath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;

import hudson.console.HyperlinkNote;
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

//        logger.println("Sending run request:\n" + model.runParamsToString());
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

        int counter = 0;
        RunState[] states = {RunState.BEFORE_COLLATING_RESULTS,RunState.BEFORE_CREATING_ANALYSIS_DATA};
        PcRunResponse response = null;
        RunState lastState = RunState.UNDEFINED;
        do {
            response = restProxy.getRunData(runId);
            RunState currentState = RunState.get(response.getRunState());
            if (lastState.ordinal() < currentState.ordinal()) {
                lastState = currentState;
                logger.println(String.format("RunID: %s - State = %s", runId, currentState.value()));
            }

            // In case we are in state before collate or before analyze, we will wait 1 minute for the state to change otherwise we exit
            // because the user probably stopped the run from PC or timeslot has reached the end.
            if (Arrays.asList(states).contains(currentState)){
                counter++;
                Thread.sleep(1000);
                if(counter > 60 ){
                    logger.println(String.format("RunID: %s  - Stopped from Performance Center side with state = %s", runId, currentState.value()));
                    break;
                }
            }else{
                counter = 0;
                Thread.sleep(interval);
            }
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
                    logger.println("Publishing analysis report");
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

    public void addRunToTrendReport(int runId, String trendReportId){

        TrendReportRequest trRequest = new TrendReportRequest(model.getAlmProject(), runId, null);
        logger.println("Adding run: " + runId + " to trend report: " + trendReportId);
        try {
            restProxy.updateTrendReport(trendReportId, trRequest);
            logger.println("Publishing run: " + runId + " on trend report: " + trendReportId);
        }
        catch (PcException e) {
            logger.println("Failed to add run to trend report: " + e.getMessage());
        }
        catch (IOException e) {
            logger.println("Failed to add run to trend report: Problem connecting to PC Server");
        }
    }

    public void waitForRunToPublishOnTrendReport(int runId, String trendReportId) throws PcException,IOException,InterruptedException{

        ArrayList<PcTrendedRun> trendReportMetaDataResultsList;
        boolean publishEnded = false;
        int counter = 0;

        do {
            trendReportMetaDataResultsList = restProxy.getTrendReportMetaData(trendReportId);

            if (trendReportMetaDataResultsList.isEmpty())  break;

            for (PcTrendedRun result : trendReportMetaDataResultsList) {

                if (result.getRunID() != runId) continue;

                if (result.getState().equals(PcBuilder.TRENDED) || result.getState().equals(PcBuilder.ERROR)){
                    publishEnded = true;
                    logger.println("Run: " + runId + " publishing status: "+ result.getState());
                    break;
                }else{
                    Thread.sleep(5000);
                    logger.println("Publishing...");
                    counter++;
                    if(counter >= 120){
                        logger.println("Error: Publishing didn't ended after 10 minutes, aborting...");
                        break;
                    }
                }
             }

        }while (!publishEnded );
    }

    public boolean downloadTrendReportAsPdf(String trendReportId, String directory) throws PcException {


        try {
            logger.println("Downloading trend report: " + trendReportId + " in PDF format");
            InputStream in = restProxy.getTrendingPDF(trendReportId);
            File dir = new File(directory);
            if(!dir.exists()){
                dir.mkdirs();
            }
            String filePath = directory + IOUtils.DIR_SEPARATOR + "trendReport" + trendReportId + ".pdf";
            Path destination = Paths.get(filePath);
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
            logger.println("Trend report: " + trendReportId + " was successfully downloaded");
        }
        catch (Exception e) {

            logger.println("Failed to download trend report: " + e.getMessage());
            throw new PcException(e.getMessage());
        }

        return true;

    }

    public void publishTrendReport(String filePath, String trendReportId){

        if (filePath == null){return;}
   //     return String.format( HyperlinkNote.encodeTo(filePath, "View trend report " + trendReportId));
        logger.println( HyperlinkNote.encodeTo(filePath, "View trend report " + trendReportId));

    }


}
