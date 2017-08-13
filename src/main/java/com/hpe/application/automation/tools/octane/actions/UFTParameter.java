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

package com.hpe.application.automation.tools.octane.actions;

/**
 * Created by kashbi on 22/09/2016.
 */
public class UFTParameter {
    private String argName;
    private int argDirection;
    private String argDefaultValue;
    private String argType;
    private int argIsExternal;

    public UFTParameter(){}

    public String getArgName() {
        return argName;
    }

    public void setArgName(String argName) {
        this.argName = argName;
    }

    public int getArgDirection() {
        return argDirection;
    }

    public void setArgDirection(int argDirection) {
        this.argDirection = argDirection;
    }

    public String getArgDefaultValue() {
        return argDefaultValue;
    }

    public void setArgDefaultValue(String argDefaultValue) {
        this.argDefaultValue = argDefaultValue;
    }

    public String getArgType() {
        return argType;
    }

    public void setArgType(String argType) {
        this.argType = argType;
    }

    public int getArgIsExternal() {
        return argIsExternal;
    }

    public void setArgIsExternal(int argIsExternal) {
        this.argIsExternal = argIsExternal;
    }
}
