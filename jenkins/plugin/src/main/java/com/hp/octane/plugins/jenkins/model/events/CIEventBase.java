package com.hp.octane.plugins.jenkins.model.events;

import com.hp.octane.plugins.jenkins.model.CIServerType;
import com.hp.octane.plugins.jenkins.model.causes.*;
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
	public abstract CIEventType getEventType();

	public CIServerType serverType;
	public String serverURL;
	public String project;
	public CIEventCauseBase cause;

	public CIEventBase(CIServerType serverType, String serverURL, String project, CIEventCauseBase cause) {
		this.serverType = serverType;
		this.serverURL = serverURL;
		this.project = project;
		this.cause = cause;
	}

	public CIEventBase(CIServerType serverType, String serverURL) {
		this.serverType = serverType;
		this.serverURL = serverURL;
	}
}