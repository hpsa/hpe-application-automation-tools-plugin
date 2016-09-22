package com.hp.application.automation.tools;


import com.hp.application.automation.tools.results.PerformanceJobReportAction;
import com.hp.application.automation.tools.results.projectparser.performance.*;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.util.RunList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class PerformanceProjectAction implements Action{

    private static final String CSV_RESULT_FOLDER = "CSV";
    private static final String JUNIT_RESULT_NAME = "junitResult.xml";
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
            for(Map.Entry<String,jobLrScenarioResult> runResult : jobLrResult.getLrScenarioResults().entrySet())
            {
                if(!_projectResult.getLrScenarioResults().containsKey(runResult.getKey())) {
                    _projectResult.addScenrio(new LrProjectScenarioResults(runResult.getKey()));
                }


                //Join the rule results
                LrProjectScenarioResults targetLrScenarioResult = _projectResult.getLrScenarioResults().get(runResult.getKey());
                targetLrScenarioResult.averageThroughputResults.put(run.getNumber(), runResult.getValue().averageThroughputResults);
                targetLrScenarioResult.totalThroughtputResutls.put(run.getNumber(), runResult.getValue().totalThroughtputResutls);
                targetLrScenarioResult.averageHitsPerSecondResults.put(run.getNumber(), runResult.getValue().averageHitsPerSecondResults);
                targetLrScenarioResult.totalHitsResults.put(run.getNumber(), runResult.getValue().totalHitsResults);
                targetLrScenarioResult.errPerSecResults.put(run.getNumber(), runResult.getValue().errPerSecResults);
                targetLrScenarioResult.percentileTransactionResultsProject.put(run.getNumber(), runResult.getValue().percentileTransactionResults);
                targetLrScenarioResult.transactionTimeRangesProject.put(run.getNumber(), runResult.getValue().transactionTimeRanges);

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
    public JSONObject getTotalHitsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().totalHitsResults;

            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());

            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());
            for( WholeRunResult result : graphData.values())
            {
                data.add(result.getActualValue());
            }

//            graphDataSet.put("Goal", goalValue);

           graphDataSet.put("labels", labels);

            dataset.put("label", LrTest.SLA_GOAL.TotalHits + " of scenario " + scenarioResults.getKey().toString());
//            dataset.put("fill", "false");
//            dataset.put("lineTension", "0.1");
//            dataset.put("backgroundColor", "rgba(220,220,220,0.2)");
//            dataset.put("borderColor", "rgba(220,220,220,1)");
//            dataset.put("borderCapStyle", "butt");
//            dataset.put("borderDash", "[]");
//            dataset.put("borderDash", "[]");
//            dataset.put("pointBackgroundColor", "rgba(220,220,220,1)");
//            dataset.put("pointBorderColor", "#fff");
//            dataset.put("pointHoverBorderWidth", "1");
//            dataset.put("pointHoverBorderWidth", "1");
//            dataset.put("pointRadius", "1");
//            dataset.put("pointHitRadius", "10");

            dataset.put("fillColor", "rgba(220,220,220,0.2)");
            dataset.put("strokeColor", "rgba(220,220,220,1)");
            dataset.put("pointColor", "rgba(220,220,220,1)");
            dataset.put("pointStrokeColor", "#fff");
            dataset.put("pointHighlightFill", "#fff");
            dataset.put("pointHighlightStroke", "rgba(220,220,220,0.2)");

            dataset.put("data", data);
            datasets.add(dataset);
            graphDataSet.put("datasets", datasets);
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
       return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getAvgHitsPerSecGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarioGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().averageHitsPerSecondResults;

            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());

            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());
            for( WholeRunResult result : graphData.values())
            {
                data.add(result.getActualValue());
            }

//            graphDataSet.put("Goal", goalValue);

            graphDataSet.put("labels", labels);

            dataset.put("label", LrTest.SLA_GOAL.AverageHitsPerSecond);
            dataset.put("fillColor", "rgba(220,220,21,0.2)");
            dataset.put("strokeColor", "rgba(220,42,220,1)");
            dataset.put("pointColor", "rgba(34,220,220,1)");
            dataset.put("pointStrokeColor", "#fff");
            dataset.put("pointHighlightFill", "#fff");
            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");

            dataset.put("data", data);
            datasets.add(dataset);
            graphDataSet.put("datasets", datasets);
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
            Map<Integer, WholeRunResult> graphData = scenarioResults.getValue().totalThroughtputResutls;

            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());

            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());
            for( WholeRunResult result : graphData.values())
            {
                data.add(result.getActualValue());
            }

//            graphDataSet.put("Goal", goalValue);

            graphDataSet.put("labels", labels);

            dataset.put("label", LrTest.SLA_GOAL.AverageHitsPerSecond);
            dataset.put("fillColor", "rgba(220,220,21,0.2)");
            dataset.put("strokeColor", "rgba(220,42,220,1)");
            dataset.put("pointColor", "rgba(34,220,220,1)");
            dataset.put("pointStrokeColor", "#fff");
            dataset.put("pointHighlightFill", "#fff");
            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");

            dataset.put("data", data);
            datasets.add(dataset);
            graphDataSet.put("datasets", datasets);
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

            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());

            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());
            for( WholeRunResult result : graphData.values())
            {
                data.add(result.getActualValue());
            }

