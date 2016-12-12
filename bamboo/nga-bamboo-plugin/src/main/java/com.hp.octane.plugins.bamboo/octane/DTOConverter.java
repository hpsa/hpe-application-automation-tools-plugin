package com.hp.octane.plugins.bamboo.octane;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.chains.cache.ImmutableChainStage;
import com.atlassian.bamboo.plan.PlanIdentifier;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.ImmutableResultsSummary;
import com.hp.octane.integrations.dto.api.causes.CIEventCause;
import com.hp.octane.integrations.dto.api.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.api.events.CIEvent;
import com.hp.octane.integrations.dto.api.events.CIEventType;
import com.hp.octane.integrations.dto.api.general.CIJobsList;
import com.hp.octane.integrations.dto.api.general.CIServerInfo;
import com.hp.octane.integrations.dto.api.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.api.pipelines.PipelinePhase;
import com.hp.octane.integrations.dto.api.scm.SCMData;
import com.hp.octane.integrations.dto.api.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.api.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.api.tests.BuildContext;
import com.hp.octane.integrations.dto.api.tests.TestRun;
import com.hp.octane.integrations.dto.api.tests.TestRunResult;

import java.util.List;

public interface DTOConverter {
	PipelineNode getRootPipelineNodeFromTopLevelPlan(ImmutableTopLevelPlan plan);

	PipelineNode getPipelineNodeFromJob(ImmutableJob job);

	PipelinePhase getPipelinePhaseFromStage(ImmutableChainStage stage);

	CIProxyConfiguration getProxyCconfiguration(String proxyServer, int proxyPort, String proxyUser,
	                                            String proxyPassword);

	CIServerInfo getServerInfo(String baseUrl, String instanceId);

	SnapshotNode getSnapshot(ImmutableTopLevelPlan plan, ImmutableResultsSummary summary);

	CIJobsList getRootJobsList(List<ImmutableTopLevelPlan> toplevels);

	String getCiId(PlanIdentifier identifier);

	TestRun getTestRunFromTestResult(TestResults currentTestResult, TestRunResult result, long startTime);


	CIEvent getEventWithDetails(String project, String buildCiId, String displayName, CIEventType eventType,
								long startTime, long estimatedDuration, List<CIEventCause> causes, String number, BuildState buildState, Long currnetTime);

	CIEvent getEventWithDetails(String project, String buildCiId, String displayName, CIEventType eventType,
	                            long startTime, long estimatedDuration, List<CIEventCause> causes, String number);

	CIEvent getEventWithDetails(String project, String buildCiId, String displayName, CIEventType eventType,
								long startTime, long estimatedDuration, List<CIEventCause> causes, String number, SCMData scmData);


	CIEventCause getCauseWithDetails(String buildCiId, String project, String user);

	BuildContext getBuildContext(String instanceId, String identifier, String build);

	SCMData getScmData(com.atlassian.bamboo.v2.build.BuildContext buildContext);
}
