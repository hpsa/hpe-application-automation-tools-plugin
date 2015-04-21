package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.model.snapshots.SnapshotItem;
import hudson.Extension;
import hudson.model.*;
import jenkins.model.RunAction2;
import jenkins.model.TransientActionFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class BuildActions extends TransientActionFactory<AbstractBuild> {

	static final public class OctaneBuildActions implements RunAction2 {

		AbstractBuild build;

		public OctaneBuildActions(AbstractBuild b) {
			build = b;
		}

		public void onAttached(Run<?, ?> run) {
		}

		public void onLoad(Run<?, ?> run) {
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

		public void doSnapshot(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			String metaonlyParam = req.getParameter("metaonly");
			boolean metaonly = metaonlyParam != null && metaonlyParam.equals("true");
			res.serveExposedBean(req, new SnapshotItem(build, metaonly), Flavor.JSON);
		}
	}

	@Override
	public Class<AbstractBuild> type() {
		return AbstractBuild.class;
	}

	@Override
	@Nonnull
	public Collection<? extends Action> createFor(@Nonnull AbstractBuild build) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new OctaneBuildActions(build));
		return actions;
	}
}