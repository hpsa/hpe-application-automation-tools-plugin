package com.hp.octane.plugins.jenkins.workflow;

import com.hp.nga.integrations.dto.causes.CIEventCause;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
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
