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

package com.hp.octane.plugins.jenkins.buildLogs;

import com.hp.octane.plugins.jenkins.AbstractResultQueueImpl;
import jenkins.model.Jenkins;

import java.io.File;
import java.io.IOException;

/**
 * Created by benmeior on 11/21/2016.
 */
public class LogAbstractResultQueue extends AbstractResultQueueImpl {

    public LogAbstractResultQueue() throws IOException {
        File queueFile = new File(Jenkins.getInstance().getRootDir(), "octane-log-result-queue.dat");
        init(queueFile);
    }
}
