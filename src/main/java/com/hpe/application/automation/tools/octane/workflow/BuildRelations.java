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

package com.hpe.application.automation.tools.octane.workflow;

import com.hp.octane.integrations.dto.causes.CIEventCause;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by gadiel on 21/06/2016.
 */
public class BuildRelations {
    private static BuildRelations instance;
    private static Map<String,CIEventCause> map;

    private BuildRelations()
    {
        map = new ConcurrentHashMap<String,CIEventCause>();
    }

    public static BuildRelations getInstance()
    {
        if(instance == null)
            instance = new BuildRelations();
        return instance;
    }

    public void removePairByKey(String key)
    {
        if(map.containsKey(key))
        {
            map.remove(key);
        }
    }
    public void addBuildRelation(String projectName, CIEventCause ciEventCause)
    {
        map.put(projectName,ciEventCause);
    }

    public boolean containKey(String key)
    {
        return map.containsKey(key);
    }

    public CIEventCause getValue(String key)
    {
        return (CIEventCause)map.get(key);
    }

    public String print()
    {
        String totalMap="";
        for(String s : map.keySet())
        {
            totalMap = totalMap.concat(s+"_");
        }
        return totalMap;
    }

}
