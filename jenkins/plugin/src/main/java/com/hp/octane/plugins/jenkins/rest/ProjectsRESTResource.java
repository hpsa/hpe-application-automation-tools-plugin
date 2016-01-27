package com.hp.octane.plugins.jenkins.rest;

import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.octane.plugins.jenkins.OctanePlugin;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import hudson.model.*;
import hudson.security.ACL;
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
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gullery on 22/12/2015.
 * This Resource is the entry point for all the Projects' related REST APIs under 'octane/projects' context
 */

public class ProjectsRESTResource {
	private static final Logger logger = Logger.getLogger(ProjectsRESTResource.class.getName());
	public static final ProjectsRESTResource instance = new ProjectsRESTResource();

	private static final String BUILDS_REST = "builds";
	private static final String RUN_REST = "run";

	private ProjectsRESTResource() {
	}

	public void handle(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		String[] path = req.getRestOfPath().split("/");
		TopLevelItem item = Jenkins.getInstance().getItem(path[1]);
		if (item == null || !(item instanceof AbstractProject)) {
			res.setStatus(404);
			res.getWriter().write("project '" + path[1] + "' not exists");
		} else {
			if (path.length > 2) {
				if (BUILDS_REST.equals(path[2])) {
					BuildsRESTResource.instance.handle((AbstractProject) item, path.length > 3 ? path[3] : null, req, res);
				} else if (RUN_REST.equals(path[2])) {
					executeProject((AbstractProject) item, req, res);
				} else {
					res.setStatus(404);
				}
			} else {
				serveProjectStructure((AbstractProject) item, req, res);
			}
		}
	}

	private void serveProjectStructure(AbstractProject project, StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
		if ("GET".equals(req.getMethod())) {
			res.serveExposedBean(req, ModelFactory.createStructureItem(project)/*new StructureItem(project)*/, Flavor.JSON);
		} else {
			res.setStatus(405);
		}
	}

	private void executeProject(AbstractProject project, StaplerRequest req, StaplerResponse res) throws IOException {
		if ("POST".equals(req.getMethod())) {
			SecurityContext context = null;
			String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();

			if (user != null && !user.isEmpty()) {
				User jenkinsUser;
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
			} catch (Exception e) {
				logger.severe("CheckPermission failed to user: " + user);
				if (user != null && !user.isEmpty()) {
					res.setStatus(403);
				} else {
					res.setStatus(404);
				}
				return;
			}

			doRunImpl(project, req, res, context);
		} else {
			res.setStatus(405);
		}
	}

	private void doRunImpl(AbstractProject project, StaplerRequest req, StaplerResponse res, SecurityContext context) throws IOException {
		int delay = project.getQuietPeriod();
		ParametersAction parametersAction = new ParametersAction();

		String bodyText = RESTUtils.readBody(req);

		if (!bodyText.isEmpty()) {
			JSONObject bodyJSON = JSONObject.fromObject(bodyText);

			//  delay
			if (bodyJSON.has("delay") && bodyJSON.get("delay") != null) {
				delay = bodyJSON.getInt("delay");
			}

			//  parameters
			if (bodyJSON.has("parameters") && bodyJSON.get("parameters") != null) {
				JSONArray paramsJSON = bodyJSON.getJSONArray("parameters");
				parametersAction = new ParametersAction(createParameters(project, paramsJSON));
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

	private List<ParameterValue> createParameters(AbstractProject project, JSONArray paramsJSON) {
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
}
