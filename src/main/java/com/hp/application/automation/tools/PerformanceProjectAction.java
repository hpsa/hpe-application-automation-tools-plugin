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


        RunList<? extends Run> projectBuilds = currentProject.getBuilds().completedOnly();

//        updateLastBuild();

        for (Run run : projectBuilds) {
            PerformanceJobReportAction performanceJobReportAction = run.getAction(PerformanceJobReportAction.class);
            if (performanceJobReportAction == null) {
                continue;
            }
            if(_workedBuilds.contains(run.getNumber()))
            {
                continue;
            }

            _workedBuilds.add(run.getNumber());
            LrJobResults jobLrResult = performanceJobReportAction.getLrResultBuildDataset();

            //get all the ran scenario results
            for(Map.Entry<String,jobLrScenarioResult> b : jobLrResult.getLrScenarioResults().entrySet())
            {
                if(!_projectResult.getLrScenarioResults().containsKey(b.getKey())) {
                    _projectResult.addScenrio(new LrProjectScenarioResults(b.getKey()));
                }


                //Join the rule results
                LrProjectScenarioResults targetLrScenarioResult = _projectResult.getLrScenarioResults().get(b.getKey());
                targetLrScenarioResult.averageThroughputResults.put(run.getNumber(), b.getValue().averageThroughputResults);
                targetLrScenarioResult.totalThroughtputResutls.put(run.getNumber(), b.getValue().totalThroughtputResutls);
                targetLrScenarioResult.averageHitsPerSecondResults.put(run.getNumber(), b.getValue().averageHitsPerSecondResults);
                targetLrScenarioResult.totalHitsResults.put(run.getNumber(), b.getValue().totalHitsResults);
                targetLrScenarioResult.errPerSecResults.put(run.getNumber(), b.getValue().errPerSecResults);
                targetLrScenarioResult.percentileTransactionResultsProject.put(run.getNumber(), b.getValue().percentileTransactionResults);
                targetLrScenarioResult.transactionTimeRangesProject.put(run.getNumber(), b.getValue().transactionTimeRanges);

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

            double goalValue = graphData.values().iterator().next().getGoalValue();
            for( WholeRunResult result : graphData.values())
            {
                data.add(result.getActualValue());
            }

            graphDataSet.put("Goal", goalValue);

            graphDataSet.put("labels: ", labels);

            dataset.put("label:", LrTest.SLA_GOAL.TotalHits.toString());
            dataset.put("fillColor:", "rgba(220,220,220,0.2)");
            dataset.put("strokeColor:", "rgba(220,220,220,1)");
            dataset.put("pointColor:", "rgba(220,220,220,1)");
            dataset.put("pointStrokeColor:", "#fff");
            dataset.put("pointHighlightFill:", "#fff");
            dataset.put("pointHighlightStroke:", "rgba(220,220,220,0.2)");

            dataset.put("data: ", data);
            datasets.add(1, dataset);
            graphDataSet.put("datasets: ", datasets);
            scenarionGraphData.put(scenarioResults.getKey(), graphDataSet);
        }

        return scenarionGraphData;
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
}
