package com.hp.octane.plugins.jenkins;

import com.hp.nga.integrations.api.CIPluginServices;
import com.hp.nga.integrations.dto.parameters.ParameterInstance;
import com.hp.nga.integrations.dto.parameters.ParameterType;
import com.hp.nga.integrations.services.configuration.NGAConfiguration;
import com.hp.nga.integrations.dto.general.CIServerTypes;
import com.hp.nga.integrations.dto.general.PluginInfoDTO;
import com.hp.nga.integrations.dto.general.ServerInfoDTO;
import com.hp.nga.integrations.dto.parameters.ParameterConfig;
import com.hp.nga.integrations.dto.pipelines.StructureItem;
import com.hp.nga.integrations.dto.projects.JobsListDTO;
import com.hp.nga.integrations.dto.snapshots.SnapshotItem;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BooleanParameterValue;
import hudson.model.BuildAuthorizationToken;
import hudson.model.Cause;
import hudson.model.FileParameterDefinition;
import hudson.model.FileParameterValue;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterValue;
import hudson.model.StringParameterValue;
import hudson.model.TopLevelItem;
import hudson.model.User;
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
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIPluginServicesImpl implements CIPluginServices {
	private static final Logger logger = Logger.getLogger(CIPluginServicesImpl.class.getName());

	@Override
	public ServerInfoDTO getServerInfo() {
		ServerInfoDTO result = new ServerInfoDTO();
		String serverUrl = Jenkins.getInstance().getRootUrl();
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		result.setType(CIServerTypes.JENKINS);
		result.setVersion(Jenkins.VERSION);
		result.setUrl(serverUrl);
		result.setInstanceId(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentity());
		result.setInstanceIdFrom(Jenkins.getInstance().getPlugin(OctanePlugin.class).getIdentityFrom());
		result.setSendingTime(System.currentTimeMillis());
		return result;
	}

	@Override
	public PluginInfoDTO getPluginInfo() {
		PluginInfoDTO result = new PluginInfoDTO();
		result.setVersion(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion());
		return result;
	}

	@Override
	public NGAConfiguration getNGAConfiguration() {
		NGAConfiguration result = new NGAConfiguration();
		ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
		result.setUrl(serverConfiguration.location);
		result.setSharedSpace(Long.parseLong(serverConfiguration.sharedSpace));
		result.setUsername(serverConfiguration.username);
		result.setPassword(serverConfiguration.password);
		return result;
	}

	@Override
	public JobsListDTO getJobsList(boolean includeParameters) {

		JobsListDTO result = new JobsListDTO();
		JobsListDTO.ProjectConfig tmpConfig;
		AbstractProject tmpProject;
		List<JobsListDTO.ProjectConfig> list = new ArrayList<JobsListDTO.ProjectConfig>();
		List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
		for (String name : itemNames) {
			tmpProject = (AbstractProject) Jenkins.getInstance().getItem(name);
			tmpConfig = new JobsListDTO.ProjectConfig();
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
		result.setJobs(list.toArray(new JobsListDTO.ProjectConfig[list.size()]));
		return result;
	}

	@Override
	public StructureItem getPipeline(String rootCIJobId) {

		try {
			rootCIJobId = URLDecoder.decode(rootCIJobId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(rootCIJobId);
		return ModelFactory.createStructureItem(project);
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
			item = Jenkins.getInstance().getItem(ciJobId);
			if (item != null && item instanceof AbstractProject) {
				project = (AbstractProject) item;
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

		try {
			ciJobId = URLDecoder.decode(ciJobId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		AbstractProject project = (AbstractProject) Jenkins.getInstance().getItem(ciJobId);
		AbstractBuild build = project.getLastBuild();
		return ModelFactory.createSnapshotItem(build, subTree);
	}

	private int doRunImpl(AbstractProject project, String originalBody) {
		int delay = project.getQuietPeriod();
		ParametersAction parametersAction = new ParametersAction();

		if (!originalBody.isEmpty()) {
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
}
