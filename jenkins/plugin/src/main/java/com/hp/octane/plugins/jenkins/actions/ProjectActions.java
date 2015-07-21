package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationAction;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextImpl;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.ModelBuilder;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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

		public void doRun(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			//SecurityContext context = ACL.impersonate(User.get("gullerya").impersonate());
			if (project.isParameterized()) {
				project.doBuildWithParameters(req, res, null);
			} else {
				project.doBuild(req, res, null);
			}
			//ACL.impersonate(context.getAuthentication());
		}
	}

	@Override
	public Collection<? extends Action> createFor(AbstractProject project) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new OctaneProjectActions(project));
		actions.add(new ConfigurationAction(project));
		return actions;
	}
}
