package com.hp.octane.plugins.jenkins;

import com.hp.octane.integrations.api.CIPluginServices;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.parameters.CIParameterType;
import com.hp.octane.integrations.dto.pipelines.BuildHistory;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.scm.SCMData;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.tests.TestsResult;
import com.hp.octane.integrations.exceptions.ConfigurationException;
import com.hp.octane.integrations.exceptions.PermissionException;
import com.hp.octane.plugins.jenkins.configuration.ServerConfiguration;
import com.hp.octane.plugins.jenkins.model.ModelFactory;
import com.hp.octane.plugins.jenkins.model.processors.parameters.ParameterProcessors;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessor;
import com.hp.octane.plugins.jenkins.model.processors.scm.SCMProcessors;
import com.hp.octane.plugins.jenkins.model.processors.builders.WorkFlowJobProcessor;
import hudson.ProxyConfiguration;
import hudson.model.*;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.context.SecurityContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by gullery on 21/01/2016.
 * <p/>
 * Jenkins CI Server oriented extension of CI Data Provider
 */

public class CIJenkinsServicesImpl implements CIPluginServices {
	private static final Logger logger = Logger.getLogger(CIJenkinsServicesImpl.class.getName());
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public CIServerInfo getServerInfo() {
		CIServerInfo result = dtoFactory.newDTO(CIServerInfo.class);
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
	public CIPluginInfo getPluginInfo() {
		CIPluginInfo result = dtoFactory.newDTO(CIPluginInfo.class);
		result.setVersion(Jenkins.getInstance().getPlugin(OctanePlugin.class).getWrapper().getVersion());
		return result;
	}

	@Override
	public File getAllowedOctaneStorage() {
		return new File(Jenkins.getInstance().getRootDir(), "userContent" + File.separator + "nga");
	}

	@Override
	public OctaneConfiguration getOctaneConfiguration() {
		OctaneConfiguration result = null;
		ServerConfiguration serverConfiguration = Jenkins.getInstance().getPlugin(OctanePlugin.class).getServerConfiguration();
		if (serverConfiguration.location != null && !serverConfiguration.location.isEmpty() &&
				serverConfiguration.sharedSpace != null && !serverConfiguration.sharedSpace.isEmpty()) {
			result = dtoFactory.newDTO(OctaneConfiguration.class)
					.setUrl(serverConfiguration.location)
					.setSharedSpace(serverConfiguration.sharedSpace)
					.setApiKey(serverConfiguration.username)
					.setSecret(serverConfiguration.password);
		}
		return result;
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration(String targetHost) {
		CIProxyConfiguration result = null;
		ProxyConfiguration proxy = Jenkins.getInstance().proxy;
		if (proxy != null) {
			result = dtoFactory.newDTO(CIProxyConfiguration.class)
					.setHost(proxy.name)
					.setPort(proxy.port)
					.setUsername(proxy.getUserName())
					.setPassword(proxy.getPassword());
		}
		return result;
	}

	@Override
	public CIJobsList getJobsList(boolean includeParameters) {
		SecurityContext securityContext = startImpersonation();
		CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
		PipelineNode tmpConfig;
		TopLevelItem tmpItem;
		List<PipelineNode> list = new ArrayList<PipelineNode>();
		try {
			boolean hasReadPermission = Jenkins.getInstance().hasPermission(Item.READ);
			if (!hasReadPermission) {
				stopImpersonation(securityContext);
				throw new PermissionException(403);
			}
			List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
			for (String name : itemNames) {
				tmpItem = Jenkins.getInstance().getItem(name);
				if (tmpItem instanceof AbstractProject) {
					AbstractProject abstractProject = (AbstractProject) tmpItem;
					tmpConfig = dtoFactory.newDTO(PipelineNode.class)
							.setJobCiId(name)
							.setName(name);
					if (includeParameters) {
						tmpConfig.setParameters(ParameterProcessors.getConfigs(abstractProject));
					}
					list.add(tmpConfig);
				} else if (tmpItem.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
					Job tmpJob = (Job) tmpItem;
					tmpConfig = dtoFactory.newDTO(PipelineNode.class)
							.setJobCiId(name)
							.setName(name);
					if (includeParameters) {
						tmpConfig.setParameters(ParameterProcessors.getConfigs(tmpJob));
					}
					list.add(tmpConfig);
				} else {
					logger.info("item '" + name + "' is not of supported type");
				}

			}
			result.setJobs(list.toArray(new PipelineNode[list.size()]));
			stopImpersonation(securityContext);
		} catch (AccessDeniedException e) {
			stopImpersonation(securityContext);
			throw new PermissionException(403);
		}
		return result;
	}

	@Override
	public PipelineNode getPipeline(String rootJobCiId) {
		PipelineNode result;
		SecurityContext securityContext = startImpersonation();
		boolean hasRead = Jenkins.getInstance().hasPermission(Item.READ);
		if (!hasRead) {
			stopImpersonation(securityContext);
			throw new PermissionException(403);
		}
		Job project = getJobByRefId(rootJobCiId);
		if (project != null) {
			result = ModelFactory.createStructureItem(project);
			stopImpersonation(securityContext);
			return result;
		} else {
			//todo: check error message(s)
			logger.warning("Failed to get project from jobRefId: '" + rootJobCiId + "' check plugin user Job Read/Overall Read permissions / project name");
			stopImpersonation(securityContext);
			throw new ConfigurationException(404);
		}
	}

	private SecurityContext startImpersonation() {
		String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();
		SecurityContext originalContext = null;
		if (user != null && !user.equalsIgnoreCase("")) {
			User jenkinsUser = User.get(user, false);
			if (jenkinsUser != null) {
				originalContext = ACL.impersonate(jenkinsUser.impersonate());
			} else {
				throw new PermissionException(401);
			}
		} else {
			logger.info("No user set to impersonating to. Operations will be done using Anonymous user");
		}
		return originalContext;
	}

	private void stopImpersonation(SecurityContext originalContext) {
		if (originalContext != null) {
			ACL.impersonate(originalContext.getAuthentication());
		} else {
			logger.warning("Could not roll back impersonation, originalContext is null ");
		}
	}

	@Override
	public void runPipeline(String jobCiId, String originalBody) {
		SecurityContext securityContext = startImpersonation();
		Job job = getJobByRefId(jobCiId);
		AbstractProject project = null;
		if (job != null) {
			boolean hasBuildPermission = job.hasPermission(Item.BUILD);
			if (!hasBuildPermission) {
				stopImpersonation(securityContext);
				throw new PermissionException(403);
			}
			if (job instanceof AbstractProject || job.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
				doRunImpl(job, originalBody);
			}
			stopImpersonation(securityContext);
		} else {
			stopImpersonation(securityContext);
			throw new ConfigurationException(404);
		}
	}

	@Override
	public SnapshotNode getSnapshotLatest(String jobCiId, boolean subTree) {
		SecurityContext securityContext = startImpersonation();
		SnapshotNode result = null;
		Job job = getJobByRefId(jobCiId);
		if (job != null) {
			Run run = job.getLastBuild();
			if (run != null) {
				result = ModelFactory.createSnapshotItem(run, subTree);
			}
		}
		stopImpersonation(securityContext);
		return result;
	}

	@Override
	public SnapshotNode getSnapshotByNumber(String jobCiId, String buildCiId, boolean subTree) {
		SecurityContext securityContext = startImpersonation();
		Job job = getJobByRefId(jobCiId);

		Integer buildNumber = null;
		try {
			buildNumber = Integer.parseInt(buildCiId);
		} catch (NumberFormatException nfe) {
			logger.severe("failed to parse build CI ID to build number, " + nfe.getMessage());
		}
		if (job != null && buildNumber != null) {
			Run build = job.getBuildByNumber(buildNumber);
			stopImpersonation(securityContext);
			return ModelFactory.createSnapshotItem(build, subTree);
		}
		stopImpersonation(securityContext);
		return null;
	}

	@Override
	public BuildHistory getHistoryPipeline(String jobCiId, String originalBody) {
		SecurityContext securityContext = startImpersonation();
		BuildHistory buildHistory = dtoFactory.newDTO(BuildHistory.class);
		Job job = getJobByRefId(jobCiId);
		AbstractProject project;
		if (job instanceof AbstractProject) {
			project = (AbstractProject) job;
			SCMData scmData;
			Set<User> users;
			SCMProcessor scmProcessor = SCMProcessors.getAppropriate(project.getScm().getClass().getName());

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
			List<Run> result = project.getLastBuildsOverThreshold(numberOfBuilds, Result.ABORTED); // get last five build with result that better or equal failure
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
			AbstractBuild lastSuccessfulBuild = null;
			AbstractBuild lastProjectBuild = project.getLastBuild();
			if (lastProjectBuild != null) {
				lastSuccessfulBuild = (AbstractBuild) lastProjectBuild.getPreviousSuccessfulBuild();
			}
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
			stopImpersonation(securityContext);
		} else {
			//  TODO: handle workflow later on
		}
		return buildHistory;
	}

	//  TODO: implement
	@Override
	public TestsResult getTestsResult(String jobId, String buildNumber) {
		return null;
	}

	private void doRunImpl(Job job, String originalBody) {
		if (job instanceof AbstractProject) {
			AbstractProject project = (AbstractProject) job;
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

			project.scheduleBuild(delay, new Cause.RemoteCause(getOctaneConfiguration() == null ? "non available URL" : getOctaneConfiguration().getUrl(), "octane driven execution"), parametersAction);
		} else if (job.getClass().getName().equals("org.jenkinsci.plugins.workflow.job.WorkflowJob")) {
			WorkFlowJobProcessor workFlowJobProcessor = new WorkFlowJobProcessor(job);
			workFlowJobProcessor.scheduleBuild(originalBody);

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
						switch (CIParameterType.fromValue(paramJSON.getString("type"))) {
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

	private Job getJobByRefId(String jobRefId) {
		Job result = null;
		if (jobRefId != null) {
			try {
				jobRefId = URLDecoder.decode(jobRefId, "UTF-8");
				TopLevelItem item = getTopLevelItem(jobRefId);
				if (item != null && item instanceof Job) {
					result = (Job) item;
				}
			} catch (UnsupportedEncodingException e) {
				logger.severe("failed to decode job ref ID '" + jobRefId + "'");
			}
		}
		return result;
	}

	private TopLevelItem getTopLevelItem(String jobRefId) {
		TopLevelItem item;
		try {
			item = Jenkins.getInstance().getItem(jobRefId);
		} catch (AccessDeniedException e) {
			String user = Jenkins.getInstance().getPlugin(OctanePlugin.class).getImpersonatedUser();
			if (user != null && !user.isEmpty()) {
				throw new PermissionException(403);
			} else {
				throw new PermissionException(405);
			}
		}
		return item;
	}
}
