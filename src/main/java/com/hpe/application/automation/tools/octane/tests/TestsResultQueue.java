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

import com.hpe.application.automation.tools.octane.AbstractResultQueueImpl;
import hudson.Extension;
import jenkins.model.Jenkins;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;

@Extension
@SuppressWarnings("squid:S2259")
public class TestsResultQueue extends AbstractResultQueueImpl {

    public TestsResultQueue() throws IOException {
        Jenkins instance =Jenkins.getInstance();
        if(instance==null){
            Assert.isNull(instance);
        }
        File queueFile = new File(instance.getRootDir(), "octane-test-result-queue.dat");
        init(queueFile);
    }

    /*
     * To be used in tests only.
     */
    TestsResultQueue(File queueFile) throws IOException {
        init(queueFile);
    }
}
