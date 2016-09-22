package com.hp.octane.plugins.bamboo.octane;

import com.atlassian.bamboo.agent.classserver.AgentServerManager;
import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.builder.LifeCycleState;
import com.atlassian.bamboo.chains.cache.ImmutableChainStage;
import com.atlassian.bamboo.plan.PlanIdentifier;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.ImmutableResultsSummary;
import com.atlassian.sal.api.component.ComponentLocator;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.general.CIServerTypes;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.snapshots.CIBuildStatus;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.snapshots.SnapshotPhase;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestRun;
import com.hp.octane.integrations.dto.tests.TestRunError;
import com.hp.octane.integrations.dto.tests.TestRunResult;
import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;

import java.util.ArrayList;
import java.util.List;

public class DefaultOctaneConverter implements DTOConverter {

	private DTOFactory dtoFactoryInstance;

	private static DTOConverter converter;

	private DefaultOctaneConverter() {
		super();
		dtoFactoryInstance = DTOFactory.getInstance();
	}

	public static DTOConverter getInstance() {
		synchronized (DefaultOctaneConverter.class) {
			if (converter == null) {
				converter = new DefaultOctaneConverter();
			}
		}
		return converter;
	}

	public PipelineNode getPipelineNodeFromJob(ImmutableJob job) {
		return dtoFactoryInstance.newDTO(PipelineNode.class).setJobCiId(getJobCiId(job)).setName(job.getName());
	}

	public String getRootJobCiId(ImmutableTopLevelPlan plan) {
		return getCiId(plan);
	}

	public String getJobCiId(ImmutableJob job) {
		return getCiId(job);
	}

	public PipelinePhase getPipelinePhaseFromStage(ImmutableChainStage stage) {
		PipelinePhase phase = dtoFactoryInstance.newDTO(PipelinePhase.class).setName(stage.getName()).setBlocking(true);
		List<PipelineNode> nodes = new ArrayList<PipelineNode>(stage.getJobs().size());
		for (ImmutableJob job : stage.getJobs()) {
			// TODO decide if we want to mirror disabled jobs or not
			// if (!job.isSuspendedFromBuilding()) {
			nodes.add(getPipelineNodeFromJob(job));
			// }
		}
		phase.setJobs(nodes);
		return phase;
	}

	public CIProxyConfiguration getProxyCconfiguration(String server, int port, String user, String password) {
		return dtoFactoryInstance.newDTO(CIProxyConfiguration.class).setHost(server).setPort(port).setUsername(user)
				.setPassword(password);
	}

	public PipelineNode getRootPipelineNodeFromTopLevelPlan(ImmutableTopLevelPlan plan) {
		PipelineNode node = dtoFactoryInstance.newDTO(PipelineNode.class).setJobCiId(getRootJobCiId(plan))
				.setName(plan.getName());
		List<PipelinePhase> phases = new ArrayList<PipelinePhase>(plan.getAllStages().size());
		for (ImmutableChainStage stage : plan.getAllStages()) {
			phases.add(getPipelinePhaseFromStage(stage));
		}
		node.setPhasesInternal(phases);
		return node;
	}

	public CIServerInfo getServerInfo(String baseUrl, String instanceId) {
		return dtoFactoryInstance.newDTO(CIServerInfo.class)
				.setInstanceId(OctaneConfigurationKeys.BAMBOO_INSTANCE_PREFIX + instanceId)
				.setInstanceIdFrom(System.currentTimeMillis()).setSendingTime(System.currentTimeMillis())
				.setType(CIServerTypes.BAMBOO).setUrl(baseUrl);
	}

    public SnapshotNode getSnapshot(ImmutableTopLevelPlan plan, ImmutableResultsSummary resultsSummary){
        SnapshotNode snapshotNode = getSnapshotNode(plan, resultsSummary);

        List<SnapshotPhase> phases = new ArrayList<SnapshotPhase>(plan.getAllStages().size());
        for (ImmutableChainStage stage : plan.getAllStages()) {
            phases.add(getSnapshotPhaseFromStage(stage));
        }
        snapshotNode.setPhasesInternal(phases);
        return snapshotNode;
    }

    private SnapshotPhase getSnapshotPhaseFromStage(ImmutableChainStage stage) {
        SnapshotPhase phase = dtoFactoryInstance.newDTO(SnapshotPhase.class);
        phase.setName(stage.getName());
        phase.setBlocking(true);

        List<SnapshotNode> nodes = new ArrayList<SnapshotNode>(stage.getJobs().size());
        for (ImmutableJob job : stage.getJobs()) {
            // TODO decide if we want to mirror disabled jobs or not
            nodes.add(getSnapshotNode(job, job.getLatestResultsSummary()));
        }
        phase.setBuilds(nodes);

        return phase;
    }

