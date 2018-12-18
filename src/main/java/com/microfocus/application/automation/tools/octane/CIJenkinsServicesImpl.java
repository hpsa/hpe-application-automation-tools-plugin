/*
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * © Copyright 2012-2018 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors (“Micro Focus”) are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane;

import com.hp.octane.integrations.CIPluginServices;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.connectivity.OctaneResponse;
import com.hp.octane.integrations.dto.entities.EntityConstants;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.MultiBranchType;
import com.hp.octane.integrations.dto.executor.CredentialsInfo;
import com.hp.octane.integrations.dto.executor.DiscoveryInfo;
import com.hp.octane.integrations.dto.executor.TestConnectivityInfo;
import com.hp.octane.integrations.dto.executor.TestSuiteExecutionInfo;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIPluginInfo;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.parameters.CIParameter;
import com.hp.octane.integrations.dto.parameters.CIParameters;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.securityscans.SSCProjectConfiguration;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.exceptions.ConfigurationException;
import com.hp.octane.integrations.exceptions.PermissionException;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.configuration.SSCServerConfigUtil;
import com.microfocus.application.automation.tools.octane.executor.ExecutorConnectivityService;
import com.microfocus.application.automation.tools.octane.executor.TestExecutionJobCreatorService;
import com.microfocus.application.automation.tools.octane.executor.UftConstants;
import com.microfocus.application.automation.tools.octane.executor.UftJobCleaner;
import com.microfocus.application.automation.tools.octane.model.ModelFactory;
import com.microfocus.application.automation.tools.octane.model.processors.parameters.ParameterProcessors;
import com.microfocus.application.automation.tools.octane.model.processors.projects.AbstractProjectProcessor;
import com.microfocus.application.automation.tools.octane.model.processors.projects.JobProcessorFactory;
import com.microfocus.application.automation.tools.octane.tests.TestListener;
import hudson.ProxyConfiguration;
import hudson.console.PlainTextConsoleOutputStream;
import hudson.model.*;
import hudson.security.ACL;
import jenkins.branch.OrganizationFolder;
import jenkins.model.Jenkins;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.context.SecurityContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base implementation of SPI(service provider interface) of Octane CI SDK for Jenkins
 */

