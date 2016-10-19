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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Holds information on the SLA's of one scenario (per job / run / build)
 */
public class JobLrScenarioResult extends LrScenario {
    public static final int DEFAULT_CONNECTION_MAX = -1;
    public ArrayList<GoalResult> scenarioSlaResults;
    public Map<String, Integer> vUserSum;
    public HashMap<String, Integer> transactionSum;
    public HashMap<String, HashMap<String, Integer>> transactionData;
    int connectionMax;

    public JobLrScenarioResult(String scenarioName) {
        super.setScenrioName(scenarioName);
        connectionMax = DEFAULT_CONNECTION_MAX;
        vUserSum = new HashMap<String, Integer>(0);
        transactionSum = new HashMap<String, Integer>(0);
        transactionData = new HashMap<String, HashMap<String, Integer>>(0);
        scenarioSlaResults = new ArrayList<GoalResult>(0);
    }

    public int getConnectionMax() {
        return connectionMax;
    }

    public void setConnectionMax(int connectionMax) {
        this.connectionMax = connectionMax;
    }
}
