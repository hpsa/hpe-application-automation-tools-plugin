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

import java.util.Arrays;
import java.util.List;

/**
 * Class for (de)serialization of collection of list_node entities in Octane
 */
@SuppressWarnings({"squid:S2699", "squid:S3658", "squid:S2259", "squid:S1872", "squid:S2925", "squid:S109"})
public class ListNodeEntityCollection {

    private List<ListNodeEntity> data;

    public static ListNodeEntityCollection create(ListNodeEntity item) {
        ListNodeEntityCollection coll = new ListNodeEntityCollection();
        coll.setData(Arrays.asList(item));
        return coll;
    }

    public List<ListNodeEntity> getData() {
        return data;
    }

    public void setData(List<ListNodeEntity> data) {
        this.data = data;
    }
}
