package com.hp.octane.plugins.jetbrains.teamcity.model.api;

/**
 * Created by lazara on 21/01/2016.
 */
public class AbstractPhase {

    private boolean blocking =false;
    private String name;

    public AbstractPhase(boolean blocking, String name) {
        this.blocking = blocking;
        this.name = name;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public String getName() {
        return name;
    }
}
