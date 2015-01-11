package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.model.pipeline.StructureItem;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.ProminentProjectAction;
import hudson.model.TransientProjectActionFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class ProjectActions extends TransientProjectActionFactory {

	static final public class OctaneProjectActions implements ProminentProjectAction {
		private AbstractProject project;

		public OctaneProjectActions(AbstractProject p) {
			project = p;
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

		public void doStructure(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			res.serveExposedBean(req, new StructureItem(project), Flavor.JSON);
		}
	}

	@Override
	public Collection<? extends Action> createFor(AbstractProject project) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new OctaneProjectActions(project));
		return actions;
	}
}
