package com.hp.octane.plugins.jenkins.notifications;

import com.hp.octane.plugins.jenkins.model.events.CIEventBase;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gullery on 15/02/2015.
 */
@ExportedBean
public class EventsList {
	private static final String serverType = "Jenkins";
	private static final String serverURL;

	static {
		String tmpURL = Jenkins.getInstance().getRootUrl();
		if (tmpURL != null && tmpURL.endsWith("/")) tmpURL = tmpURL.substring(0, tmpURL.length() - 1);
		serverURL = tmpURL;
	}

	private final List<CIEventBase> events = new ArrayList<CIEventBase>();

	@Exported
	public String serverType() {
		return serverType;
	}

	@Exported
	public String serverURL() {
		return serverURL;
	}

	@Exported(inline = true)
	public List<CIEventBase> getEvents() {
		return events;
	}
}
