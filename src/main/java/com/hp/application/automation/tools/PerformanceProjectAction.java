package com.hp.application.automation.tools;


import com.hp.application.automation.tools.results.PerformanceJobReportAction;
import com.hp.application.automation.tools.results.projectparser.performance.*;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.RunList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ObjectUtils;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.*;
import java.util.logging.Logger;


public class PerformanceProjectAction implements Action{

    private static final String CSV_RESULT_FOLDER = "CSV";
    private static final String JUNIT_RESULT_NAME = "junitResult.xml";
    public static final String X_AXIS_TITLE = "x_axis_title";
    public static final String Y_AXIS_TITLE = "y_axis_title";
    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";
    public final AbstractProject<?, ?> currentProject;
    private ArrayList<LrJobResults> jobLrResults;
    private int lastBuildId = -1;



    /** Logger. */
    private static final Logger LOGGER = Logger
            .getLogger(PerformanceProjectAction.class.getName());

    private ArrayList<Integer> _workedBuilds;
    private ProjectLrResults _projectResult;


    public void getUpdatedData() {
        if (isUpdateDataNeeded()) {
            return;
        }

        _workedBuilds = new ArrayList<Integer>(); //TODO: remove after testing!

        RunList<? extends Run> projectBuilds = currentProject.getBuilds();

//        updateLastBuild();

        for (Run run : projectBuilds) {
            PerformanceJobReportAction performanceJobReportAction = run.getAction(PerformanceJobReportAction.class);
            if (performanceJobReportAction == null) {
                continue;
            }
            if(run.isBuilding())
            {
                continue;
            }

            if(_workedBuilds.contains(run.getNumber()))
            {
                continue;
            }

            _workedBuilds.add(run.getNumber());
            LrJobResults jobLrResult = performanceJobReportAction.getLrResultBuildDataset();

            //get all the ran scenario results
            for(Map.Entry<String, JobLrScenarioResult> runResult : jobLrResult.getLrScenarioResults().entrySet())
            {
                if(!_projectResult.getLrScenarioResults().containsKey(runResult.getKey())) {
                    _projectResult.addScenrio(new LrProjectScenarioResults(runResult.getKey()));
                }

                //Join the rule results
                LrProjectScenarioResults lrProjectScenarioResults = _projectResult.getLrScenarioResults().get(runResult.getKey());
                for(GoalResult goalResult : runResult.getValue().scenarioSlaResults)
                {
                    switch (goalResult.getSlaGoal()) {
                        case AverageThroughput:
                            lrProjectScenarioResults.averageThroughputResults.put(run.getNumber(), (WholeRunResult) goalResult);
                            break;
                        case TotalThroughput:
                            lrProjectScenarioResults.totalThroughtputResults.put(run.getNumber(), (WholeRunResult) goalResult);
                            break;
                        case AverageHitsPerSecond:
                            lrProjectScenarioResults.averageHitsPerSecondResults.put(run.getNumber(), (WholeRunResult) goalResult);
                            break;
                        case TotalHits:
                            lrProjectScenarioResults.totalHitsResults.put(run.getNumber(), (WholeRunResult) goalResult);
                            break;
                        case ErrorsPerSecond:
                            lrProjectScenarioResults.errPerSecResults.put(run.getNumber(), (TimeRangeResult) goalResult);
                            break;
                        case PercentileTRT:
                            if(!lrProjectScenarioResults.percentileTransactionResults.containsKey(run.getNumber()))
                            {
                                lrProjectScenarioResults.percentileTransactionResults.put(run.getNumber(), new HashMap<String, PercentileTransactionWholeRun>(0));
                            }
                            lrProjectScenarioResults.transactions.add(((PercentileTransactionWholeRun) goalResult).getName());
                            lrProjectScenarioResults.percentileTransactionResults.get(run.getNumber()).put(((PercentileTransactionWholeRun) goalResult).getName(), (PercentileTransactionWholeRun) goalResult);
                            break;
                        case AverageTRT:
                            if(!lrProjectScenarioResults.avgTransactionResponseTimeResults.containsKey(run.getNumber()))
                            {
                                lrProjectScenarioResults.avgTransactionResponseTimeResults.put(run.getNumber(), new HashMap<String, AvgTransactionResponseTime>(0));
                            }
                            lrProjectScenarioResults.transactions.add(((AvgTransactionResponseTime) goalResult).getName());
                            lrProjectScenarioResults.avgTransactionResponseTimeResults.get(run.getNumber()).put(((AvgTransactionResponseTime) goalResult).getName(), (AvgTransactionResponseTime) goalResult);
                            break;
                        case Bad:
                            break;
                    }
                }
            }
        }
    }

