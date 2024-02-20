/*
 * Certain versions of software accessible here may contain branding from Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.
 * This software was acquired by Micro Focus on September 1, 2017, and is now offered by OpenText.
 * Any reference to the HP and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright 2012-2023 Open Text
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors ("Open Text") are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.results.projectparser.performance;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


/**
 * Holds information on the SLA's of one scenario (per job / run / build)
 */
public class JobLrScenarioResult extends LrScenario {
    public static final int DEFAULT_CONNECTION_MAX = -1;
    public static final int DEFAULT_SCENARIO_DURATION = -1;
    public ArrayList<GoalResult> scenarioSlaResults;
    public Map<String, Integer> vUserSum;
    public TreeMap<String, Integer> transactionSum;
    public TreeMap<String, TreeMap<String, Integer>> transactionData;
    private int connectionMax;
    private long scenarioDuration;

    public JobLrScenarioResult(String scenarioName) {
        super.setScenrioName(scenarioName);
        connectionMax = DEFAULT_CONNECTION_MAX;
        scenarioDuration = DEFAULT_SCENARIO_DURATION;
        vUserSum = new TreeMap<String, Integer>();
        transactionSum = new TreeMap<String, Integer>();
        transactionData = new TreeMap<>();
        scenarioSlaResults = new ArrayList<GoalResult>(0);
    }

    public long getScenarioDuration() {
        return scenarioDuration;
    }

    public void setScenarioDuration(long scenarioDuration) {
        this.scenarioDuration = scenarioDuration;
    }

    public int getConnectionMax() {
        return connectionMax;
    }

    public void setConnectionMax(int connectionMax) {
        this.connectionMax = connectionMax;
    }
}
