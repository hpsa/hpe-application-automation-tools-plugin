package ngalambda;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.CollectionUtils;

import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;
import ngalambda.aws.NgaCodePipelineClient;
import ngalambda.nga.NgaClient;

/**
 * Scheduled NGA lambda function executed periodically, processing pending NGA
 * configuration custom actions and passing recent (since the last execution)
 * job success/failure to NGA if any of the stages have finished with success or
 * any of the actions have failed.
 *
 * @author Robert Roth
 *
 */
public class NgaScheduled extends NgaConfig {

    public NgaScheduled() {
        super();
    }

    public NgaScheduled(final Context context, final NgaCodePipelineClient client) {
        super(context, client);
    }

    final class ActionInfo {
        private final ActionState action;
        private final String stageName;
        private final boolean isLastActionOfStage;

        public ActionState getAction() {
            return this.action;
        }

        public String getStageName() {
            return this.stageName;
        }

        public boolean isLastActionOfStage() {
            return this.isLastActionOfStage;
        }

        public ActionInfo(final ActionState action, final String stageName, final boolean isLastActionOfStage) {
            super();
            this.action = action;
            this.stageName = stageName;
            this.isLastActionOfStage = isLastActionOfStage;
        }

    }

    private static final long FRESH_CHANGE_SECONDS = 128; // 2 minutes and a
                                                          // couple of seconds.
                                                          // this time each
                                                          // event gets
                                                          // processed twice,
                                                          // but we have to make
                                                          // sure it gets
                                                          // processed
    private static final String JOB_SUCCEEDED = "Succeeded";
    private static final String JOB_FAILED = "Failed";
    private static final String JOB_IN_PROGRESS = "InProgress";

    @Override
    public Object handleRequest(final Map<String, ?> input, final Context context) {
        // process any events that have not been processed yet
        try {
            super.handleRequest(input, context);
        } catch (final IllegalStateException e) {
            // ignore to avoid rescheduling as we are running on a scheduled
            // basis anyway
        }
        // check for recently updated jobs in the user's pipelines
        final List<String> pipelines = this.client.getPipelines();
        for (final String pipeline : pipelines) {
            final List<StageState> state = this.client.getPipelineState(pipeline);
            final List<ActionInfo> lastActions = findRecentlyFinishedActions(state);
            if (!CollectionUtils.isNullOrEmpty(lastActions)) {
                final String revision = this.client.getRevision(state);
                for (int i = 0; i < lastActions.size(); i++) {
                    final ActionInfo lastAction = lastActions.get(i);
                    this.logger.log(Instant.now() + " Found last action " + lastAction.getStageName() + ":"
                            + lastAction.getAction().getActionName() + " with status "
                            + lastAction.getAction().getLatestExecution().getStatus());

                    final String lastJobId = getJobId(pipeline, lastAction.getStageName());
                    final String lastBuildId = getBuildId(revision, lastJobId);
                    final boolean isRootJob = lastAction.getStageName().equals(state.get(0).getStageName());
                    final String rootJobId = isRootJob ? null : getJobId(pipeline, state.get(0).getStageName());
                    final String rootBuildId = isRootJob ? null : getBuildId(revision, rootJobId);
                    this.logger.log("Recent job id is " + lastJobId + ", recent build id is " + lastBuildId);
                    this.logger.log("Root job id is " + rootJobId + ", root build id is " + rootBuildId);

                    try (NgaClient ngaClient = getNgaClient(this.client.getPipeline(pipeline), null)) {
                        final String lastStatus = lastAction.getAction().getLatestExecution().getStatus();
                        // for failed jobs mark the job as failed
                        if (lastStatus.equals(JOB_FAILED)) {
                            ngaClient.updateBuildStatus(lastJobId, lastBuildId, BuildStatus.FINISHED,
                                    lastAction.getAction().getLatestExecution().getLastStatusChange().toInstant(),
                                    BuildResult.FAILURE, 0, rootJobId, rootBuildId);
                        } else if (lastStatus.equals(JOB_SUCCEEDED) && lastAction.isLastActionOfStage()) {
                            // for succeeded actions at the end of a stage mark
                            // the
                            // stage as finished and start the next one
                            markStageAsFinished(pipeline, lastAction.getStageName(), ngaClient);
                        }
                    } catch (final Exception e) {
                        this.logger.log(Instant.now() + " Exception while sending job update to NGA : ");
                        e.printStackTrace();
                    }
                }

            }
        }

        return Boolean.TRUE;
    }

    private List<ActionInfo> findRecentlyFinishedActions(final List<StageState> state) {
        final List<ActionInfo> recents = new ArrayList<NgaScheduled.ActionInfo>(state.size());
        // we start looking from the second phase, as the first phase is the
        // source phase, we only create the pipeline in the second phase
        for (int stageIndex = 1; stageIndex < state.size(); stageIndex++) {
            final StageState currentStage = state.get(stageIndex);
            for (int actionIndex = 0; actionIndex < currentStage.getActionStates().size(); actionIndex++) {
                final ActionState currentAction = currentStage.getActionStates().get(actionIndex);
                // we only need Succeeded or Failed jobs
                if (currentAction.getLatestExecution() != null
                        && !currentAction.getLatestExecution().getStatus().equals(JOB_IN_PROGRESS)
                        // we only need recently changed actions
                        && Instant.now().getEpochSecond() - currentAction.getLatestExecution().getLastStatusChange()
                                .toInstant().getEpochSecond() < FRESH_CHANGE_SECONDS) {
                    this.logger.log(Instant.now() + " Found recently updated action " + currentAction.getActionName()
                            + " in stage " + currentStage.getStageName() + " with change date "
                            + currentAction.getLatestExecution().getLastStatusChange() + " and status "
                            + currentAction.getLatestExecution().getStatus());
                    if (currentAction.getLatestExecution().getStatus().equals(JOB_FAILED)
                            || actionIndex == currentStage.getActionStates().size() - 1) {
                        recents.add(new ActionInfo(currentAction, currentStage.getStageName(),
                                actionIndex == currentStage.getActionStates().size() - 1));
                    }
                }

            }
        }
        return recents;
    }

}
