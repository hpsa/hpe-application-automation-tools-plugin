package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.DTOFactory;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfo;
import com.hp.nga.integrations.dto.general.ServerInfo;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.dto.pipelines.BuildHistory;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.general.JobsList;
import com.hp.nga.integrations.dto.general.JobConfig;
import com.hp.nga.integrations.dto.general.JobConfigImpl;
import com.hp.nga.integrations.dto.scm.SCMData;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.nga.integrations.dto.configuration.NGAConfiguration;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
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

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIPluginServicesImpl implements CIPluginServices {
	private static final Logger logger = Logger.getLogger(CIPluginServicesImpl.class.getName());

	@Override
	public ServerInfo getServerInfo() {
		ServerInfo result = DTOFactory.instance.createDTO(ServerInfo.class);
		String serverUrl = Jenkins.getInstance().getRootUrl();
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		result.setType(CIServerTypes.JENKINS)
				.setVersion(Jenkins.VERSION)
				.setUrl(serverUrl)
				.setInstanceId(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity())
				.setInstanceIdFrom(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom())
				.setSendingTime(System.currentTimeMillis());
		return result;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo result = DTOFactory.instance.createDTO(PluginInfo.class);
		result.setVersion(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion());
		return result;
	}

	@Override
	public NGAConfiguration getNGAConfiguration() {
		NGAConfiguration result = DTOFactory.instance.createDTO(NGAConfiguration.class);
		ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
		result.setUrl(serverConfiguration.location);
		result.setSharedSpace(Long.parseLong(serverConfiguration.sharedSpace));
		result.setClientId(serverConfiguration.username);
		result.setApiKey(serverConfiguration.password);
		return result;
	}

	@Override
	public JobsList getJobsList(boolean includeParameters) {

		JobsList result = DTOFactory.instance.createDTO(JobsList.class);
		JobConfig tmpConfig;
		AbstractProject tmpProject;
		List<JobConfig> list = new ArrayList<JobConfig>();
		List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
		for (String name : itemNames) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
			tmpConfig = new JobConfigImpl();
			tmpConfig.setName(name);
			tmpConfig.setCiId(name);
			if (includeParameters) {
				List<ParameterConfig> tmpList = ParameterProcessors.getConfigs(tmpProject);
				List<com.hp.nga.integrations.dto.parameters.ParameterConfig> configs = new ArrayList<com.hp.nga.integrations.dto.parameters.ParameterConfig>();
				for (ParameterConfig pc : tmpList) {
					configs.add(new com.hp.nga.integrations.dto.parameters.ParameterConfig(
							pc.getType(),
							pc.getName(),
							pc.getDescription(),
							pc.getDefaultValue(),
							pc.getChoices() == null ? null : pc.getChoices()
					));
				}
				tmpConfig.setParameters(configs.toArray(new com.hp.nga.integrations.dto.parameters.ParameterConfig[configs.size()]));
			}
			list.add(tmpConfig);
		}
		result.setJobs(list.toArray(new JobConfig[list.size()]));
		return result;
	}

	@Override
	public StructureItem getPipeline(String rootCIJobId) {

		AbstractProject project = getProjectFromId(rootCIJobId);
		if (project != null) {
			return ModelFactory.createStructureItem(project);
		}
		return null;
	}

	@Override
	public int runPipeline(String ciJobId, String originalBody) {
		int result;
		TopLevelItem item;
		AbstractProject project;
		SecurityContext originalContext = null;
		String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();

		if (user != null && !user.isEmpty()) {
			User jenkinsUser;
			try {
				jenkinsUser = User.get(user, false);
				if (jenkinsUser == null) {
					logger.severe("Failed to load user details: " + user);
					return 401;
				}
			} catch (Exception e) {
				logger.severe("Failed to load user details: " + user);
				return 500;
			}
			try {
				originalContext = ACL.impersonate(jenkinsUser.impersonate());
			} catch (UsernameNotFoundException unfe) {
				logger.severe("Failed to impersonate '" + user + "':" + unfe.getMessage());
				return 402;
			}
		}

		try {
			project = getProjectFromId(ciJobId);
			if (project != null) {
				project.checkPermission(Item.BUILD);
			} else {
				return 404;
			}
		} catch (AccessDeniedException2 accessDeniedException) {
			logger.severe(accessDeniedException.getMessage());
			if (user != null && !user.isEmpty()) {
				return 403;
			} else {
				return 405;
			}
		}
		result = doRunImpl(project, originalBody);
		if (originalContext != null) {
			ACL.impersonate(originalContext.getAuthentication());
		}
		return result;
	}

	@Override
	public SnapshotItem getSnapshotLatest(String ciJobId, boolean subTree) {

		AbstractProject project = getProjectFromId(ciJobId);
		if (project != null) {
			AbstractBuild build = project.getLastBuild();
			return ModelFactory.createSnapshotItem(build, subTree);
		}
		return null;
	}

	@Override
	public BuildHistory getHistoryPipeline(String ciJobId, String originalBody) {
		AbstractProject project = getProjectFromId(ciJobId);
		SCMData scmData;
		Set<User> users;
		SCMProcessor scmProcessor = SCMProcessors.getAppropriate(project.getScm().getClass().getName());
		BuildHistory buildHistory = DTOFactory.instance.createDTO(BuildHistory.class);
		int numberOfBuilds = 5;
//		if (req.getParameter("numberOfBuilds") != null) {
//			numberOfBuilds = Integer.valueOf(req.getParameter("numberOfBuilds"));
//		}
		//TODO : check if it works!!
		if (originalBody != null && !originalBody.isEmpty()) {
			JSONObject bodyJSON = JSONObject.fromObject(originalBody);
			if (bodyJSON.has("numberOfBuilds")) {
				numberOfBuilds = bodyJSON.getInt("numberOfBuilds");
			}
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
		return buildHistory;
	}

	private int extractParameter(String numberOfBuilds, String originalBody) {
		return 0;
	}

	private int doRunImpl(AbstractProject project, String originalBody) {
		int delay = project.getQuietPeriod();
		ParametersAction parametersAction = new ParametersAction();

		if (originalBody != null && !originalBody.isEmpty()) {
			JSONObject bodyJSON = JSONObject.fromObject(originalBody);

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
		boolean success = project.scheduleBuild(delay, new Cause.RemoteCause(getNGAConfiguration().getUrl(), "octane driven execution"), parametersAction);
		if (success) {
			return 201;
		} else {
			return 500;
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

	private AbstractProject getProjectFromId(String projectId) {

		if (projectId != null) {
			try {
				projectId = URLDecoder.decode(projectId, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			TopLevelItem item = Jenkins.getInstance().getItem(projectId);
			if (item != null && item instanceof AbstractProject) {
				AbstractProject project = (AbstractProject) item;
				return project;
			}
		}

		return null;
	}
}
