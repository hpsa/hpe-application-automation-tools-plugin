package com.hp.octane.plugins.common.bridge.tasks;

/**
 * Created by linsha on 07/01/2016.
 */
public interface CITaskService {

    String getProjects(boolean withParameters);
    String getStatus();
    String getStructure(String id);
    String getSnapshot(String id);
}
