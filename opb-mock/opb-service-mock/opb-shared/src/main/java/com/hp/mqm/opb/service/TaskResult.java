package com.hp.mqm.opb.service;
/**
 * Representation a task result, including status {IN_PROCESSING, SUCCESS, FAILED} and result content as String. 
 */
public class TaskResult {
    private TaskResultStatus taskResultStatus;
    private String content;
    public TaskResult(TaskResultStatus taskResultStatus, String content) {
        super();
        this.taskResultStatus = taskResultStatus;
        this.content = content;
    }
    /**
     * Get result status
     * @return {@link TaskResultStatus}
     */
    public TaskResultStatus getTaskResultStatus() {
        return taskResultStatus;
    }
    /**
     * Set task result status.
     * 
     * @param taskResultStatus
     */
    public void setTaskResultStatus(TaskResultStatus taskResultStatus) {
        this.taskResultStatus = taskResultStatus;
    }
    /**
     * Get result content by string.
     * @return content
     */
    public String getContent() {
        return content;
    }
    /**
     * Set result content.
     *  
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }
}
