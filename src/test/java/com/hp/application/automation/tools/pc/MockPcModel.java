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

package com.hp.application.automation.tools.pc;

import com.hp.application.automation.tools.model.PcModel;
import com.hp.application.automation.tools.model.PostRunAction;
import com.hp.application.automation.tools.model.SecretContainer;
import com.hp.application.automation.tools.model.SecretContainerTest;

public class MockPcModel extends PcModel {


    public MockPcModel(String pcServerName, String almUserName, String almPassword, String almDomain,
            String almProject, String testId, String testInstanceId, String timeslotDurationHours,
            String timeslotDurationMinutes, PostRunAction postRunAction, boolean vudsMode, String description) {
        super(pcServerName, almUserName, almPassword, almDomain, almProject, testId, testInstanceId, timeslotDurationHours,
            timeslotDurationMinutes, postRunAction, vudsMode, description, false, null);
    }

    @Override
    protected SecretContainer setPassword(String almPassword) {
        
        SecretContainer secretContainer = new SecretContainerTest();
        secretContainer.initialize(almPassword);
        
        return secretContainer;
    }
    
    

}
