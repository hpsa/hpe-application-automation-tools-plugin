/*
 * Certain versions of software and/or documents ("Material") accessible here may contain branding from
 * Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 * the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 * and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 * marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * (c) Copyright 2012-2019 Micro Focus or one of its affiliates.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are set forth in the express warranty statements
 * accompanying such products and services. Nothing herein should be construed as
 * constituting an additional warranty. Micro Focus shall not be liable for technical
 * or editorial errors or omissions contained herein.
 * The information contained herein is subject to change without notice.
 * ___________________________________________________________________
 */

package com.microfocus.application.automation.tools.octane;

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
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
import com.hp.octane.integrations.dto.securityscans.FodServerConfiguration;
import com.hp.octane.integrations.dto.securityscans.SSCProjectConfiguration;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.exceptions.ConfigurationException;
import com.hp.octane.integrations.exceptions.PermissionException;
import com.microfocus.application.automation.tools.model.OctaneServerSettingsModel;
import com.microfocus.application.automation.tools.octane.configuration.ConfigurationService;
import com.microfocus.application.automation.tools.octane.configuration.FodConfigUtil;
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
import com.microfocus.application.automation.tools.octane.tests.junit.JUnitExtension;
import hudson.ProxyConfiguration;
import hudson.console.PlainTextConsoleOutputStream;
import hudson.matrix.MatrixConfiguration;
import hudson.model.*;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import org.acegisecurity.AccessDeniedException;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;
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
		return new File(Jenkins.get().getRootDir(), "userContent");
	}

	@Override
	public CIProxyConfiguration getProxyConfiguration(URL targetUrl) {
		CIProxyConfiguration result = null;
		ProxyConfiguration proxy = Jenkins.get().proxy;
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
		ACLContext securityContext = startImpersonation();
		CIJobsList result = dtoFactory.newDTO(CIJobsList.class);
		Map<String, PipelineNode> jobsMap = new HashMap<>();

		try {
			boolean hasReadPermission = Jenkins.get().hasPermission(Item.READ);
			if (!hasReadPermission) {
				throw new PermissionException(403);
			}

			Collection<String> jobNames = Jenkins.get().getJobNames();
			for (String jobName : jobNames) {
				String tempJobName = jobName;
				try {
					Job tmpJob = (Job) Jenkins.get().getItemByFullName(tempJobName);

					if (tmpJob == null) {
						continue;
					}
					if (tmpJob instanceof AbstractProject && ((AbstractProject) tmpJob).isDisabled()) {
						continue;
					}
					if (tmpJob instanceof MatrixConfiguration) {
						continue;
					}

					PipelineNode tmpConfig;
					if (JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME.equals(tmpJob.getParent().getClass().getName())) {
						tempJobName = tmpJob.getParent().getFullName();
						tmpConfig = createPipelineNodeFromJobName(tempJobName);
					} else {
						tmpConfig = createPipelineNode(tempJobName, tmpJob, includeParameters);
					}
					jobsMap.put(tempJobName, tmpConfig);
				} catch (Throwable e) {
					logger.error("failed to add job '" + tempJobName + "' to JobList", e);
				}
			}

			result.setJobs(jobsMap.values().toArray(new PipelineNode[0]));
		} catch (AccessDeniedException ade) {
			throw new PermissionException(403);
		} finally {
			stopImpersonation(securityContext);
		}

		return result;
	}

	@Override
	public PipelineNode getPipeline(String rootJobCiId) {
		ACLContext securityContext = startImpersonation();
		try {
			PipelineNode result;
			boolean hasRead = Jenkins.get().hasPermission(Item.READ);
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
		ACLContext securityContext = startImpersonation();
		try {
			Job job = getJobByRefId(jobCiId);
			//create UFT test runner job on the fly if missing
			if (job == null && jobCiId != null && jobCiId.startsWith(UftConstants.EXECUTION_JOB_MIDDLE_NAME_WITH_TEST_RUNNERS)) {
				job = createExecutorByJobName(jobCiId);
			}
			if (job != null) {
				if (job instanceof AbstractProject && ((AbstractProject) job).isDisabled()) {
					//disabled job is not runnable and in this context we will handle it as 404
					throw new ConfigurationException(404);
				}
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
	public void stopPipelineRun(String jobCiId, String originalBody) {
		ACLContext securityContext = startImpersonation();
		try {
			Job job = getJobByRefId(jobCiId);
			if (job != null) {
				boolean hasAbortPermissions = job.hasPermission(Item.CANCEL);
				if (!hasAbortPermissions) {
					stopImpersonation(securityContext);
					throw new PermissionException(403);
				}
				if (job instanceof AbstractProject || job.getClass().getName().equals(JobProcessorFactory.WORKFLOW_JOB_NAME)) {
					doStopImpl(job, originalBody);
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
		ACLContext securityContext = startImpersonation();
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
		ACLContext securityContext = startImpersonation();
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
		ACLContext originalContext = startImpersonation();
		try {
			InputStream result = null;
			Run run = getRunByRefNames(jobId, buildId);
			if (run != null) {
				try {
					result = new FileInputStream(run.getRootDir() + File.separator + TestListener.TEST_RESULT_FILE);
				} catch (Exception fnfe) {
					logger.error("'" + TestListener.TEST_RESULT_FILE + "' file no longer exists, test results of '" + jobId + " #" + buildId + "' won't be pushed to Octane", fnfe);
				}
				tryRemoveTempTestResultFile(run);
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' not found");
			}
			return result;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	private void tryRemoveTempTestResultFile(Run run) {
		try {
			File[] matches = run.getRootDir().listFiles((dir, name) -> name.startsWith(JUnitExtension.TEMP_TEST_RESULTS_FILE_NAME_PREFIX));
			if (matches != null) {
				for (File f : matches) {
					try {
						Files.deleteIfExists(f.toPath());
					} catch (Exception e) {
						logger.error("Failed to delete the temp test result file at '" + f.getPath() + "'", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Fail to tryRemoveTempTestResultFile : " + e.getMessage());
		}
	}

	@Override
	public InputStream getBuildLog(String jobId, String buildId) {
		ACLContext originalContext = startImpersonation();
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
		ACLContext originalContext = startImpersonation();
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
		ACLContext originalContext = startImpersonation();
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
	public Long getFodRelease(String jobId, String buildId) {
		ACLContext originalContext = startImpersonation();
		try {
			Run run = getRunByRefNames(jobId, buildId);
			if (run instanceof AbstractBuild) {
				return FodConfigUtil.getFODReleaseFromBuild((AbstractBuild) run);
			} else {
				logger.error("build '" + jobId + " #" + buildId + "' (of specific type AbstractBuild) not found");
				return null;
			}
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public FodServerConfiguration getFodServerConfiguration() {

		ACLContext originalContext = startImpersonation();
		try {

			FodConfigUtil.ServerConnectConfig fodServerConfig = FodConfigUtil.getFODServerConfig();
			if (fodServerConfig != null) {
				return dtoFactory.newDTO(FodServerConfiguration.class)
						.setClientId(fodServerConfig.clientId)
						.setClientSecret(fodServerConfig.clientSecret)
						.setApiUrl(fodServerConfig.apiUrl)
						.setBaseUrl(fodServerConfig.baseUrl);
			}
			return null;
		} finally {
			stopImpersonation(originalContext);
		}
	}

	@Override
	public void runTestDiscovery(DiscoveryInfo discoveryInfo) {
		ACLContext securityContext = startImpersonation();
		try {
			TestExecutionJobCreatorService.runTestDiscovery(discoveryInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public PipelineNode createExecutor(DiscoveryInfo discoveryInfo) {
		if (EntityConstants.Executors.UFT_TEST_RUNNER_SUBTYPE_ENTITY_NAME.equals(discoveryInfo.getExecutorType())) {
			ACLContext securityContext = startImpersonation();
			try {
				Job project = TestExecutionJobCreatorService.createExecutor(discoveryInfo);
				return ModelFactory.createStructureItem(project);
			} finally {
				stopImpersonation(securityContext);
			}
		} else {
			return null;
		}
	}


	private Job createExecutorByJobName(String uftExecutorJobNameWithTestRunner) {
		ACLContext securityContext = startImpersonation();
		try {
			return TestExecutionJobCreatorService.createExecutorByJobName(uftExecutorJobNameWithTestRunner);
		} catch (Exception e) {
			logger.warn("Failed to create createExecutor by name : " + e.getMessage());
			return null;
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public void runTestSuiteExecution(TestSuiteExecutionInfo suiteExecutionInfo) {
		ACLContext securityContext = startImpersonation();
		try {
			TestExecutionJobCreatorService.runTestSuiteExecution(suiteExecutionInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public OctaneResponse checkRepositoryConnectivity(TestConnectivityInfo testConnectivityInfo) {
		ACLContext securityContext = startImpersonation();
		try {
			return ExecutorConnectivityService.checkRepositoryConnectivity(testConnectivityInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public void deleteExecutor(String id) {
		ACLContext securityContext = startImpersonation();
		try {
			UftJobCleaner.deleteDiscoveryJobByExecutor(id);
			UftJobCleaner.deleteExecutionJobByExecutorIfNeverExecuted(id);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	@Override
	public OctaneResponse upsertCredentials(CredentialsInfo credentialsInfo) {
		ACLContext securityContext = startImpersonation();
		try {
			return ExecutorConnectivityService.upsertRepositoryCredentials(credentialsInfo);
		} finally {
			stopImpersonation(securityContext);
		}
	}

	private ACLContext startImpersonation() {
		return ImpersonationUtil.startImpersonation(getInstanceId());
	}

	private void stopImpersonation(ACLContext impersonatedContext) {
		ImpersonationUtil.stopImpersonation(impersonatedContext);
	}

	private PipelineNode createPipelineNode(String name, Job job, boolean includeParameters) {
		PipelineNode tmpConfig = dtoFactory.newDTO(PipelineNode.class)
				.setJobCiId(JobProcessorFactory.getFlowProcessor(job).getTranslatedJobName())
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

	private void doRunImpl(Job job, String originalBody) {
		AbstractProjectProcessor jobProcessor = JobProcessorFactory.getFlowProcessor(job);
		doRunStopImpl(jobProcessor::scheduleBuild, "execution", job, originalBody);
	}

	private void doStopImpl(Job job, String originalBody) {
		AbstractProjectProcessor jobProcessor = JobProcessorFactory.getFlowProcessor(job);
		doRunStopImpl(jobProcessor::cancelBuild, "stop", job, originalBody);
	}

	//  TODO: the below flow should go via JobProcessor, once scheduleBuild will be implemented for all of them
	private void doRunStopImpl(BiConsumer<Cause, ParametersAction> method, String methodName, Job job, String originalBody) {
		ParametersAction parametersAction = new ParametersAction();
		if (originalBody != null && !originalBody.isEmpty() && originalBody.contains("parameters")) {
			CIParameters ciParameters = DTOFactory.getInstance().dtoFromJson(originalBody, CIParameters.class);
			parametersAction = new ParametersAction(createParameters(job, ciParameters));
		}

		Cause cause = new Cause.RemoteCause(ConfigurationService.getSettings(getInstanceId()) == null ? "non available URL" :
				ConfigurationService.getSettings(getInstanceId()).getLocation(), "octane driven " + methodName);
		method.accept(cause, parametersAction);
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
				jobRefId = URLDecoder.decode(jobRefId, StandardCharsets.UTF_8.name());
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
		if (itemRefId == null) {
			return null;
		}

		try {
			String itemRefIdDecoded = URLDecoder.decode(itemRefId, StandardCharsets.UTF_8.name());
			if (!itemRefIdDecoded.contains("/")) {
				return null;
			}

			String newItemRefId = itemRefIdDecoded.substring(0, itemRefIdDecoded.indexOf('/'));
			Item item = getTopLevelItem(newItemRefId);
			if (item == null) {
				return null;
			}

			Item result = null;
			if (item.getClass().getName().equals(JobProcessorFactory.GITHUB_ORGANIZATION_FOLDER)) {
				Collection<? extends Item> allItems = ((AbstractFolder) item).getItems();
				for (Item multiBranchItem : allItems) {
					if (itemRefIdDecoded.endsWith(multiBranchItem.getName())) {
						result = multiBranchItem;
						break;
					}
				}
			} else {
				Collection<? extends Job> allJobs = item.getAllJobs();
				for (Job job : allJobs) {
					if (JobProcessorFactory.WORKFLOW_MULTI_BRANCH_JOB_NAME.equals(job.getParent().getClass().getName()) &&
							itemRefId.endsWith(job.getParent().getFullName())
					) {
						result = (Item) job.getParent();
					} else {
						if (itemRefId.endsWith(job.getName())) {
							result = job;
						}
					}
					if (result != null) {
						break;
					}
				}
			}
			return result;
		} catch (UnsupportedEncodingException uee) {
			logger.error("failed to decode job ref ID '" + itemRefId + "'", uee);
			return null;
		}
	}

	private TopLevelItem getTopLevelItem(String jobRefId) {
		TopLevelItem item;
		try {
			item = Jenkins.get().getItem(jobRefId);
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
		String serverUrl = Jenkins.get().getRootUrl();
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
