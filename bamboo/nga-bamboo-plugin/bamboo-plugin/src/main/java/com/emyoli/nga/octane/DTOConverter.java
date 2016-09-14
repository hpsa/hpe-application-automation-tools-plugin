package com.emyoli.nga.octane;

import com.atlassian.bamboo.chains.cache.ImmutableChainStage;
import com.atlassian.bamboo.plan.PlanIdentifier;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutableTopLevelPlan;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.resultsummary.ImmutableResultsSummary;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.configuration.CIProxyConfiguration;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.general.CIJobsList;
import com.hp.octane.integrations.dto.general.CIServerInfo;
import com.hp.octane.integrations.dto.pipelines.PipelineNode;
import com.hp.octane.integrations.dto.pipelines.PipelinePhase;
import com.hp.octane.integrations.dto.snapshots.SnapshotNode;
import com.hp.octane.integrations.dto.tests.TestRun;
import com.hp.octane.integrations.dto.tests.TestRunResult;

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
            long startTime, long estimatedDuration, List<CIEventCause> causes, String number);

    CIEventCause getCauseWithDetails(String buildCiId, String project, String user);

}
