package com.hp.octane.plugins.jenkins.model.events;

import com.hp.nga.integrations.dto.causes.CIEventCauseBase;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */

@ExportedBean
public abstract class CIEventBase {
	abstract CIEventType provideEventType();

	private String project;
	private CIEventCauseBase[] causes;

	public CIEventBase(String project, CIEventCauseBase[] causes) {
		this.project = project;
		this.causes = causes;
	}

	@Exported(inline = true)
	public String getEventType() {
		return provideEventType().toString();
	}

	@Exported(inline = true)
	public String getProject() {
		return project;
	}

	@Exported(inline = true)
	public CIEventCauseBase[] getCauses() {
		return causes;
	}
}