package com.hp.mqm.opb.service.api.callback;

public class OpbResultCallbackStatus {

    private Integer taskId;
    private String status;
    private boolean isSuccess;
    private String result;

    public OpbResultCallbackStatus(Integer taskId, String status, boolean isSuccess, String result) {
        this.taskId = taskId;
        this.status = status;
        this.isSuccess = isSuccess;
        this.result = result;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public String getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "OpbResultCallbackStatus: " +
                "\ntaskId = " + taskId +
                "\nstatus = " + status +
                "\nisSuccess = " + isSuccess +
                "\nresult = " + result;
    }
}

