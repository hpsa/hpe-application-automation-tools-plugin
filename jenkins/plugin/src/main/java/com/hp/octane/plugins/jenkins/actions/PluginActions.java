package com.hp.octane.plugins.jenkins.actions;

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 8/10/14
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class PluginActions implements RootAction {

	@ExportedBean
	static final public class PluginInfo {
		private final String version = "1.0.0";
		private final String type = "jenkins";

		@Exported(inline = true)
		public String getType() {
			return type;
		}

		@Exported(inline = true)
		public String getVersion() {
			return version;
		}
	}

	@ExportedBean
	static final public class ProjectsList {
		private String[] items;

		ProjectsList() {
			List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
			items = itemNames.toArray(new String[itemNames.size()]);
		}

		@Exported(inline = true)
		public String[] getJobs() {
			return items;
		}
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "octane";
	}

	public void doAbout(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.serveExposedBean(req, new PluginInfo(), Flavor.JSON);
	}

	public void doJobs(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.serveExposedBean(req, new ProjectsList(), Flavor.JSON);
	}
}
