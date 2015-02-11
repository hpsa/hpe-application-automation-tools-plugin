package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.model.pipeline.ParameterConfig;
import com.sun.xml.internal.ws.api.ha.StickyFeature;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
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
	static final public class ProjectConfig {
		private String name;
		private List<ParameterConfig> parameters;

		public void setName(String value) {
			name = value;
		}

		@Exported(inline = true)
		public String getName() {
			return name;
		}

		public List<ParameterConfig> getParameters() {
			return parameters;
		}

		public void setParameters(List<ParameterConfig> parameters) {
			this.parameters = parameters;
		}
	}

	@ExportedBean
	static final public class ProjectsList {
		@Exported(inline = true)
		public ProjectConfig[] getJobs() {
			ProjectConfig tmpConfig;
			AbstractProject tmpProject;
			List<ParameterDefinition> paramDefinitions;
			List<ParameterConfig> parameters;
			List<ProjectConfig> list = new ArrayList<ProjectConfig>();
			List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
			for (String name : itemNames) {
				tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
				tmpConfig = new ProjectConfig();
				tmpConfig.setName(name);
				parameters = new ArrayList<ParameterConfig>();
				if (tmpProject.isParameterized()) {
					paramDefinitions = ((ParametersDefinitionProperty) tmpProject.getProperty(ParametersDefinitionProperty.class)).getParameterDefinitions();
					for (ParameterDefinition pdp : paramDefinitions) {
						parameters.add(new ParameterConfig(pdp));
					}
				}
				tmpConfig.setParameters(parameters);
				list.add(tmpConfig);
			}
			return list.toArray(new ProjectConfig[list.size()]);
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

	public void doConfig(StaplerRequest req, StaplerResponse res) throws IOException {
		if (req.getMethod().toLowerCase().compareTo("post") != 0)
			res.sendError(405, "Configuration MUST be performed by POST requests only.");
		//  process the configuration request
		//  respond accordingly
	}

	public void doJobs(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		res.serveExposedBean(req, new ProjectsList(), Flavor.JSON);
	}
}