public class CIJenkinsServicesImpl extends CIPluginServices {
	private static final Logger logger = LogManager.getLogger(CIJenkinsServicesImpl.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Override
	public CIServerInfo getServerInfo() {
		return getJenkinsServerInfo();
	}

	@Override
	public CIPluginInfo getPluginInfo() {
		CIPluginInfo result = dtoFactory.newDTO(CIPluginInfo.class);
		result.setVersion(ConfigurationService.getPluginVersion());
		return result;
	}

	@Override
	public void suspendCIEvents(boolean suspend) {
		OctaneServerSettingsModel model = ConfigurationService.getSettings(getInstanceId());
		model.setSuspend(suspend);
		ConfigurationService.configurePlugin(model);
		logger.info("suspend ci event: " + suspend);
	}

	@Override
	public File getAllowedOctaneStorage() {
		return new File(Jenkins.getInstance().getRootDir(), "userContent");
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration(URL targetUrl) {
		CIProxyConfiguration result = null;
		ProxyConfiguration proxy = Jenkins.getInstance().proxy;
		if (proxy != null) {
			boolean noProxyHost = false;
			for (Pattern pattern : proxy.getNoProxyHostPatterns()) {
				if (pattern.matcher(targetUrl.getHost()).matches()) {
					noProxyHost = true;
					break;
				}
			}
			if (!noProxyHost) {
				result = dtoFactory.newDTO(CIProxyConfiguration.class)
						.setHost(proxy.name)
						.setPort(proxy.port)
						.setUsername(proxy.getUserName())
						.setPassword(proxy.getPassword());
			}
		}
		return result;
	}

	@Override
	public CIJobsList getJobsList(boolean includeParameters) {
		SecurityContext securityContext = startImpersonation();
		CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
		PipelineNode tmpConfig;
		TopLevelItem tmpItem;
		List<PipelineNode> list = new ArrayList<>();
		try {
			boolean hasReadPermission = Jenkins.getInstance().hasPermission(Item.READ);
			if (!hasReadPermission) {
				stopImpersonation(securityContext);
				throw new PermissionException(403);
			}
			List<String> itemNames = (List<String>) Jenkins.getInstance().getTopLevelItemNames();
			for (String name : itemNames) {
				tmpItem = Jenkins.getInstance().getItem(name);

				if (tmpItem == null) {
					continue;
				}

				String jobClassName = tmpItem.getClass().getName();
				try {
					if (tmpItem instanceof AbstractProject) {
						AbstractProject abstractProject = (AbstractProject) tmpItem;
						if (abstractProject.isDisabled()) {
							continue;
						}
						tmpConfig = createPipelineNode(name, abstractProject, includeParameters);
						list.add(tmpConfig);
					} else if (jobClassName.equals(JobProcessorFactory.WORKFLOW_JOB_NAME)) {
						tmpConfig = createPipelineNode(name, (Job) tmpItem, includeParameters);
						list.add(tmpConfig);
					} else if (jobClassName.equals(JobProcessorFactory.FOLDER_JOB_NAME)) {
						for (Job tmpJob : tmpItem.getAllJobs()) {
							tmpConfig = createPipelineNode(tmpJob.getName(), tmpJob, includeParameters);
							list.add(tmpConfig);
						}
					} else if (jobClassName.equals(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
						tmpConfig = createPipelineNodeFromJobName(name);
						list.add(tmpConfig);
					} else if (jobClassName.equals(JobProcessorFactory.GITHUB_ORGANIZATION_FOLDER)) {
						for (Item item : ((OrganizationFolder) tmpItem).getItems()) {
							tmpConfig = createPipelineNodeFromJobNameAndFolder(item.getDisplayName(), name);
							list.add(tmpConfig);
						}
					} else {
						logger.info(String.format("getJobsList : Item '%s' of type '%s' is not supported", name, jobClassName));
					}
				} catch (Throwable e) {
					logger.error("getJobsList : Failed to add job '" + name + "' to JobList  : " + e.getClass().getCanonicalName() + " - " + e.getMessage(), e);
				}

			}
			result.setJobs(list.toArray(new PipelineNode[0]));
		} catch (AccessDeniedException e) {
			throw new PermissionException(403);
		} finally {
			stopImpersonation(securityContext);
		}
		return result;
	}

	@Override
	public PipelineNode getPipeline(String rootJobCiId) {
		SecurityContext securityContext = startImpersonation();
		try {
			PipelineNode result;
			boolean hasRead = Jenkins.getInstance().hasPermission(Item.READ);
			if (!hasRead) {
				throw new PermissionException(403);
			}

			TopLevelItem tli = getTopLevelItem(rootJobCiId);
			if (tli != null && tli.getClass().getName().equals(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
				result = createPipelineNodeFromJobName(rootJobCiId);
				result.setMultiBranchType(MultiBranchType.MULTI_BRANCH_PARENT);
			} else {
				Job project = getJobByRefId(rootJobCiId);
				if (project != null) {
					result = ModelFactory.createStructureItem(project);
				} else {
					Item item = getItemByRefId(rootJobCiId);
					//todo: check error message(s)
					if (item != null && item.getClass().getName().equals(JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME)) {
						result = createPipelineNodeFromJobName(rootJobCiId);
						result.setMultiBranchType(MultiBranchType.MULTI_BRANCH_PARENT);
					} else {
						logger.warn("Failed to get project from jobRefId: '" + rootJobCiId + "' check plugin user Job Read/Overall Read permissions / project name");
						throw new ConfigurationException(404);
					}
				}
			}
			return result;
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public void runPipeline(String jobCiId, String originalBody) {
		SecurityContext securityContext = startImpersonation();
		try {
			Job job = getJobByRefId(jobCiId);
			//create UFT test runner job on the fly if missing
			if (job == null && jobCiId != null && jobCiId.startsWith(UftConstants.EXECUTION_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS)) {
				job = createExecutorByJobName(jobCiId);
			}
			if (job != null) {
				boolean hasBuildPermission = job.hasPermission(Item.BUILD);
				if (!hasBuildPermission) {
					stopImpersonation(securityContext);
					throw new PermissionException(403);
				}
				if (job instanceof AbstractProject || job.getClass().getName().equals(JobProcessorFactory.WORKFLOW_JOB_NAME)) {
					doRunImpl(job, originalBody);
				}
			} else {
				throw new ConfigurationException(404);
			}
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public SnapshotNode getSnapshotLatest(String jobCiId, boolean subTree) {
		SecurityContext securityContext = startImpersonation();
		try {
			SnapshotNode result = null;
			Job job = getJobByRefId(jobCiId);
			if (job != null) {
				Run run = job.getLastBuild();
				if (run != null) {
					result = ModelFactory.createSnapshotItem(run, subTree);
				}
			}
			return result;
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public SnapshotNode getSnapshotByNumber(String jobId, String buildId, boolean subTree) {
		SecurityContext securityContext = startImpersonation();
		try {
			SnapshotNode result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run != null) {
				result = ModelFactory.createSnapshotItem(run, subTree);
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' not found");
			}
			return result;
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public InputStream getTestsResult(String jobId, String buildId) {
		SecurityContext originalContext = startImpersonation();
		try {
			InputStream result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run != null) {
				try {
					result = new FileInputStream(run.getRootDir() + File.separator + TestListener.TEST_RESULT_FILE);
				} catch (Exception fnfe) {
					logger.error("'" + TestListener.TEST_RESULT_FILE + "' file no longer exists, test results of '" + jobId + " #" + buildId + "' won't be pushed to Octane", fnfe);
				}
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' not found");
			}
			return result;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public InputStream getBuildLog(String jobId, String buildId) {
		SecurityContext originalContext = startImpersonation();
		try {
			InputStream result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run != null) {
				result = getOctaneLogFile(run);
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' not found");
			}
			return result;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public InputStream getCoverageReport(String jobId, String buildId, String reportFileName) {
		SecurityContext originalContext = startImpersonation();
		try {
			InputStream result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run != null) {
				File coverageReport = new File(run.getRootDir(), reportFileName);
				if (coverageReport.exists()) {
					try {
						result = new FileInputStream(coverageReport);
					} catch (FileNotFoundException fnfe) {
						logger.warn("file not found for '" + reportFileName + "' although just verified its existence, concurrency?");
					}
				}
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' not found");
			}
			return result;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public SSCProjectConfiguration getSSCProjectConfiguration(String jobId, String buildId) {
		SecurityContext originalContext = startImpersonation();
		try {
			SSCProjectConfiguration result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run instanceof AbstractBuild) {
				String sscServerUrl = SSCServerConfigUtil.getSSCServer();
				String sscAuthToken = ConfigurationService.getSettings(getInstanceId()).getSscBaseToken();
				SSCServerConfigUtil.SSCProjectVersionPair projectVersionPair = SSCServerConfigUtil.getProjectConfigurationFromBuild((AbstractBuild) run);
				if (sscServerUrl != null && !sscServerUrl.isEmpty() && projectVersionPair != null) {
					result = dtoFactory.newDTO(SSCProjectConfiguration.class)
							.setSSCUrl(sscServerUrl)
							.setSSCBaseAuthToken(sscAuthToken)
							.setProjectName(projectVersionPair.project)
							.setProjectVersion(projectVersionPair.version);
				}
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' (of specific type AbstractBuild) not found");
			}
			return result;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public void runTestDiscovery(DiscoveryInfo discoveryInfo) {
		SecurityContext securityContext = startImpersonation();
		try {
			TestExecutionJobCreatorService.runTestDiscovery(discoveryInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public PipelineNode createExecutor(DiscoveryInfo discoveryInfo) {
		if (EntityConstants.Executors.UFT_TEST_RUNNER_SUBTYPE_ENTITY_NAME.equals(discoveryInfo.getExecutorType())) {
			SecurityContext securityContext = startImpersonation();
			try {
				Job project = TestExecutionJobCreatorService.createExecutor(discoveryInfo);
				PipelineNode result = ModelFactory.createStructureItem(project);
				return result;
			} finally {
				stopImpersonation(securityContext);
			}
		} else {
			return null;
		}
	}


	private Job createExecutorByJobName(String uftExecutorJobNameWithTestRunner) {
		SecurityContext securityContext = startImpersonation();
		try {
			Job project = TestExecutionJobCreatorService.createExecutorByJobName(uftExecutorJobNameWithTestRunner);
			return project;
		} catch (Exception e) {
			logger.warn("Failed to create createExecutor by name : " + e.getMessage());
			return null;
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public void runTestSuiteExecution(TestSuiteExecutionInfo suiteExecutionInfo) {
		SecurityContext securityContext = startImpersonation();
		try {
			TestExecutionJobCreatorService.runTestSuiteExecution(suiteExecutionInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public OctaneResponse checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
		SecurityContext securityContext = startImpersonation();
		try {
			return ExecutorConnectivityService.checkRepositoryConnectivity(testConnectivityInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public void deleteExecutor(String id) {
		SecurityContext securityContext = startImpersonation();
		try {
			UftJobCleaner.deleteDiscoveryJobByExecutor(id);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public OctaneResponse upsertCredentials(CredentialsInfo credentialsInfo) {
		SecurityContext securityContext = startImpersonation();
		try {
			return ExecutorConnectivityService.upsertRepositoryCredentials(credentialsInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	private SecurityContext startImpersonation() {
		OctaneServerSettingsModel settings = ConfigurationService.getSettings(getInstanceId());
		if (settings == null) {
			throw new IllegalStateException("failed to retrieve configuration settings by instance ID " + getInstanceId());
		}
		String user = settings.getImpersonatedUser();
		SecurityContext originalContext = null;
		if (user != null && !user.isEmpty()) {
			User jenkinsUser = User.get(user, false, Collections.emptyMap());
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
			ACL.as(originalContext.getAuthentication());
		} else {
			logger.warn("Could not roll back impersonation, originalContext is null ");
		}
	}

	private PipelineNode createPipelineNode(String name, Job job, boolean includeParameters) {
		PipelineNode tmpConfig = dtoFactory.newDTO(PipelineNode.class)
				.setJobCiId(JobProcessorFactory.getFlowProcessor(job).getTranslateJobName())
				.setName(name);
		if (includeParameters) {
			tmpConfig.setParameters(ParameterProcessors.getConfigs(job));
		}
		return tmpConfig;
	}

	private PipelineNode createPipelineNodeFromJobName(String name) {
		return dtoFactory.newDTO(PipelineNode.class)
				.setJobCiId(name)
				.setName(name);
	}

	private PipelineNode createPipelineNodeFromJobNameAndFolder(String name, String folderName) {
		return dtoFactory.newDTO(PipelineNode.class)
				.setJobCiId(folderName + "/" + name)
				.setName(folderName + "/" + name);
	}

	private InputStream getOctaneLogFile(Run run) {
		InputStream result = null;
		String octaneLogFilePath = run.getLogFile().getParent() + File.separator + "octane_log";
		File logFile = new File(octaneLogFilePath);
		if (!logFile.exists()) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(logFile);
			     InputStream logStream = run.getLogInputStream();
			     PlainTextConsoleOutputStream out = new PlainTextConsoleOutputStream(fileOutputStream)) {
				IOUtils.copy(logStream, out);
				out.flush();
			} catch (IOException ioe) {
				logger.error("failed to transfer native log to Octane's one for " + run);
			}
		}
		try {
			result = new FileInputStream(octaneLogFilePath);
		} catch (IOException ioe) {
			logger.error("failed to obtain log for " + run);
		}
		return result;
	}

	private Run getRunByRefNames(String jobId, String buildId) {
		Run result = null;
		Job project = getJobByRefId(jobId);
		if (project != null) {
			result = project.getBuildByNumber(Integer.parseInt(buildId));
		}
		return result;
	}

	//  TODO: the below flow should go via JobProcessor, once scheduleBuild will be implemented for all of them
	private void doRunImpl(Job job, String originalBody) {
		ParametersAction parametersAction = new ParametersAction();
		if (originalBody != null && !originalBody.isEmpty() && originalBody.contains("parameters")) {
			CIParameters ciParameters = DTOFactory.getInstance().dtoFromJson(originalBody, CIParameters.class);
			parametersAction = new ParametersAction(createParameters(job, ciParameters));
		}

		Cause cause = new Cause.RemoteCause(ConfigurationService.getSettings(getInstanceId()) == null ? "non available URL" :
									ConfigurationService.getSettings(getInstanceId()).getLocation(), "octane driven execution");

		AbstractProjectProcessor jobProcessor = JobProcessorFactory.getFlowProcessor(job);
		jobProcessor.scheduleBuild(cause, parametersAction);
	}

	private List<ParameterValue> createParameters(Job project, CIParameters ciParameters) {
		List<ParameterValue> result = new ArrayList<>();
		boolean parameterHandled;
		ParameterValue tmpValue;
		ParametersDefinitionProperty paramsDefProperty = (ParametersDefinitionProperty) project.getProperty(ParametersDefinitionProperty.class);
		if (paramsDefProperty != null) {
			Map<String, CIParameter> ciParametersMap = ciParameters.getParameters().stream().collect(Collectors.toMap(CIParameter::getName, Function.identity()));
			for (ParameterDefinition paramDef : paramsDefProperty.getParameterDefinitions()) {
				parameterHandled = false;
				CIParameter ciParameter = ciParametersMap.remove(paramDef.getName());
				if (ciParameter != null) {
					tmpValue = null;
					switch (ciParameter.getType()) {
						case FILE:
							try {
								FileItemFactory fif = new DiskFileItemFactory();
								FileItem fi = fif.createItem(ciParameter.getName(), "text/plain", false, UUID.randomUUID().toString());
								fi.getOutputStream().write(DatatypeConverter.parseBase64Binary(ciParameter.getValue().toString()));
								tmpValue = new FileParameterValue(ciParameter.getName(), fi);
							} catch (IOException ioe) {
								logger.warn("failed to process file parameter", ioe);
							}
							break;
						case NUMBER:
							tmpValue = new StringParameterValue(ciParameter.getName(), ciParameter.getValue().toString());
							break;
						case STRING:
							tmpValue = new StringParameterValue(ciParameter.getName(), ciParameter.getValue().toString());
							break;
						case BOOLEAN:
							tmpValue = new BooleanParameterValue(ciParameter.getName(), Boolean.parseBoolean(ciParameter.getValue().toString()));
							break;
						case PASSWORD:
							tmpValue = new PasswordParameterValue(ciParameter.getName(), ciParameter.getValue().toString());
							break;
						default:
							break;
					}
					if (tmpValue != null) {
						result.add(tmpValue);
						parameterHandled = true;
					}
				}
				if (!parameterHandled) {
					if (paramDef instanceof FileParameterDefinition) {
						FileItemFactory fif = new DiskFileItemFactory();
						FileItem fi = fif.createItem(paramDef.getName(), "text/plain", false, "");
						try {
							fi.getOutputStream().write(new byte[0]);
						} catch (IOException ioe) {
							logger.error("failed to create default value for file parameter '" + paramDef.getName() + "'", ioe);
						}
						tmpValue = new FileParameterValue(paramDef.getName(), fi);
						result.add(tmpValue);
					} else {
						result.add(paramDef.getDefaultParameterValue());
					}
				}
			}

			//add parameters that are not defined in job
			for (CIParameter notDefinedParameter : ciParametersMap.values()) {
				tmpValue = new StringParameterValue(notDefinedParameter.getName(), notDefinedParameter.getValue().toString());
				result.add(tmpValue);
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
				if (item instanceof Job) {
					result = (Job) item;
				} else if (jobRefId.contains("/") && item == null) {
					String newJobRefId = jobRefId.substring(0, jobRefId.indexOf("/"));
					item = getTopLevelItem(newJobRefId);
					if (item != null) {
						Collection<? extends Job> allJobs = item.getAllJobs();
						for (Job job : allJobs) {
							if (jobRefId.endsWith(job.getName())) {
								result = job;
								break;
							}
						}
					}
				}
			} catch (UnsupportedEncodingException uee) {
				logger.error("failed to decode job ref ID '" + jobRefId + "'", uee);
			}
		}
		return result;
	}

	private Item getItemByRefId(String itemRefId) {
		Item result = null;
		if (itemRefId != null) {
			try {
				String itemRefIdUncoded = URLDecoder.decode(itemRefId, "UTF-8");
				if (itemRefIdUncoded.contains("/")) {
					String newItemRefId = itemRefIdUncoded.substring(0, itemRefIdUncoded.indexOf("/"));
					Item item = getTopLevelItem(newItemRefId);
					if (item != null && item.getClass().getName().equals(JobProcessorFactory.GITHUB_ORGANIZATION_FOLDER)) {
						Collection<? extends Item> allItems = ((OrganizationFolder) item).getItems();
						for (Item multibranchItem : allItems) {
							if (itemRefIdUncoded.endsWith(multibranchItem.getName())) {
								result = multibranchItem;
								break;
							}
						}
					}
				}
			} catch (UnsupportedEncodingException uee) {
				logger.error("failed to decode job ref ID '" + itemRefId + "'", uee);
			}
		}
		return result;
	}

	private TopLevelItem getTopLevelItem(String jobRefId) {
		TopLevelItem item;
		try {
			item = Jenkins.getInstance().getItem(jobRefId);
		} catch (AccessDeniedException e) {
			String user = ConfigurationService.getSettings(getInstanceId()).getImpersonatedUser();
			if (user != null && !user.isEmpty()) {
				throw new PermissionException(403);
			} else {
				throw new PermissionException(405);
			}
		}
		return item;
	}

	public static CIServerInfo getJenkinsServerInfo() {
		CIServerInfo result = dtoFactory.newDTO(CIServerInfo.class);
		String serverUrl = Jenkins.getInstance().getRootUrl();
		if (serverUrl != null && serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		result.setType(CIServerTypes.JENKINS.value())
				.setVersion(Jenkins.VERSION)
				.setUrl(serverUrl)
				.setSendingTime(System.currentTimeMillis());
		return result;
	}

	public static void publishEventToRelevantClients(CIEvent event) {
		OctaneSDK.getClients().forEach(octaneClient -> {
			String instanceId = octaneClient.getInstanceId();
			OctaneServerSettingsModel settings = ConfigurationService.getSettings(instanceId);
			if (settings != null && !settings.isSuspend()) {
				octaneClient.getEventsService().publishEvent(event);
			}
		});
	}
}
