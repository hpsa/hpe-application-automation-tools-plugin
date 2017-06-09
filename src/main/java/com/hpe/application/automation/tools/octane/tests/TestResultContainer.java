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

package com.hpe.application.automation.tools.octane.tests;

import com.hpe.application.automation.tools.octane.tests.detection.ResultFields;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;

import java.util.Iterator;

public class TestResultContainer {

    private Iterator<TestResult> iterator;
    private ResultFields resultFields;

    public TestResultContainer(Iterator<TestResult> iterator, ResultFields resultFields) {
        this.iterator = iterator;
        this.resultFields = resultFields;
    }

    public Iterator<TestResult> getIterator() {
        return iterator;
    }

    public ResultFields getResultFields() {
        return resultFields;
    }
}
