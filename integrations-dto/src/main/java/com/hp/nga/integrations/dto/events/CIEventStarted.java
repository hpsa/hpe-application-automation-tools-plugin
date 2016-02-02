package com.hp.nga.integrations.dto.events;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */

public class CIEventStarted extends CIEventQueued {
	private int number;
	private int subNumber;
	private long startTime;
	private long estimatedDuration;
	private ParameterInstance[] parameters;

	public CIEventStarted(String project,int number, int subNumber, long startTime, long estimatedDuration, CIEventCauseBase[] causes, ParameterInstance[] parameters) {
		super(project,causes);
		this.number = number;
        this.subNumber = subNumber;
		this.startTime = startTime;
		this.estimatedDuration = estimatedDuration;
		this.parameters = parameters;
	}

	@Override
	public CIEventType getEventType() {
		return CIEventType.STARTED;
	}

	public int getNumber() {
		return number;
	}

	public int getSubNumber() {
		return subNumber;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEstimatedDuration() {
		return estimatedDuration;
	}

	public ParameterInstance[] getParameters() {
		return parameters;
	}


}
