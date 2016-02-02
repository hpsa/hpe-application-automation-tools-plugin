package com.hp.octane.plugins.jenkins.actions;

import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import hudson.Extension;
import hudson.model.*;
import hudson.security.ACL;
import hudson.security.AccessDeniedException2;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
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

		public void doHistory(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			SCMData scmData;
			Set<User> users;
			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(project.getScm().getClass().getName());
			BuildHistory buildHistory = new BuildHistory();
			int numberOfBuilds = 5;
			if (req.getParameter("numberOfBuilds") != null) {
				numberOfBuilds = Integer.valueOf(req.getParameter("numberOfBuilds"));
			}
			List<Run> result = project.getLastBuildsOverThreshold(numberOfBuilds, Result.FAILURE); // get last five build with result that better or equal failure
			for (int i = 0; i < result.size(); i++) {
				AbstractBuild build = (AbstractBuild) result.get(i);
				scmData = null;
				users = null;
				if (build != null) {
					if (scmProcessor != null) {
						scmData = scmProcessor.getSCMData(build);
						users = build.getCulprits();
					}

					buildHistory.addBuild(build.getResult().toString(), String.valueOf(build.getNumber()), build.getTimestampString(), String.valueOf(build.getStartTimeInMillis()), String.valueOf(build.getDuration()), scmData, ModelFactory.createScmUsersList(users));
				}
			}
			AbstractBuild lastSuccessfulBuild = (AbstractBuild) project.getLastSuccessfulBuild();
			if (lastSuccessfulBuild != null) {
				scmData = null;
				users = null;
				if (scmProcessor != null) {
					scmData = scmProcessor.getSCMData(lastSuccessfulBuild);
					users = lastSuccessfulBuild.getCulprits();
				}
				buildHistory.addLastSuccesfullBuild(lastSuccessfulBuild.getResult().toString(), String.valueOf(lastSuccessfulBuild.getNumber()), lastSuccessfulBuild.getTimestampString(), String.valueOf(lastSuccessfulBuild.getStartTimeInMillis()), String.valueOf(lastSuccessfulBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
			}
			AbstractBuild lastBuild = project.getLastBuild();
			if (lastBuild != null) {
				scmData = null;
				users = null;
				if (scmProcessor != null) {
					scmData = scmProcessor.getSCMData(lastBuild);
					users = lastBuild.getCulprits();
				}

				if (lastBuild.getResult() == null) {
					buildHistory.addLastBuild("building", String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
				} else {
					buildHistory.addLastBuild(lastBuild.getResult().toString(), String.valueOf(lastBuild.getNumber()), lastBuild.getTimestampString(), String.valueOf(lastBuild.getStartTimeInMillis()), String.valueOf(lastBuild.getDuration()), scmData, ModelFactory.createScmUsersList(users));
				}
			}
			res.serveExposedBean(req, buildHistory, Flavor.JSON);
		}

		//  TODO:   limit to POST only?
		public void doRun(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
			SecurityContext context = null;
			String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();

			if (user != null && !user.isEmpty()) {
				User jenkinsUser = null;
				try {
					jenkinsUser = User.get(user, false);
					if (jenkinsUser == null) {
						logger.severe("Failed to load user details: " + user);
						res.setStatus(401);
						return;
					}
				} catch (Exception e) {
					logger.severe("Failed to load user details: " + user);
					res.setStatus(500);
					return;
				}
				try {
					context = ACL.impersonate(jenkinsUser.impersonate());
					logger.severe("Failed to impersonate user: " + user + "(UsernameNotFoundException)");
				} catch (UsernameNotFoundException e) {
					res.setStatus(402);
					return;
				}
			}

			try {
				BuildAuthorizationToken.checkPermission((Job) project, project.getAuthToken(), req, res);
			}
			catch(AccessDeniedException2 accessDeniedException){
				logger.severe(accessDeniedException.getMessage());
				if (user != null && !user.isEmpty()){
					res.setStatus(403);
				}else{
					res.setStatus(405);
				}

				return;
			}
			doRunImpl(req, res, context);
		}

		private void doRunImpl(StaplerRequest req, StaplerResponse res, SecurityContext context) throws IOException {
			int delay = project.getQuietPeriod();
			ParametersAction parametersAction = new ParametersAction();

			String bodyText = "";
			byte[] buffer = new byte[1024];
			int readLen;
			while ((readLen = req.getInputStream().read(buffer)) > 0) {
				bodyText += new String(buffer, 0, readLen);
			}

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
			if (context != null) {
				ACL.impersonate(context.getAuthentication());
			}
		}

		private List<ParameterValue> createParameters(JSONArray paramsJSON) {
			List<ParameterValue> result = new ArrayList<ParameterValue>();
			boolean parameterHandled;
			ParameterValue tmpValue;
			ParametersDefinitionProperty paramsDefProperty = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
			if (paramsDefProperty != null) {
				for (ParameterDefinition paramDef : paramsDefProperty.getParameterDefinitions()) {
					parameterHandled = false;
					for (int i = 0; i < paramsJSON.size(); i++) {
						JSONObject paramJSON = paramsJSON.getJSONObject(i);
						if (paramJSON.has("name") && paramJSON.get("name") != null && paramJSON.get("name").equals(paramDef.getName())) {
							tmpValue = null;
							switch (ParameterType.fromValue(paramJSON.getString("type"))) {
								case FILE:
									try {
										FileItemFactory fif = new DiskFileItemFactory();
										FileItem fi = fif.createItem(paramJSON.getString("name"), "text/plain", false, paramJSON.getString("file"));
										fi.getOutputStream().write(DatatypeConverter.parseBase64Binary(paramJSON.getString("value")));
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
							if (tmpValue != null) {
								result.add(tmpValue);
								parameterHandled = true;
							}
							break;
						}
					}
					if (!parameterHandled) {
						if (paramDef instanceof FileParameterDefinition) {
							FileItemFactory fif = new DiskFileItemFactory();
							FileItem fi = fif.createItem(paramDef.getName(), "text/plain", false, "");
							try {
								fi.getOutputStream().write(new byte[0]);
							} catch (IOException ioe) {
								logger.severe("failed to create default value for file parameter '" + paramDef.getName() + "'");
							}
							tmpValue = new FileParameterValue(paramDef.getName(), fi);
							result.add(tmpValue);
						} else {
							result.add(paramDef.getDefaultParameterValue());
						}
					}
				}
			}
			return result;
		}

		private void completeLackParameterWithDefaults() {
			//  TODO:
		}

	}

	@Override
	public Collection<? extends Action> createFor(AbstractProject project) {
		ArrayList<Action> actions = new ArrayList<Action>();
		actions.add(new OctaneProjectActions(project));
		return actions;
	}
}
