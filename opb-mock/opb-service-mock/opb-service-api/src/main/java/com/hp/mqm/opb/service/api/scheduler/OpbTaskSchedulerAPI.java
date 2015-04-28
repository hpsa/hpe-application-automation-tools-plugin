package com.hp.mqm.opb.service.api.scheduler;

import com.hp.mqm.opb.service.api.scheduler.beans.OpbTaskScheduleData;
import com.hp.mqm.opb.service.api.scheduler.beans.TaskScheduleNotFoundException;

/**
 * OPB task scheduler service API.
 * 
 * @author avrahame
 * 
 */
public interface OpbTaskSchedulerAPI {


	void setSchedule(OpbTaskScheduleData schedule);

	OpbTaskScheduleData getTaskSchedule(String taskType, String uniqueContext) throws TaskScheduleNotFoundException;

	void removeSchedule(OpbTaskScheduleData schedule);

	/**
	 * Returns task schedule enabled status.
	 * 
	 * @param taskType
	 *            The task type.
	 * @param uniqueContext
	 *            The unique context.
	 * @return the task schedule enabled status - true if enabled , else false.
	 */
	boolean isTaskScheduleEnabled(String taskType, String uniqueContext);

    /**
     * Removes the task schedules has linkage with a deleted product group.
     *
     * @param productGroupId
     *          The deleted product group id
     */
    void removeScheduleFromProductGroup(int productGroupId);
}
