package com.hp.octane.plugins.jenkins.actions;

import com.hp.octane.plugins.jenkins.configuration.ConfigurationAction;
import com.hp.octane.plugins.jenkins.model.parameters.ParameterType;
import com.hp.octane.plugins.jenkins.model.pipelines.BuildHistory;
import com.hp.octane.plugins.jenkins.model.pipelines.StructureItem;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.scm.SCMData;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterValue;
import hudson.model.BuildAuthorizationToken;
import hudson.model.Cause;
import hudson.model.FileParameterDefinition;
import hudson.model.FileParameterValue;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterValue;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.model.TransientProjectActionFactory;
import hudson.model.User;
import hudson.security.Permission;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 12/08/14
 * Time: 10:46
 * To change this template use File | Settings | File Templates.
 */

@Extension
public class ProjectActions extends TransientProjectActionFactory {
	private static final Logger logger = Logger.getLogger(ProjectActions.class.getName());

	static final public class OctaneProjectActions implements ProminentProjectAction {
		private static final Logger logger = Logger.getLogger(OctaneProjectActions.class.getName());
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

				AbstractBuild abstractBuild = (AbstractBuild) result.get(i);
				if (abstractBuild != null) {
					SCMData smData = SCMProcessors
							.getAppropriate(abstractBuild.getProject().getScm().getClass().getName())
							.getSCMChanges(abstractBuild);
					Set<User> users = abstractBuild.getCulprits();
					buildHistory.addBuild(abstractBuild.getResult().toString(), String.valueOf(abstractBuild.getNumber()), abstractBuild.getTimestampString(), String.valueOf(abstractBuild.getStartTimeInMillis()), String.valueOf(abstractBuild.getDuration()), smData, users);
				}
			}
			SCMData smData = null;
			AbstractBuild lastSuccessfulBuild = (AbstractBuild) project.getLastSuccessfulBuild();
			if (lastSuccessfulBuild != null) {
				smData = SCMProcessors
						.getAppropriate(lastSuccessfulBuild.getProject().getScm().getClass().getName())
						.getSCMChanges(lastSuccessfulBuild);
				Set<User> users = lastSuccessfulBuild.getCulprits();
				buildHistory.addLastSuccesfullBuild(lastSuccessfulBuild.getResult().toString(), String.valueOf(lastSuccessfulBuild.getNumber()), lastSuccessfulBuild.getTimestampString(), String.valueOf(lastSuccessfulBuild.getStartTimeInMillis()), String.valueOf(lastSuccessfulBuild.getDuration()), smData, users);
			}
			AbstractBuild lastBuild = (AbstractBuild) project.getLastBuild();
			if (lastBuild != null) {
				smData = SCMProcessors
						.getAppropriate(lastBuild.getProject().getScm().getClass().getName())
						.getSCMChanges(lastBuild);
				Set<User> users = lastBuild.getCulprits();
				if (lastBuild.getResult() == null) {
					buildHistory.addLastBuild("building", String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), smData, users);
				} else {
					buildHistory.addLastBuild(lastBuild.getResult().toString(), String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), smData, users);
				}

			}
			res.serveExposedBean(req, buildHistory, Flavor.JSON);
		}

		//  TODO:   limit to POST only?
		public void doRun(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			//SecurityContext context = ACL.impersonate(User.get("gullerya").impersonate());

			BuildAuthorizationToken.checkPermission((Job) project, project.getAuthToken(), req, res);

			int delay = project.getQuietPeriod();
			ParametersAction parametersAction = new ParametersAction();

			String bodyText = "";
			byte[] buffer = new byte[1024];
			while (req.getInputStream().read(buffer, 0, buffer.length) > 0)
				bodyText += new String(buffer);

			if (!bodyText.isEmpty()) {
				JSONObject bodyJSON = JSONObject.fromObject(bodyText);

				//  delay
				if (bodyJSON.has("delay") && bodyJSON.get("delay") != null) {
					delay = bodyJSON.getInt("delay");
				}

				//  parameters
				if (bodyJSON.has("parameters") && bodyJSON.get("parameters") != null) {
					JSONArray paramsJSON = bodyJSON.getJSONArray("parameters");
					parametersAction = new ParametersAction(createParameters(paramsJSON));
				}
			}
			boolean success = project.scheduleBuild(delay, new Cause.RemoteCause(req.getRemoteHost(), "octane driven execution"), parametersAction);
			if (success) {
				res.setStatus(201);
			} else {
				res.setStatus(500);
			}

			//ACL.impersonate(context.getAuthentication());
		}

		private List<ParameterValue> createParameters(JSONArray paramsJSON) {
			List<ParameterValue> result = new ArrayList<ParameterValue>();
			ParameterValue tmpValue;
			ParametersDefinitionProperty paramsDefs = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
			for (ParameterDefinition paramDef : paramsDefs.getParameterDefinitions()) {
				for (int i = 0; i < paramsJSON.size(); i++) {
					JSONObject paramJSON = paramsJSON.getJSONObject(i);
					if (paramJSON.has("name") && paramJSON.get("name") != null && paramJSON.get("name").equals(paramDef.getName())) {
						tmpValue = null;
						switch (ParameterType.getByValue(paramJSON.getString("type"))) {
							case FILE:
								try {
									FileItemFactory fif = new DiskFileItemFactory();
									FileItem fi = fif.createItem(paramJSON.getString("name"), "text/plain", false, paramJSON.getString("file"));
									fi.getOutputStream().write(paramJSON.getString("value").getBytes());
									tmpValue = new FileParameterValue(paramJSON.getString("name"), fi);
								} catch (IOException ioe) {
									logger.warning("failed to process file parameter");
								}
								break;
							case NUMBER:
								tmpValue = new StringParameterValue(paramJSON.getString("name"), paramJSON.get("value").toString());
								break;
							case STRING:
								tmpValue = new StringParameterValue(paramJSON.getString("name"), paramJSON.getString("value"));
								break;
							case BOOLEAN:
								tmpValue = new BooleanParameterValue(paramJSON.getString("name"), paramJSON.getBoolean("value"));
								break;
							case PASSWORD:
								tmpValue = new PasswordParameterValue(paramJSON.getString("name"), paramJSON.getString("value"));
								break;
							default:
								break;
						}
						if (tmpValue != null) result.add(tmpValue);
						break;
					}
				}
			}
			return result;
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
