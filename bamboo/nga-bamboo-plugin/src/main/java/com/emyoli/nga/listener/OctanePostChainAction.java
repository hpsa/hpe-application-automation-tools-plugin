package com.emyoli.nga.listener;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.plugins.PostChainAction;
import com.atlassian.bamboo.event.HibernateEventListenerAspect;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.results.tests.TestResults;
import com.atlassian.bamboo.v2.build.CurrentBuildResult;
import com.atlassian.bamboo.v2.build.events.BuildContextEvent;
import com.atlassian.bamboo.v2.build.events.PostBuildCompletedEvent;
import com.atlassian.event.api.EventListener;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.events.CIEvent;
import com.hp.octane.integrations.dto.events.CIEventType;
import com.hp.octane.integrations.dto.snapshots.CIBuildResult;
import com.hp.octane.integrations.dto.tests.BuildContext;
import com.hp.octane.integrations.dto.tests.TestRun;
import com.hp.octane.integrations.dto.tests.TestRunResult;
import com.hp.octane.integrations.dto.tests.TestsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OctanePostChainAction extends BaseListener implements PostChainAction {

    @EventListener
    @HibernateEventListenerAspect
    public void handle(BuildContextEvent event) {
        // TODO move this listener into OctanePostJobAction
        log.info("Build context event " + event.getClass().getSimpleName());
        if (event instanceof PostBuildCompletedEvent) {
            CurrentBuildResult results = event.getContext().getBuildResult();
            // Object source = event.getSource();
            PlanKey planKey = event.getPlanKey();
            PlanResultKey planResultKey = event.getPlanResultKey();
            {
                CIEventCause cause = CONVERTER.getCauseWithDetails(
                        event.getContext().getParentBuildIdentifier().getBuildResultKey(),
                        event.getContext().getParentBuildContext().getPlanResultKey().getPlanKey().getKey(), "admin");

                CIEvent ciEvent = CONVERTER.getEventWithDetails(planResultKey.getPlanKey().getKey(),
                        planResultKey.getKey(), event.getContext().getDisplayName(), CIEventType.FINISHED,
                        System.currentTimeMillis(), 100, Arrays.asList(cause),
                        String.valueOf(planResultKey.getBuildNumber()));
                ciEvent.setResult(event.getContext().getCurrentResult().getBuildState().equals(BuildState.SUCCESS)
                        ? CIBuildResult.SUCCESS : CIBuildResult.FAILURE);
                ciEvent.setDuration(System.currentTimeMillis() - ciEvent.getStartTime());
                OctaneSDK.getInstance().getEventsService().publishEvent(ciEvent);
            }
            String identifier = planKey.getKey();
            List<TestRun> testRuns = new ArrayList<TestRun>(results.getFailedTestResults().size()
                    + results.getSkippedTestResults().size() + results.getSuccessfulTestResults().size());
            for (TestResults currentTestResult : results.getFailedTestResults()) {
                testRuns.add(CONVERTER.getTestRunFromTestResult(currentTestResult, TestRunResult.FAILED,
                        results.getTasksStartDate().getTime()));
            }
            for (TestResults currentTestResult : results.getSkippedTestResults()) {
                testRuns.add(CONVERTER.getTestRunFromTestResult(currentTestResult, TestRunResult.SKIPPED,
                        results.getTasksStartDate().getTime()));
            }
            for (TestResults currentTestResult : results.getSuccessfulTestResults()) {
                testRuns.add(CONVERTER.getTestRunFromTestResult(currentTestResult, TestRunResult.PASSED,
                        results.getTasksStartDate().getTime()));
            }
            String build = PlanKeys.getPlanResultKey(identifier, planResultKey.getBuildNumber()).getKey();
            log.info("Pushing test results for " + identifier + " build " + planResultKey.getKey());
            BuildContext context = CONVERTER.getBuildContext(build, identifier);
            TestsResult testsResult = DTOFactory.getInstance().newDTO(TestsResult.class).setTestRuns(testRuns)
                    .setBuildContext(context);
            try {
                // TODO check if synchronous test results push is a good idea
                OctaneSDK.getInstance().getTestsService().pushTestsResult(testsResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // TODO if synchronous test results push is a bad idea,
            // move to a better inter-instance communication than a
            // synchronizedHashMap
            // BambooPluginServices.TEST_RESULTS.put(build, testsResult);
            // OctaneSDK.getInstance().getTestsService().enqueuePushTestsResult(identifier,
            // build);
        }
    }

    //
    public void execute(Chain chain, ChainResultsSummary chainResultsSummary, ChainExecution chainExecution)
            throws InterruptedException, Exception {
        log.info("Chain " + chain.getName() + " completed with result "
                + chainResultsSummary.getBuildState().toString());
        log.info("Build identifier " + chainExecution.getBuildIdentifier().getBuildResultKey() + " chain id "
                + chain.getKey());
        // String chainRes = chainResultsSummary.getPlanResultKey().getKey();
        // String jobRes =
        // chainResultsSummary.getOrderedJobResultSummaries().get(0).getPlanResultKey().getKey();
        List<CIEventCause> causes = new ArrayList<CIEventCause>();
        CIEvent event = CONVERTER.getEventWithDetails(chain.getPlanKey().getKey(),
                chainExecution.getBuildIdentifier().getBuildResultKey(), chain.getName(), CIEventType.FINISHED,
                chainExecution.getStartTime() != null ? chainExecution.getStartTime().getTime()
                        : chainExecution.getQueueTime().getTime(),
                chainResultsSummary.getDuration(), causes,
                String.valueOf(chainExecution.getBuildIdentifier().getBuildNumber()));
        event.setResult((chainResultsSummary.getBuildState() == BuildState.SUCCESS) ? CIBuildResult.SUCCESS
                : CIBuildResult.FAILURE);
        // TODO pushing finished type event with null duration results in http
        // 400, octane rest api could be more verbose to specify the reason
        event.setDuration(System.currentTimeMillis() - chainExecution.getQueueTime().getTime());
        OctaneSDK.getInstance().getEventsService().publishEvent(event);

    }

}
