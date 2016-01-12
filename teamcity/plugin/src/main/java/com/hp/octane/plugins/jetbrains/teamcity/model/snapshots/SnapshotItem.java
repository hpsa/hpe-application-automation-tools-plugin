package com.hp.octane.plugins.jetbrains.teamcity.model.snapshots;

/**
 * Created by lazara on 12/01/2016.
 */
public class SnapshotItem {
    public SnapshotItem() {
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    private String name;
    private String id;
    private Integer number = null;
//    private CIEventCauseBase[] causes = null;
//    private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
//    private SnapshotResult result = SnapshotResult.UNAVAILABLE;
    private Long estimatedDuration = null;
    private Long startTime = null;
    private Long duration = null;
//    private SCMData scmData = null;


}