//            graphDataSet.put("Goal", goalValue);

            graphDataSet.put("labels", labels);

            dataset.put("label", LrTest.SLA_GOAL.AverageHitsPerSecond);
            dataset.put("fillColor", "rgba(220,220,21,0.2)");
            dataset.put("strokeColor", "rgba(220,42,220,1)");
            dataset.put("pointColor", "rgba(34,220,220,1)");
            dataset.put("pointStrokeColor", "#fff");
            dataset.put("pointHighlightFill", "#fff");
            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");

            dataset.put("data", data);
            datasets.add(dataset);
            graphDataSet.put("datasets", datasets);
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getAvgTransactionResultsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, ArrayList<TransactionTimeRange>> graphData = scenarioResults.getValue().transactionTimeRangesProject;
            HashMap<String, ArrayList<Double>> averageTRTData = new HashMap<String, ArrayList<Double>>(0);
            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());
            graphDataSet.put("labels", labels);

//            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());

            for( ArrayList<TransactionTimeRange> result : graphData.values())
            {
                for(TransactionTimeRange transactionTimeRange : result)
                {
                    if(averageTRTData.containsKey(transactionTimeRange.getName()))
                    {
                        averageTRTData.get(transactionTimeRange.getName()).add(transactionTimeRange.getActualValueAvg());
                    }
                    else
                    {
                        ArrayList<Double> temp = new ArrayList<Double>(0);
                        temp.add(transactionTimeRange.getActualValueAvg());
                        averageTRTData.put(transactionTimeRange.getName(),temp);
                    }
                }
            }

            createGraphDatasets(averageTRTData, datasets);

            graphDataSet.put("datasets", datasets);



//            graphDataSet.put("Goal", goalValue);


//            dataset.put("fillColor", "rgba(220,220,21,0.2)");
//            dataset.put("strokeColor", "rgba(220,42,220,1)");
//            dataset.put("pointColor", "rgba(34,220,220,1)");
//            dataset.put("pointStrokeColor", "#fff");
//            dataset.put("pointHighlightFill", "#fff");
//            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");


            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarionGraphData;
    }

    @JavaScriptMethod
    public JSONObject getPercentelieTransactionResultsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarionGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, ArrayList<PercentileTransactionWholeRun>> graphData = scenarioResults.getValue().percentileTransactionResultsProject;
            HashMap<String, ArrayList<Double>> averageTRTData = new HashMap<String, ArrayList<Double>>(0);
            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());
            graphDataSet.put("labels", labels);

//            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());

            for( ArrayList<PercentileTransactionWholeRun> result : graphData.values())
            {
                for(PercentileTransactionWholeRun percentileTransactionWholeRun : result)
                {
                    if(averageTRTData.containsKey(percentileTransactionWholeRun.getName()))
                    {
                        (averageTRTData.get(percentileTransactionWholeRun.getName())).add(percentileTransactionWholeRun.getActualValue());
                    }
                    else
                    {
                        ArrayList<Double> temp = new ArrayList<Double>(0);
                        temp.add(percentileTransactionWholeRun.getActualValue());
                        averageTRTData.put(percentileTransactionWholeRun.getName(),temp);
                    }
                }
            }

            createGraphDatasets(averageTRTData, datasets);

            graphDataSet.put("datasets", datasets);



//            graphDataSet.put("Goal", goalValue);


//            dataset.put("fillColor", "rgba(220,220,21,0.2)");
//            dataset.put("strokeColor", "rgba(220,42,220,1)");
//            dataset.put("pointColor", "rgba(34,220,220,1)");
//            dataset.put("pointStrokeColor", "#fff");
//            dataset.put("pointHighlightFill", "#fff");
//            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");


            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarionGraphData;
    }

    private void createGraphDatasets(HashMap<String, ArrayList<Double>> averageTRTData, JSONArray datasets) {
        for(Map.Entry<String, ArrayList<Double>> transactionData : averageTRTData.entrySet())
        {
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            dataset.put("label", transactionData.getKey());
            data.addAll(transactionData.getValue());
            dataset.put("data", data);
            datasets.add(dataset);
        }
    }

    @JavaScriptMethod
    public JSONObject getErrorPerSecResultsGraphData()
    {
        JSONObject graphDataSet;
        JSONObject scenarioGraphData = new JSONObject();

        for(Map.Entry<String, LrProjectScenarioResults> scenarioResults: _projectResult.getLrScenarioResults().entrySet() ) {
            Map<Integer, TimeRangeResult> graphData = scenarioResults.getValue().errPerSecResults;

            graphDataSet = new JSONObject();

            JSONArray labels = new JSONArray();
            JSONArray datasets = new JSONArray();
            JSONObject dataset = new JSONObject();
            JSONArray data = new JSONArray();
            JSONObject datasetStyle = new JSONObject();

            labels.addAll(graphData.keySet());

//            String goalValue = String.valueOf(graphData.values().iterator().next().getGoalValue());
            for( TimeRangeResult result : graphData.values())
            {
                data.add(result.getActualValueAvg());
            }

//            graphDataSet.put("Goal", goalValue);

            graphDataSet.put("labels", labels);

            dataset.put("label", LrTest.SLA_GOAL.ErrorsPerSecond);
            dataset.put("fillColor", "rgba(220,220,21,0.2)");
            dataset.put("strokeColor", "rgba(220,42,220,1)");
            dataset.put("pointColor", "rgba(34,220,220,1)");
            dataset.put("pointStrokeColor", "#fff");
            dataset.put("pointHighlightFill", "#fff");
            dataset.put("pointHighlightStroke", "rgba(220,158,220,0.2)");

            dataset.put("data", data);
            datasets.add(dataset);
            graphDataSet.put("datasets", datasets);
            scenarioGraphData.put(scenarioResults.getKey(), graphDataSet);

            return graphDataSet;

        }
        return scenarioGraphData;
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
