package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.causes.*;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 09/09/14
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */

//  TODO: support multiple causes

@ExportedBean
public abstract class CIEventBase {
	abstract CIEventType provideEventType();

	private final String serverType = "jenkins";
	private String serverURL;
	private String project;
	private CIEventCauseBase cause;

	public CIEventBase(String serverURL, String project, CIEventCauseBase cause) {
		this.serverURL = serverURL;
		this.project = project;
		this.cause = cause;
	}

	@Exported(inline = true)
	public String getServerType() {
		return serverType;
	}

	@Exported(inline = true)
	public String getServerURL() {
		return serverURL;
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
	public CIEventCauseBase getCause() {
		return cause;
	}
}