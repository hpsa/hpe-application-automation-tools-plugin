package com.hp.octane.plugins.jetbrains.teamcity.model.snapshots;

import com.hp.nga.integrations.dto.snapshots.SnapshotResult;
import com.hp.nga.integrations.dto.snapshots.SnapshotStatus;
import com.hp.octane.plugins.jetbrains.teamcity.model.causes.CIEventCauseBase;
import com.hp.octane.plugins.jetbrains.teamcity.model.pipeline.StructureItem;

/**
 * Created by lazara on 12/01/2016.
 */
public class SnapshotItem extends StructureItem{

    public SnapshotStatus getStatus() {
        return status;
    }

    public void setStatus(SnapshotStatus status) {
        this.status = status;
    }

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

    public CIEventCauseBase[] getCauses() {
        return causes;
    }

    public void setCauses(CIEventCauseBase[] causes) {
        this.causes = causes;
    }

    public void setResult(SnapshotResult result) {
        this.result = result;
    }

    public SnapshotResult getResult() {
        return result;
    }


    public String getScmData() {
        return scmData;
    }

    public void setScmData(String scmData) {
        this.scmData = scmData;
    }

    private String scmData = null;
    private Integer number = null;
    private Long estimatedDuration = null;
    private Long startTime = null;
    private Long duration = null;
    private CIEventCauseBase[] causes = null;
    private SnapshotResult result;
    private SnapshotStatus status;


}
