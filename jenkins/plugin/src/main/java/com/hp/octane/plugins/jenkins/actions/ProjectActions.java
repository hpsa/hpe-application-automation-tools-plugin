package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationAction;
import com.hp.octane.plugins.jenkins.model.pipelines.BuildHistory;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import hudson.Extension;
import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        public void doHistory(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {

            BuildHistory buildHistory = new BuildHistory();
            int numberOfBuilds = 5;
            if (req.getParameter("numberOfBuilds") != null) {
                numberOfBuilds = Integer.valueOf(req.getParameter("numberOfBuilds"));
            }
            List<Run> result = project.getLastBuildsOverThreshold(numberOfBuilds, Result.FAILURE); // get last five build with result that better or equal failure
            for (int i = 0; i < result.size(); i++) {
                AbstractBuild abstractBuild =  (AbstractBuild)result.get(i);
                SCMData smData = SCMProcessors
                        .getAppropriate(abstractBuild.getProject().getScm().getClass().getName())
                        .getSCMChanges(abstractBuild);
                buildHistory.addBuild(result.get(i).getResult().toString(), String.valueOf(result.get(i).getNumber()), result.get(i).getTimestampString(),String.valueOf(result.get(i).getStartTimeInMillis()), String.valueOf(result.get(i).getDuration()),smData);
            }
            SCMData smData = null;
            AbstractBuild lastSuccessfulBuild = (AbstractBuild)project.getLastSuccessfulBuild();
            if (lastSuccessfulBuild != null) {
                 smData = SCMProcessors
                        .getAppropriate(lastSuccessfulBuild.getProject().getScm().getClass().getName())
                        .getSCMChanges(lastSuccessfulBuild);
                buildHistory.addLastSuccesfullBuild(lastSuccessfulBuild.getResult().toString(), String.valueOf(lastSuccessfulBuild.getNumber()), lastSuccessfulBuild.getTimestampString(),String.valueOf(lastSuccessfulBuild.getStartTimeInMillis()),  String.valueOf(lastSuccessfulBuild.getDuration()),smData);
            }
            AbstractBuild lastBuild =  (AbstractBuild)project.getLastBuild();
            if (lastBuild != null) {
                 smData = SCMProcessors
                        .getAppropriate(lastBuild.getProject().getScm().getClass().getName())
                        .getSCMChanges(lastBuild);
                buildHistory.addLastBuild(lastBuild.getResult().toString(), String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(),String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()),smData);
            }
            res.serveExposedBean(req, buildHistory, Flavor.JSON);
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
