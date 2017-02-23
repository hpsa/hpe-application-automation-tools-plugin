/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.octane.plugins.jenkins.tests.junit;

public enum TestResultStatus {

    PASSED("Passed"),
    SKIPPED("Skipped"),
    FAILED("Failed");

    private final String prettyName;

    private TestResultStatus(String prettyName) {
        this.prettyName = prettyName;
    }

    public String toPrettyName() {
        return prettyName;
    }

    public static TestResultStatus fromPrettyName(String prettyName) {
        for (TestResultStatus status : values()) {
            if (status.toPrettyName().equals(prettyName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported TestResultStatus '" + prettyName + "'.");
    }
}
