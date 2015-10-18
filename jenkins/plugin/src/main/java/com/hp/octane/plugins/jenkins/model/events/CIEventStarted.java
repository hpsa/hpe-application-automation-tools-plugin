package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.api.ParameterInstance;
import com.hp.octane.plugins.jenkins.model.causes.CIEventCauseBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
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
	CIEventType provideEventType() {
		return CIEventType.STARTED;
	}

	@Exported(inline = true)
	public int getNumber() {
		return number;
	}

	@Exported(inline = true)
	public int getSubNumber() {
		return subNumber;
	}

	@Exported(inline = true)
	public long getStartTime() {
		return startTime;
	}

	@Exported(inline = true)
	public long getEstimatedDuration() {
		return estimatedDuration;
	}

	@Exported(inline = true)
	public ParameterInstance[] getParameters() {
		return parameters;
	}


}
