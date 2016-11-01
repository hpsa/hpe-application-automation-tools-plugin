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

package com.hp.application.automation.tools.results.projectparser.performance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Data model for a project / build / pipeline that contains multiple runs per scenario being run.
 */
public class LrProjectScenarioResults extends LrScenario {

    private Map<Integer, WholeRunResult> totalThroughtputResults;
    private Map<Integer, WholeRunResult> averageHitsPerSecondResults;
    private Map<Integer, WholeRunResult> totalHitsResults;
    private Map<Integer, TimeRangeResult> errPerSecResults;
    private Map<Integer, HashMap<String, PercentileTransactionWholeRun>> percentileTransactionResults;
    private Map<Integer, HashMap<String, AvgTransactionResponseTime>> avgTransactionResponseTimeResults;
    //Holds the data of a transactionss for the whole Job
    private HashSet<String> transactions;
    //Holds the data of connections per run for the whole Job
    private Map<Integer, Integer> maxConnectionsCount;
    //Holds the summary data of vuser status(count, fail, pass, error) for the whole Job
    private Map<String, Integer> vUserSummary;
    //Holds the summary data of vuser status(count, fail, pass, error) **per run** for the whole Job
    private Map<Integer, Map<String, Integer>> vUserPerRun;
    //Holds the summary status data of transactions(count, fail, pass, error) for the whole Job
    private Map<String, Integer> transactionSum;
    //Holds the summary data of transaction status(count, fail, pass, error) **per run** for the whole Job
    private Map<String, HashMap<String, Integer>> transactionData;
    //Holds the duration aggragation for all scenario runs
    private Map<Integer, Long> durationData;
    //Holds the data of an SLA rule per run for the whole Job
    private Map<Integer, WholeRunResult> averageThroughputResults;
    private HashMap<Integer, HashMap<String, HashMap<String, Integer>>> transactionPerRun;
    /**
     * Instantiates a new Lr project scenario results.
     *
     * @param scenarioName the scenario name
     */
    public LrProjectScenarioResults(String scenarioName) {
        this.setScenrioName(scenarioName);
        averageThroughputResults = new HashMap<Integer, WholeRunResult>(0);
        totalThroughtputResults = new HashMap<Integer, WholeRunResult>(0);
        averageHitsPerSecondResults = new HashMap<Integer, WholeRunResult>(0);
        totalHitsResults = new HashMap<Integer, WholeRunResult>(0);
        errPerSecResults = new HashMap<Integer, TimeRangeResult>(0);
        percentileTransactionResults = new HashMap<Integer, HashMap<String, PercentileTransactionWholeRun>>(0);
        avgTransactionResponseTimeResults = new HashMap<Integer, HashMap<String, AvgTransactionResponseTime>>(0);
        transactions = new HashSet<String>(0);
        maxConnectionsCount = new HashMap<Integer, Integer>(0);

        durationData = new HashMap<>(0);

        vUserSummary = new HashMap<String, Integer>(0);
        vUserPerRun = new HashMap<Integer, Map<String, Integer>>(0);

        transactionSum = new HashMap<String, Integer>(0);
        transactionData = new HashMap<String, HashMap<String, Integer>>(0);
        transactionPerRun = new HashMap<Integer, HashMap<String, HashMap<String, Integer>>>(0);

        vUserMapInit(vUserSummary);
        vTransactionMapInit(transactionSum);
    }

    /**
     * initilize vuser maps with required values
     *
     * @param map the map
     */
    public static void vUserMapInit(Map<String, Integer> map) {
        map.put("Passed", 0);
        map.put("Stopped", 0);
        map.put("Failed", 0);
        map.put("Count", 0);
        map.put("MaxVuserRun", 0);
    }

    /**
     * initilize vuser maps with required values
     *
     * @param map the map
     */
    public static void vTransactionMapInit(Map<String, Integer> map) {
        map.put("Pass", 0);
        map.put("Stop", 0);
        map.put("Fail", 0);
        map.put("Count", 0);
    }

    /**
     * Gets total throughtput results.
     *
     * @return the total throughtput results
     */
    public Map<Integer, WholeRunResult> getTotalThroughtputResults() {
        return totalThroughtputResults;
    }

    /**
     * Gets average hits per second results.
     *
     * @return the average hits per second results
     */
    public Map<Integer, WholeRunResult> getAverageHitsPerSecondResults() {
        return averageHitsPerSecondResults;
    }

    /**
     * Gets total hits results.
     *
     * @return the total hits results
     */
    public Map<Integer, WholeRunResult> getTotalHitsResults() {
        return totalHitsResults;
    }

    /**
     * Gets err per sec results.
     *
     * @return the err per sec results
     */
    public Map<Integer, TimeRangeResult> getErrPerSecResults() {
        return errPerSecResults;
    }

    /**
     * Gets percentile transaction results.
     *
     * @return the percentile transaction results
     */
    public Map<Integer, HashMap<String, PercentileTransactionWholeRun>> getPercentileTransactionResults() {
        return percentileTransactionResults;
    }

    /**
     * Gets avg transaction response time results.
     *
     * @return the avg transaction response time results
     */
    public Map<Integer, HashMap<String, AvgTransactionResponseTime>> getAvgTransactionResponseTimeResults() {
        return avgTransactionResponseTimeResults;
    }

    /**
     * Gets transactions.
     *
     * @return the transactions
     */
    public Set<String> getTransactions() {
        return transactions;
    }

    /**
     * Gets max connections count.
     *
     * @return the max connections count
     */
    public Map<Integer, Integer> getMaxConnectionsCount() {
        return maxConnectionsCount;
    }

    /**
     * Gets user summary.
     *
     * @return the user summary
     */
    public Map<String, Integer> getvUserSummary() {
        return vUserSummary;
    }

    /**
     * Gets user per run.
     *
     * @return the user per run
     */
    public Map<Integer, Map<String, Integer>> getvUserPerRun() {
        return vUserPerRun;
    }

    /**
     * Gets transaction sum.
     *
     * @return the transaction sum
     */
    public Map<String, Integer> getTransactionSum() {
        return transactionSum;
    }

    /**
     * Gets transaction data.
     *
     * @return the transaction data
     */
    public Map<String, HashMap<String, Integer>> getTransactionData() {
        return transactionData;
    }

    /**
     * Gets duration data.
     *
     * @return the duration data
     */
    public Map<Integer, Long> getDurationData() {
        return durationData;
    }

    /**
     * Gets average throughput results.
     *
     * @return the average throughput results
     */
    public Map<Integer, WholeRunResult> getAverageThroughputResults() {
        return averageThroughputResults;
    }

    /**
     * Gets transaction per run.
     *
     * @return the transaction per run
     */
    public Map<Integer, HashMap<String, HashMap<String, Integer>>> getTransactionPerRun() {
        return transactionPerRun;
    }
}
