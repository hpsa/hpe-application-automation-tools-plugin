/*
 * MIT License
 *
 * Copyright (c) 2016 Hewlett-Packard Development Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
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

package com.hp.application.automation.tools;

import com.hp.application.automation.tools.results.projectparser.performance.AvgTransactionResponseTime;
import com.hp.application.automation.tools.results.projectparser.performance.LrProjectScenarioResults;
import com.hp.application.automation.tools.results.projectparser.performance.PercentileTransactionWholeRun;
import com.hp.application.automation.tools.results.projectparser.performance.TimeRangeResult;
import com.hp.application.automation.tools.results.projectparser.performance.WholeRunResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The type Lr graph utils.
 */
public final class LrGraphUtils {

    /**
     * The constant X_AXIS_TITLE.
     */
    private static final String X_AXIS_TITLE = "x_axis_title";
    /**
     * The constant Y_AXIS_TITLE.
     */
    private static final String Y_AXIS_TITLE = "y_axis_title";
    /**
     * The constant DESCRIPTION.
     */
    private static final String DESCRIPTION = "description";
    /**
     * The constant TITLE.
     */
    private static final String TITLE = "title";
    /**
     * The constant LABELS.
     */
    private static final String LABELS = "labels";
    /**
     * The constant BUILD_NUMBER.
     */
    private static final String BUILD_NUMBER = "Build number";
    /**
     * The constant PERCENTILE_TRANSACTION_RESPONSE_TIME.
     */
    private static final String PERCENTILE_TRANSACTION_RESPONSE_TIME = "Percentile Transaction Response TIme";
    /**
     * The constant TRANSACTIONS_RESPONSE_TIME_SECONDS.
     */
    private static final String TRANSACTIONS_RESPONSE_TIME_SECONDS = "Transactions response time (Seconds)";
    private static final String PRECENTILE_GRAPH_DESCRIPTION =
            "Displays the average time taken to perform transactions during each second of the load test." +
                    " This graph helps you determine whether the performance of the server is within " +
                    "acceptable minimum and maximum transaction performance time ranges defined for your " +
                    "system.";
    public static final String SERIES = "series";

    private LrGraphUtils() {
    }

