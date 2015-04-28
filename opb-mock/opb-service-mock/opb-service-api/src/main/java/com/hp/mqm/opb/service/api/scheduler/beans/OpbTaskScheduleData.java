package com.hp.mqm.opb.service.api.scheduler.beans;

import java.util.Map;

/**
 * This interface represents task schedule data.
 * 
 * @author avrahame
 */
public interface OpbTaskScheduleData {

	/**
	 * Get task schedule id.
	 * 
	 * @return the task schedule id
	 */
	Integer getId();

	/**
	 * Get the task type.
	 * 
	 * @return The task type.
	 */
	String getTaskType();

	/**
	 * Get the task unique context.
	 * 
	 * @return The task unique context.
	 */
	String getUniqueContext();

	/**
	 * Get the task context parameters.
	 * 
	 * @return The task context parameters.
	 */
	Map<String, String> getContextParams();

	/**
	 * Set the task context parameters.
	 * 
	 */
	void setContextParams(Map<String, String> contextParams);

	/**
	 * Get the task schedule interval in seconds..
	 * 
	 * @return The task schedule interval.
	 */
	int getScheduleInterval();

	/**
	 * Set the task type.
	 * 
	 */
	void setScheuleInterval(int intervalSeconds);

	/**
	 * Get the task schedule enabled state.
	 * 
	 * @return The task schedule enabled state.
	 */
	boolean isEnabled();

	/**
	 * Set the task schedule enabled state.
	 * 
	 */
	void setEnabled(boolean enabled);

    /**
     * Gets the product group id
     *
     * @return product group id
     */
    Integer getProductGroupId();

}