    private boolean isUpdateDataNeeded()
    {
        int latestBuildNumber = currentProject.getLastBuild().getNumber();
        if(lastBuildId == latestBuildNumber)
        {
            return true;
        }
        return false;
    }

    private void updateLastBuild()
    {

        //TODO: The first one is the last one!!

    }

    @JavaScriptMethod
    public JSONArray getScenarioList()
    {
        JSONArray scenarioList = new JSONArray();
        for(String scenarioName : _projectResult.getLrScenarioResults().keySet())
        {
            JSONObject scenario = new JSONObject();
            scenario.put("ScenarioName", scenarioName);
            scenarioList.add(scenario);
        }
        return scenarioList;
    }

    /**
     * Collates graph data per scenario per build for the whole project.
     * Adds the respected graphs with scenario as the key
     *
     * @return the graph data
     */
    @JavaScriptMethod
    public JSONObject getGraphData()
    {
        JSONObject projectDataSet = new JSONObject ();
        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {

            JSONObject scenarioGraphData = new JSONObject();
            String scenarioName = scenarioResults.getKey();

            Map<Integer, WholeRunResult> totalHitsResults = scenarioResults.getValue().totalHitsResults;
            JSONObject totalHitsGraphSet = extractWholeRunSlaResult(totalHitsResults, "Total hits");
            if (totalHitsGraphSet.getJSONArray("labels").size() != 0)
            {
                totalHitsGraphSet.put(TITLE, "Total Hits");
                totalHitsGraphSet.put(X_AXIS_TITLE, "Build number");
                totalHitsGraphSet.put(Y_AXIS_TITLE,"Hits count");
                totalHitsGraphSet.put(DESCRIPTION, "Displays the number of hits made on the Web server by Vusers " +
                        "during each second of the load test. This graph helps you evaluate the amount of load Vusers " +
                        "generate, in terms of the number of hits.");
                scenarioGraphData.put("totalHits", totalHitsGraphSet);
            }

            Map<Integer, WholeRunResult> avgHitsPerSec = scenarioResults.getValue().averageHitsPerSecondResults;
            JSONObject avgHitsPerSecGraphSet = extractWholeRunSlaResult(avgHitsPerSec, "Average hits per second");
            if (avgHitsPerSecGraphSet.getJSONArray("labels").size() != 0)
            {
                avgHitsPerSecGraphSet.put(TITLE, "Average Hits per Second");
                avgHitsPerSecGraphSet.put(X_AXIS_TITLE, "Build number");
                avgHitsPerSecGraphSet.put(Y_AXIS_TITLE,"Average Hits per Second");
                avgHitsPerSecGraphSet.put(DESCRIPTION, "Displays the number of hits made on the Web server by Vusers " +
                        "during each second of the load test. This graph helps you evaluate the amount of load Vusers " +
                        "generate, in terms of the number of hits.");
                scenarioGraphData.put("avgHitsPerSec", avgHitsPerSecGraphSet);
            }

            Map<Integer, WholeRunResult> totalThroughputResults = scenarioResults.getValue().totalThroughtputResults;
            JSONObject totalThroughputResultsGraphSet = extractWholeRunSlaResult(totalThroughputResults, "Total throughput");
            if (totalThroughputResultsGraphSet.getJSONArray("labels").size() != 0)
            {
                totalThroughputResultsGraphSet.put(TITLE, "Total Throughput");
                totalThroughputResultsGraphSet.put(X_AXIS_TITLE, "Build number");
                totalThroughputResultsGraphSet.put(Y_AXIS_TITLE,"Bytes count");
                totalThroughputResultsGraphSet.put(DESCRIPTION, " Displays the amount of throughput (in bytes) on the Web server during the load test. Throughput represents the amount of data that the Vusers received from the server at any given second. This graph helps you to evaluate the amount of load Vusers generate, in terms of server throughput.\n");
                scenarioGraphData.put("totalThroughput", totalThroughputResultsGraphSet);
            }

            Map<Integer, WholeRunResult> averageThroughputResults = scenarioResults.getValue().averageThroughputResults;
            JSONObject averageThroughputResultsGraphSet = extractWholeRunSlaResult(averageThroughputResults, "Average throughput");
            if (averageThroughputResultsGraphSet.getJSONArray("labels").size() != 0)
            {
                averageThroughputResultsGraphSet.put(TITLE, "Average Throughput per second");
                averageThroughputResultsGraphSet.put(X_AXIS_TITLE, "Build number");
                averageThroughputResultsGraphSet.put(Y_AXIS_TITLE,"Average Bytes / Second");
                averageThroughputResultsGraphSet.put(DESCRIPTION, " Displays the amount of throughput (in bytes) on the Web server during the load test. Throughput represents the amount of data that the Vusers received from the server at any given second. This graph helps you to evaluate the amount of load Vusers generate, in terms of server throughput.\n");
                scenarioGraphData.put("averageThroughput", averageThroughputResultsGraphSet);
            }

            Map<Integer, TimeRangeResult> errPerSecResults = scenarioResults.getValue().errPerSecResults;
            JSONObject errPerSecResultsResultsGraphSet = extractTimeRangeResult(errPerSecResults, LrTest.SLA_GOAL.ErrorsPerSecond.toString());
            if (errPerSecResultsResultsGraphSet.getJSONArray("labels").size() != 0)
            {
                errPerSecResultsResultsGraphSet.put(TITLE, "Total errors per second");
                errPerSecResultsResultsGraphSet.put(X_AXIS_TITLE, "Build number");
                errPerSecResultsResultsGraphSet.put(Y_AXIS_TITLE,"Errors count");
                errPerSecResultsResultsGraphSet.put(DESCRIPTION, "");
                scenarioGraphData.put("errorPerSecResults", errPerSecResultsResultsGraphSet);
            }

            Map<Integer, HashMap<String, AvgTransactionResponseTime>> avgTransactionResponseTimeResults = scenarioResults.getValue().avgTransactionResponseTimeResults;
            JSONObject avgTransactionResponseTimeGraphSet = extractAvgTrtData(avgTransactionResponseTimeResults, scenarioResults.getValue().transactions);
            if (avgTransactionResponseTimeGraphSet.getJSONArray("labels").size() != 0)
            {
                avgTransactionResponseTimeGraphSet.put(TITLE, "Average Transaction Response TIme");
                avgTransactionResponseTimeGraphSet.put(X_AXIS_TITLE, "Build number");
                avgTransactionResponseTimeGraphSet.put(Y_AXIS_TITLE,"Average response time (Seconds)");
                avgTransactionResponseTimeGraphSet.put(DESCRIPTION, "Displays the average time taken to perform transactions during each second of the load test. This graph helps you determine whether the performance of the server is within acceptable minimum and maximum transaction performance time ranges defined for your system.");
                scenarioGraphData.put("averageTransactionResponseTime", avgTransactionResponseTimeGraphSet);
            }


            Map<Integer, HashMap<String, PercentileTransactionWholeRun>> percentileTransactionResults = scenarioResults.getValue().percentileTransactionResults;
            JSONObject percentileTransactionResultsGraphSet = extractPercentileTransactionSet(percentileTransactionResults, scenarioResults.getValue().transactions);
            if (percentileTransactionResultsGraphSet.getJSONArray("labels").size() != 0)
            {
                percentileTransactionResultsGraphSet.put(TITLE, "Percentile Transaction Response TIme");
                percentileTransactionResultsGraphSet.put(X_AXIS_TITLE, "Build number");
                percentileTransactionResultsGraphSet.put(Y_AXIS_TITLE,"Transactions response time (Seconds)");
                percentileTransactionResultsGraphSet.put(DESCRIPTION, "Displays the average time taken to perform transactions during each second of the load test. This graph helps you determine whether the performance of the server is within acceptable minimum and maximum transaction performance time ranges defined for your system.");
                scenarioGraphData.put("percentileTransaction", percentileTransactionResultsGraphSet);
            }

            projectDataSet.put(scenarioName, scenarioGraphData);
        }
        return projectDataSet;
    }