    /**
     * creates dataset for Percentile transaction graph
     *
     * @param scenarioResults   the relative scenario results to create the graph
     * @param scenarioGraphData the target graph data set
     */
    static void constructPercentileTransactionGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                                    JSONObject scenarioGraphData) {
        Map<Integer, HashMap<String, PercentileTransactionWholeRun>> percentileTransactionResults =
                scenarioResults.getValue().percentileTransactionResults;
        JSONObject percentileTransactionResultsGraphSet =
                extractPercentileTransactionSet(percentileTransactionResults,
                        scenarioResults.getValue().transactions);
        if (percentileTransactionResultsGraphSet.getJSONArray(LABELS).isEmpty()) {
            percentileTransactionResultsGraphSet
                    .put(TITLE, PERCENTILE_TRANSACTION_RESPONSE_TIME);
            percentileTransactionResultsGraphSet
                    .put(X_AXIS_TITLE, BUILD_NUMBER);
            percentileTransactionResultsGraphSet.put(Y_AXIS_TITLE,
                    TRANSACTIONS_RESPONSE_TIME_SECONDS);
            percentileTransactionResultsGraphSet
                    .put(DESCRIPTION, PRECENTILE_GRAPH_DESCRIPTION);
            scenarioGraphData.put("percentileTransaction", percentileTransactionResultsGraphSet);
        }
    }

    private static JSONObject extractPercentileTransactionSet(
            Map<Integer, HashMap<String, PercentileTransactionWholeRun>> graphData, HashSet<String> transactions) {
        JSONObject graphDataSet = new JSONObject();
        JSONArray labels = new JSONArray();

        HashMap<String, ArrayList<Double>> percentileTrtData = new HashMap<String, ArrayList<Double>>(0);
        for (String transaction : transactions) {
            percentileTrtData.put(transaction, new ArrayList<Double>(0));
        }

        for (Map.Entry<Integer, HashMap<String, PercentileTransactionWholeRun>> result : graphData.entrySet()) {
            labels.add(result.getKey());

            for (String transaction : transactions) {
                if (!result.getValue().containsKey(transaction)) {
                    percentileTrtData.get(transaction).add(null);// TODO:change to null
                    continue;
                }
                percentileTrtData.get(transaction).add((result.getValue()).get(transaction).getActualValue());
            }
        }

        graphDataSet.put(LABELS, labels);
        graphDataSet.put(SERIES, createGraphDatasets(percentileTrtData));

        return graphDataSet;
    }

    private static JSONArray createGraphDatasets(HashMap<String, ArrayList<Double>> averageTRTData) {
        JSONArray datasets = new JSONArray();
        for (Map.Entry<String, ArrayList<Double>> transactionData : averageTRTData.entrySet()) {
            JSONObject dataset = new JSONObject();
            dataset.put("name", transactionData.getKey());
            JSONArray data = new JSONArray();
            data.addAll(transactionData.getValue());
            dataset.put("data", data);
            datasets.add(dataset);
        }
        return datasets;
    }

    /**
     * Construct avg transaction graph.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructAvgTransactionGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                             JSONObject scenarioGraphData) {
        Map<Integer, HashMap<String, AvgTransactionResponseTime>> avgTransactionResponseTimeResults =
                scenarioResults.getValue().avgTransactionResponseTimeResults;
        JSONObject avgTransactionResponseTimeGraphSet =
                extractAvgTrtData(avgTransactionResponseTimeResults, scenarioResults.getValue().transactions);
        if (avgTransactionResponseTimeGraphSet.getJSONArray(LABELS).isEmpty()) {
            avgTransactionResponseTimeGraphSet.put(TITLE, "Average Transaction Response TIme");
            avgTransactionResponseTimeGraphSet.put(X_AXIS_TITLE, "Build number");
            avgTransactionResponseTimeGraphSet
                    .put(Y_AXIS_TITLE, "Average response time (Seconds)");
            avgTransactionResponseTimeGraphSet.put(DESCRIPTION,
                    "Displays the average time taken to perform transactions during each second of the load test." +
                            " This graph helps you determine whether the performance of the server is within " +
                            "acceptable minimum and maximum transaction performance time ranges defined for your " +
                            "system.");
            scenarioGraphData.put("averageTransactionResponseTime", avgTransactionResponseTimeGraphSet);
        }
    }

    private static JSONObject extractAvgTrtData(Map<Integer, HashMap<String, AvgTransactionResponseTime>> graphData,
                                                HashSet<String> transactions) {
        HashMap<String, ArrayList<Double>> averageTRTData = new HashMap<String, ArrayList<Double>>(0);
        JSONObject graphDataSet = new JSONObject();

        JSONArray labels = new JSONArray();

        for (String transaction : transactions) {
            averageTRTData.put(transaction, new ArrayList<Double>(0));
        }

        for (Map.Entry<Integer, HashMap<String, AvgTransactionResponseTime>> result : graphData.entrySet()) {
            labels.add(result.getKey());

            for (String transaction : transactions) {
                if (!result.getValue().containsKey(transaction)) {
                    averageTRTData.get(transaction).add(null);
                    // TODO:change to null
                    continue;
                }
                averageTRTData.get(transaction).add((result.getValue()).get(transaction).getActualValueAvg());
            }
        }

        graphDataSet.put(LABELS, labels);
        JSONArray datasets = createGraphDatasets(averageTRTData);
        graphDataSet.put(SERIES, datasets);
        return graphDataSet;
    }

    /**
     * Construct error graph.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructErrorGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                    JSONObject scenarioGraphData) {
        Map<Integer, TimeRangeResult> errPerSecResults = scenarioResults.getValue().errPerSecResults;
        JSONObject errPerSecResultsResultsGraphSet =
                extractTimeRangeResult(errPerSecResults);
        if (errPerSecResultsResultsGraphSet.getJSONArray(LABELS).isEmpty()) {
            errPerSecResultsResultsGraphSet.put(TITLE, "Total errors per second");
            errPerSecResultsResultsGraphSet.put(X_AXIS_TITLE, "Build number");
            errPerSecResultsResultsGraphSet.put(Y_AXIS_TITLE, "Errors count");
            errPerSecResultsResultsGraphSet.put(DESCRIPTION, "");
            scenarioGraphData.put("errorPerSecResults", errPerSecResultsResultsGraphSet);
        }
    }

    private static JSONObject extractTimeRangeResult(Map<Integer, TimeRangeResult> graphData) {
        JSONObject graphDataSet;
        graphDataSet = new JSONObject();

        JSONArray labels = new JSONArray();
        JSONArray datasets = new JSONArray();
        JSONArray data = new JSONArray();

        for (Map.Entry<Integer, TimeRangeResult> result : graphData.entrySet()) {
            if (result.getValue().timeRanges.isEmpty()) {
                labels.add(result.getKey());
                data.add(result.getValue().getActualValueAvg());
            }
        }

        graphDataSet.put(LABELS, labels);
        datasets.add(data);
        graphDataSet.put(SERIES, datasets);
        return graphDataSet;
    }

    /**
     * Construct average throughput.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructAverageThroughput(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                           JSONObject scenarioGraphData) {
        Map<Integer, WholeRunResult> averageThroughputResults = scenarioResults.getValue().averageThroughputResults;
        JSONObject averageThroughputResultsGraphSet =
                extractWholeRunSlaResult(averageThroughputResults);
        if (averageThroughputResultsGraphSet.getJSONArray(LABELS).isEmpty()) {
            averageThroughputResultsGraphSet.put(TITLE, "Average Throughput per second");
            averageThroughputResultsGraphSet.put(X_AXIS_TITLE, "Build number");
            averageThroughputResultsGraphSet.put(Y_AXIS_TITLE, "Average Bytes / Second");
            averageThroughputResultsGraphSet.put(DESCRIPTION,
                    " Displays the amount of throughput (in bytes) on the Web server during the load test. " +
                            "Throughput represents the amount of data that the Vusers received from the server at" +
                            " any given second. This graph helps you to evaluate the amount of load Vusers " +
                            "generate, in terms of server throughput.\n");
            scenarioGraphData.put("averageThroughput", averageThroughputResultsGraphSet);
        }
    }

    /**
     * @param graphData
     * @return
     */
    private static JSONObject extractWholeRunSlaResult(Map<Integer, WholeRunResult> graphData) {
        JSONObject graphDataSet;
        graphDataSet = new JSONObject();

        JSONArray labels = new JSONArray();
        JSONArray datasets = new JSONArray();
        JSONArray data = new JSONArray();
        for (Map.Entry<Integer, WholeRunResult> result : graphData.entrySet()) {
            labels.add(result.getKey());
            data.add(result.getValue().getActualValue());
        }
        graphDataSet.put(LABELS, labels);
        datasets.add(data);
        graphDataSet.put(SERIES, datasets);
        return graphDataSet;
    }

    /**
     * Construct total throughput graph.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructTotalThroughputGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                              JSONObject scenarioGraphData) {
        Map<Integer, WholeRunResult> totalThroughputResults = scenarioResults.getValue().totalThroughtputResults;
        JSONObject totalThroughputResultsGraphSet =
                extractWholeRunSlaResult(totalThroughputResults);
        if (totalThroughputResultsGraphSet.getJSONArray(LABELS).isEmpty()) {
            totalThroughputResultsGraphSet.put(TITLE, "Total Throughput");
            totalThroughputResultsGraphSet.put(X_AXIS_TITLE, "Build number");
            totalThroughputResultsGraphSet.put(Y_AXIS_TITLE, "Bytes count");
            totalThroughputResultsGraphSet.put(DESCRIPTION,
                    " Displays the amount of throughput (in bytes) on the Web server during the load test. " +
                            "Throughput represents the amount of data that the Vusers received from the server at" +
                            " any given second. This graph helps you to evaluate the amount of load Vusers " +
                            "generate, in terms of server throughput.\n");
            scenarioGraphData.put("totalThroughput", totalThroughputResultsGraphSet);
        }
    }

    /**
     * Construct avg hits graph.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructAvgHitsGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                      JSONObject scenarioGraphData) {
        Map<Integer, WholeRunResult> avgHitsPerSec = scenarioResults.getValue().averageHitsPerSecondResults;
        JSONObject avgHitsPerSecGraphSet = extractWholeRunSlaResult(avgHitsPerSec);
        if (avgHitsPerSecGraphSet.getJSONArray(LABELS).isEmpty()) {
            avgHitsPerSecGraphSet.put(TITLE, "Average Hits per Second");
            avgHitsPerSecGraphSet.put(X_AXIS_TITLE, "Build number");
            avgHitsPerSecGraphSet.put(Y_AXIS_TITLE, "Average Hits per Second");
            avgHitsPerSecGraphSet.put(DESCRIPTION,
                    "Displays the number of hits made on the Web server by Vusers " +
                            "during each second of the load test. This graph helps you evaluate the amount of load " +
                            "Vusers" +
                            " " +
                            "generate, in terms of the number of hits.");
            scenarioGraphData.put("avgHitsPerSec", avgHitsPerSecGraphSet);
        }
    }

    /**
     * Construct total hits graph.
     *
     * @param scenarioResults   the scenario results
     * @param scenarioGraphData the scenario graph data
     */
    static void constructTotalHitsGraph(Map.Entry<String, LrProjectScenarioResults> scenarioResults,
                                        JSONObject scenarioGraphData) {
        Map<Integer, WholeRunResult> totalHitsResults = scenarioResults.getValue().totalHitsResults;
        JSONObject totalHitsGraphSet = extractWholeRunSlaResult(totalHitsResults);
        if (totalHitsGraphSet.getJSONArray(LABELS).isEmpty()) {
            totalHitsGraphSet.put(TITLE, "Total Hits");
            totalHitsGraphSet.put(X_AXIS_TITLE, "Build number");
            totalHitsGraphSet.put(Y_AXIS_TITLE, "Hits count");
            totalHitsGraphSet.put(DESCRIPTION,
                    "Displays the number of hits made on the Web server by Vusers " +
                            "during each second of the load test. This graph helps you evaluate the amount of load " +
                            "Vusers" +
                            " " +
                            "generate, in terms of the number of hits.");
            scenarioGraphData.put("totalHits", totalHitsGraphSet);
        }
    }
}
