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

/**
 * Data model for a project / build / pipeline that contains multiple runs per scenario being run.
 */
public class LrProjectScenarioResults extends LrScenario {

    //Holds the data of an SLA rule per run for the whole Job
    public HashMap<Integer, WholeRunResult> averageThroughputResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> totalThroughtputResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> averageHitsPerSecondResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, WholeRunResult> totalHitsResults = new HashMap<Integer, WholeRunResult>(0);
    public HashMap<Integer, TimeRangeResult> errPerSecResults = new HashMap<Integer, TimeRangeResult>(0);
    public HashMap<Integer, HashMap<String, PercentileTransactionWholeRun>> percentileTransactionResults = new HashMap<Integer, HashMap<String, PercentileTransactionWholeRun>>(0);
    public HashMap<Integer, HashMap<String, AvgTransactionResponseTime>> avgTransactionResponseTimeResults = new HashMap<Integer, HashMap<String, AvgTransactionResponseTime>>(0);

    //Holds the data of a transactionss for the whole Job
    public HashSet<String> transactions = new HashSet<String>(0);
    //Holds the data of connections per run for the whole Job
    public HashMap<Integer, Integer> maxConnectionsCount = new HashMap<Integer, Integer>(0);
    //Holds the summary data of vuser status(count, fail, pass, error) for the whole Job
    public Map<String, Integer> vUserSummary = new HashMap<String, Integer>(0);
    //Holds the summary data of vuser status(count, fail, pass, error) **per run** for the whole Job
    public Map<Integer, Map<String, Integer>> vUserPerRun = new HashMap<Integer, Map<String, Integer>>(0);
    //Holds the summary status data of transactions(count, fail, pass, error) for the whole Job
    public Map<String, Integer> transactionSum = new HashMap<String, Integer>(0);
    //Holds the summary data of transaction status(count, fail, pass, error) **per run** for the whole Job
    public Map<String, HashMap<String, Integer>> transactionData = new HashMap<String, HashMap<String, Integer>>(0);

    public LrProjectScenarioResults(String scenarioName) {
        this.setScenrioName(scenarioName);
    }

}