    @JavaScriptMethod
    public JSONObject getTotalHitsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarioGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().totalHitsResults;

            graphDataSet = extractWholeRunSlaResult(graphData, "Total hits");
            scenarioGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
       return scenarioGraphData;
    }

    @JavaScriptMethod
    public JSONObject getAvgHitsPerSecGraphData()
    {
        JSONObject scenarioGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().averageHitsPerSecondResults;

            JSONObject graphDataSet = extractWholeRunSlaResult(graphData, "Average hits per second");
            scenarioGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarioGraphData;
    }

    @JavaScriptMethod
    public JSONObject getTotalThroughputGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().totalThroughtputResults;

            graphDataSet = extractWholeRunSlaResult(graphData, "Total throughput");
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getAvgThroughtputResultsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().averageThroughputResults;


            graphDataSet = extractWholeRunSlaResult(graphData, "Average throughput");
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getAvgTransactionResultsGraphData()
    {
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {

            Map<Integer, HashMap<String, AvgTransactionResponseTime>> graphData = scenarioResults.getValue().avgTransactionResponseTimeResults;
            JSONObject graphDataSet = extractAvgTrtData(graphData, scenarioResults.getValue().transactions);
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);
            return graphDataSet;

        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getPercentelieTransactionResultsGraphData()
    {
        JSONObject scenarionGraphData = new JSONObject();
        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, HashMap<String, PercentileTransactionWholeRun>> graphData = scenarioResults.getValue().percentileTransactionResults;
            JSONObject graphDataSet = extractPercentileTransactionSet(graphData, scenarioResults.getValue().transactions);
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);
            return graphDataSet;
        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getErrorPerSecResultsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarioGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, TimeRangeResult> graphData = scenarioResults.getValue().errPerSecResults;
            graphDataSet = extractTimeRangeResult(graphData,LrTest.SLA_GOAL.ErrorsPerSecond.toString());
            scenarioGraphData.put(scenarioResults.getKey(), graphDataSet);
        }
        return scenarioGraphData;
    }

    private JSONObject extractWholeRunSlaResult(Map<Integer, WholeRunResult> graphData, String graphLabel) {
        JSONObject graphDataSet;
        graphDataSet = new JSONObject();

        JSONArray labels = new JSONArray();
        JSONArray datasets = new JSONArray();
        JSONArray data = new JSONArray();
        for( Map.Entry<Integer, WholeRunResult> result : graphData.entrySet())
        {
            labels.add(result.getKey());
            data.add(result.getValue().getActualValue());
        }
        graphDataSet.put("labels", labels);
        datasets.add(data);
        graphDataSet.put("series", datasets);
        return graphDataSet;
    }

    private JSONObject extractAvgTrtData(Map<Integer, HashMap<String, AvgTransactionResponseTime>> graphData, HashSet<String> transactions) {
        HashMap<String, ArrayList<Double>> averageTRTData = new HashMap<String, ArrayList<Double>>(0);
        JSONObject graphDataSet= new JSONObject();

        JSONArray labels = new JSONArray();
        JSONObject datasetStyle = new JSONObject();

        for(String transaction : transactions)
        {
            averageTRTData.put(transaction, new ArrayList<Double>(0));
        }

        for( Map.Entry<Integer, HashMap<String ,AvgTransactionResponseTime>> result : graphData.entrySet())
        {
            labels.add(result.getKey());

            for(String transaction : transactions)
            {
                if(!result.getValue().containsKey(transaction))
                {
                    averageTRTData.get(transaction).add(null);//TODO:change to null
                    continue;
                }
                averageTRTData.get(transaction).add((result.getValue()).get(transaction).getActualValueAvg());
            }
        }

        graphDataSet.put("labels", labels);
        JSONArray datasets = createGraphDatasets(averageTRTData);
        graphDataSet.put("series", datasets);
        return graphDataSet;
    }

    private JSONObject extractTimeRangeResult(Map<Integer, TimeRangeResult> graphData, String graphLabel) {
        JSONObject graphDataSet;
        graphDataSet = new JSONObject();

        JSONArray labels = new JSONArray();
        JSONArray datasets = new JSONArray();
        JSONArray data = new JSONArray();

        for( Map.Entry<Integer, TimeRangeResult> result : graphData.entrySet())
        {
            if(result.getValue().timeRanges.size() != 0)
            {
                labels.add(result.getKey());
                data.add(result.getValue().getActualValueAvg());
            }
        }

        graphDataSet.put("labels", labels);
        datasets.add(data);
        graphDataSet.put("series", datasets);
        return graphDataSet;
    }

    private JSONObject extractPercentileTransactionSet(Map<Integer, HashMap<String , PercentileTransactionWholeRun>> graphData, HashSet<String> transactions) {
        JSONObject graphDataSet = new JSONObject();
        JSONArray labels = new JSONArray();
        JSONArray datasets = new JSONArray();
        JSONObject datasetStyle = new JSONObject();

        HashMap<String, ArrayList<Double>> percentileTrtData = new HashMap<String, ArrayList<Double>>(0);
        for(String transaction : transactions)
        {
            percentileTrtData.put(transaction, new ArrayList<Double>(0));
        }

        for( Map.Entry<Integer, HashMap<String ,PercentileTransactionWholeRun>> result : graphData.entrySet())
        {
            labels.add(result.getKey());

            for(String transaction : transactions)
            {
                if(!result.getValue().containsKey(transaction))
                {
                    percentileTrtData.get(transaction).add(null);//TODO:change to null
                    continue;
                }
                percentileTrtData.get(transaction).add((result.getValue()).get(transaction).getActualValue());
            }
        }

        graphDataSet.put("labels", labels);
        datasets = createGraphDatasets(percentileTrtData);
        graphDataSet.put("series", datasets);

        return graphDataSet;
    }

    private JSONArray createGraphDatasets(HashMap<String, ArrayList<Double>> averageTRTData) {
        JSONArray datasets = new JSONArray();
        for(Map.Entry<String, ArrayList<Double>> transactionData : averageTRTData.entrySet())
        {
            JSONObject dataset = new JSONObject();
            dataset.put("name", transactionData.getKey());
            JSONArray data = new JSONArray();
            data.addAll(transactionData.getValue());
            dataset.put("data", data);
            datasets.add(dataset);
        }
        return datasets;
    }

    public List<String> getBuildPerformanceReportList(){
//        this.buildPerformanceReportList = new ArrayList<String>(0);
//        if (null == this.currentProject) {
//            return this.buildPerformanceReportList;
//        }

//        if (null == this.currentProject.getSomeBuildWithWorkspace()) {
//            return buildPerformanceReportList;
//        }

//        List<? extends AbstractBuild<?, ?>> builds = currentProject.getBuilds();
//        int nbBuildsToAnalyze = builds.size();
////        Range buildsLimits = getFirstAndLastBuild(request, builds);

//        for (AbstractBuild<?, ?> currentBuild : builds) {
//
//            buildPerformanceReportList.add(currentBuild.getId());
//        }
//        return buildPerformanceReportList;
        return new ArrayList<String>(0);
    }

    public PerformanceProjectAction(AbstractProject<?, ?> project) {

        this._projectResult = new ProjectLrResults();
        this._workedBuilds = new ArrayList<Integer>();
        this.jobLrResults = new ArrayList<LrJobResults>();

        this.currentProject = project;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/hp-application-automation-tools-plugin/PerformanceReport/LoadRunner.png";
    }

    @Override
    public String getDisplayName() {
        return "Project Performance report";
    }

    @Override
    public String getUrlName() {
        return "PerformanceProjectReport";
    }

    @JavaScriptMethod
    public int add(int x, int y) {
        return x+y;
    }

    boolean isVisible()
    {
        getUpdatedData(); // throw this our once fixes method
        if(_workedBuilds.size() != 0)
        {
            return true;
        }
        return false;
    }

}