    private SnapshotNode getSnapshotNode(ImmutablePlan immutablePlane, ImmutableResultsSummary resultsSummary) {

        SnapshotNode result = dtoFactoryInstance.newDTO(SnapshotNode.class)
                .setBuildCiId(resultsSummary.getPlanResultKey().getKey())
                .setName(immutablePlane.getName())
                .setJobCiId((getCiId(immutablePlane)))
                .setDuration(resultsSummary.getDuration())
                .setNumber(String.valueOf(resultsSummary.getBuildNumber()))
                .setResult(getJobResult(resultsSummary.getBuildState()))
                .setStatus(getJobStatus(immutablePlane.getLatestResultsSummary().getLifeCycleState()))
                .setStartTime(resultsSummary.getBuildDate() != null ? resultsSummary.getBuildDate().getTime()
                        : (resultsSummary.getBuildCompletedDate() != null
                        ? resultsSummary.getBuildCompletedDate().getTime() : System.currentTimeMillis()));
        return result;
    }

    private CIBuildStatus getJobStatus(LifeCycleState lifeCycleState){
        switch (lifeCycleState) {
            case FINISHED:
                return CIBuildStatus.FINISHED;
            case IN_PROGRESS:
                return CIBuildStatus.RUNNING;
            case QUEUED:
                return CIBuildStatus.QUEUED;
            default:
                return CIBuildStatus.UNAVAILABLE;
        }
    }

    private CIBuildResult getJobResult(BuildState buildState){

        switch (buildState) {
            case FAILED:
                return CIBuildResult.FAILURE;
            case SUCCESS:
                return CIBuildResult.SUCCESS;
            default:
                return  CIBuildResult.UNAVAILABLE;
        }
    }

	public CIJobsList getRootJobsList(List<ImmutableTopLevelPlan> plans) {
		CIJobsList jobsList = dtoFactoryInstance.newDTO(CIJobsList.class).setJobs(new PipelineNode[0]);

		List<PipelineNode> nodes = new ArrayList<PipelineNode>(plans.size());
		for (ImmutableTopLevelPlan plan : plans) {
			PipelineNode node = DTOFactory.getInstance().newDTO(PipelineNode.class).setJobCiId(getRootJobCiId(plan))
					.setName(plan.getName());
			nodes.add(node);
		}

		jobsList.setJobs(nodes.toArray(new PipelineNode[nodes.size()]));
		return jobsList;
	}

	public String getCiId(PlanIdentifier identifier) {
		return identifier.getPlanKey().getKey();
	}

	public TestRun getTestRunFromTestResult(TestResults testResult, TestRunResult result, long startTime) {
		String className = testResult.getClassName();
		String simpleName = testResult.getShortClassName();
		String packageName = className.substring(0,
				className.length() - simpleName.length() - (className.length() > simpleName.length() ? 1 : 0));

		TestRun testRun = dtoFactoryInstance.newDTO(TestRun.class).setClassName(simpleName)
				.setDuration(Math.round(Double.valueOf(testResult.getDuration()))).setPackageName(packageName)
				.setResult(result).setStarted(startTime).setTestName(testResult.getActualMethodName());
		if (result == TestRunResult.FAILED) {
			TestRunError error = dtoFactoryInstance.newDTO(TestRunError.class)
					.setErrorMessage(testResult.getSystemOut());
			if (!testResult.getErrors().isEmpty()) {
				error.setStackTrace(testResult.getErrors().get(0).getContent());
			}
			testRun.setError(error);
		}
		return testRun;
	}

	public CIEvent getEventWithDetails(String project, String buildCiId, String displayName, CIEventType eventType,
	                                   long startTime, long estimatedDuration, List<CIEventCause> causes, String number) {
		CIEvent event = dtoFactoryInstance.newDTO(CIEvent.class).setEventType(eventType).setCauses(causes)
				.setProject(project).setProjectDisplayName(displayName).setBuildCiId(buildCiId)
				.setEstimatedDuration(estimatedDuration).setStartTime(startTime);
		if (number != null) {
			event.setNumber(number);
		}
		return event;
	}

	public CIEventCause getCauseWithDetails(String buildCiId, String project, String user) {
		CIEventCause cause = DTOFactory.getInstance().newDTO(CIEventCause.class).setBuildCiId(buildCiId)
				.setCauses(new ArrayList<CIEventCause>()).setProject(project).setType(CIEventCauseType.UPSTREAM)
				.setUser(user);
		return cause;
	}

	public BuildContext getBuildContext(String build, String identifier) {
		String instanceId = OctaneConfigurationKeys.BAMBOO_INSTANCE_PREFIX + String.valueOf(
				ComponentLocator.getComponent(AgentServerManager.class).getFingerprint().getServerFingerprint());
		return DTOFactory.getInstance().newDTO(BuildContext.class).setBuildId(build).setBuildName(build)
				.setJobId(identifier).setJobName(identifier).setServerId(instanceId);
	}

}
