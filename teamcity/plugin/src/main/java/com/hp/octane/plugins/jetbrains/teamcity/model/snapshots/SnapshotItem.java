package com.hp.octane.plugins.jetbrains.teamcity.model.snapshots;

import com.hp.octane.plugins.jetbrains.teamcity.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;

/**
 * Created by lazara on 12/01/2016.
 */
public class SnapshotItem extends StructureItem{
    private String status;

    public SnapshotItem(String name, String id) {
        super(name,id);
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

    private Integer number = null;
//    private SnapshotStatus status = SnapshotStatus.UNAVAILABLE;
//    private SnapshotResult result = SnapshotResult.UNAVAILABLE;
    private Long estimatedDuration = null;
    private Long startTime = null;
    private Long duration = null;
    private CIEventCauseBase[] causes = null;

    public CIEventCauseBase[] getCauses() {
        return causes;
    }

    public void setCauses(CIEventCauseBase[] causes) {
        this.causes = causes;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
//    private SCMData scmData = null;


}
