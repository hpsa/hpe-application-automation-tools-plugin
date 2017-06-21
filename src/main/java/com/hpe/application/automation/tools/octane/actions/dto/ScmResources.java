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

package com.hpe.application.automation.tools.octane.actions.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This file represents collection of scm resources for sending to Octane
 */
public class ScmResources {

    private List<ScmResourceFile> data = new ArrayList<>();

    public static ScmResources createWithItems(Collection<ScmResourceFile> resources) {
        ScmResources result = new ScmResources();
        result.setData(new ArrayList<>(resources));
        return result;
    }

    public List<ScmResourceFile> getData() {
        return data;
    }

    public void setData(List<ScmResourceFile> data) {
        this.data = data;
    }
}
