package ngalambda;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClient;
import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;
import ngaclient.JobInfo;
import ngalambda.aws.NgaCodePipelineClient;
import ngalambda.aws.RegionUtil;
import ngalambda.aws.impl.DefaultCodePipelineClient;
import ngalambda.nga.NgaClient;
import ngalambda.nga.impl.DefaultNgaClient;

/**
 * Base class for NGA lambda functions.
 *
 * @author Robert Roth
 *
 */
public abstract class AbstractNgaLambda implements RequestHandler<Map<String, ?>, Object> {
    protected Regions region;
    protected NgaCodePipelineClient client;
    protected LambdaLogger logger;

    protected boolean isSetUp = false;

    public void setUpFromContext(final Context context) {
        if (!this.isSetUp) {
            this.region = RegionUtil.getRegionFromContext(context);
            final AWSCodePipeline awsclient = new AWSCodePipelineClient().withRegion(this.region);
            final AmazonS3 s3client = new AmazonS3Client().withRegion(this.region);
            this.logger = context.getLogger();
            this.client = new DefaultCodePipelineClient(awsclient, s3client, this.logger);
            this.isSetUp = true;
        }
    }

    public AbstractNgaLambda(final Context context, final NgaCodePipelineClient client, final LambdaLogger logger) {
        this.region = RegionUtil.getRegionFromContext(context);
        this.client = client;
        this.logger = logger;
        this.isSetUp = true;
    }

    public AbstractNgaLambda() {
        super();
    }

    /**
     * Build a unique job identifier from the pipeline name and stage name.
     *
     * @param pipelineName
     *            the name of the pipeline
     * @param stageName
     *            the name of the stage
     * @return the unique job identifier
     */
    protected String getJobId(final String pipelineName, final String stageName) {
        return pipelineName + "-" + stageName;
    }

    /**
     * Build a unique build identifier based on the revision id and job id.
     *
     * @param revisionId
     *            the revision id the build is for
     * @param jobId
     *            the job id the build is for
     * @return the unique build identifier
     */
    protected String getBuildId(final String revisionId, final String jobId) {
        return revisionId + "-" + jobId;
    }

    /**
     * Mark the current stage as finished and the next one as started.
     *
     * @param pipelineName
     *            the name of the pipeline
     * @param stageName
     *            the name of the finished stage
     * @param ngaClient
     *            the client to use for NGA operations
     * @throws IOException
     */
    protected void markStageAsFinished(final String pipelineName, final String stageName, final NgaClient ngaClient)
            throws IOException {
        final List<StageState> states = this.client.getPipelineState(pipelineName);

        int currentStageIndex = 0;
        while (currentStageIndex < states.size() && !states.get(currentStageIndex).getStageName().equals(stageName)) {
            currentStageIndex++;
        }

        final String revision = this.client.getRevision(states);

        this.logger.log("Current stage index is " + currentStageIndex);
        final String currentJobId = getJobId(pipelineName, stageName);
        final String currentBuildId = getBuildId(revision, currentJobId);

        this.logger.log("Current job id is " + currentJobId + ", current build id is " + currentBuildId);

        final String rootJobId = getJobId(pipelineName, states.get(0).getStageName());
        final String rootBuildId = getBuildId(revision, rootJobId);

        this.logger.log("Root job id is " + rootJobId + ", root build id is " + rootBuildId);

        final List<ActionState> currentStageActions = states.get(currentStageIndex).getActionStates();
        final ActionState lastAction = currentStageActions.get(currentStageActions.size() - 1);
        final Instant lastStatusChange = lastAction.getLatestExecution() != null
                ? lastAction.getLatestExecution().getLastStatusChange().toInstant() : Instant.now();
        ngaClient.updateBuildStatus(currentJobId, currentBuildId, BuildStatus.FINISHED, lastStatusChange,
                BuildResult.SUCCESS, 0, currentStageIndex == 0 ? null : rootJobId,
                currentStageIndex == 0 ? null : rootBuildId);

        // if we still have stages to mark as in progress, aka the codepipeline
        // run is not over yet
        if (currentStageIndex < states.size() - 1) {
            final String nextJobId = getJobId(pipelineName, states.get(currentStageIndex + 1).getStageName());
            final String nextBuildId = getBuildId(revision, nextJobId);

            this.logger.log("Next job id is " + nextJobId + ", next build id is " + nextJobId);

            ngaClient.updateBuildStatus(nextJobId, nextBuildId, BuildStatus.RUNNING, lastStatusChange,
                    BuildResult.UNAVAILABLE, 0, rootJobId, rootBuildId);
        }

    }

    /**
     * Process a Lambda job.
     *
     * @param client
     *            the NGA client to use
     * @param logger
     *            the logger to use for logging
     * @param job
     *            the job to process.
     */
    protected void processJob(final NgaCodePipelineClient client, final LambdaLogger logger, final Job job) {
        logger.log("Job ack returned " + client.acknowledgeJob(job.getId(), job.getNonce()));

        try {
            syncPipelineToNga(client, logger, job);
        } catch (final Exception e) {
            logger.log("Exception while syncing the pipeline " + e);
            e.printStackTrace();
        }
        logger.log("Put job success for job with id " + job.getId());
        client.putJobSuccess(job.getId());
    }

    /**
     * Sync a codepipeline pipeline to NGA.
     *
     * @param client
     * @param logger
     * @param job
     * @throws Exception
     */
    private void syncPipelineToNga(final NgaCodePipelineClient client, final LambdaLogger logger, final Job job)
            throws Exception {
        final String pipelineName = job.getData().getPipelineContext().getPipelineName();

        final PipelineDeclaration pipeline = client.getPipeline(pipelineName);

        final JobDetails details = client.getJobDetails(job.getId());

        try (NgaClient ngaClient = getNgaClient(pipeline, details)) {

            final List<JobInfo> stages = new ArrayList<JobInfo>(pipeline.getStages().size());
            for (final StageDeclaration stage : pipeline.getStages()) {
                stages.add(new JobInfo(getJobId(pipelineName, stage.getName()), stage.getName()));
                logger.log("Processing job " + stages.get(stages.size() - 1));
            }

            ngaClient.getCiServerId();
            ngaClient.syncPipeline(pipelineName, stages);
            markStageAsFinished(pipelineName, pipeline.getStages().get(0).getName(), ngaClient);
        }

    }

    /**
     * Build an NGA client based on the access details found in the job
     * configuration or the S3 folder of the pipeline.
     *
     * @param pipeline
     *            the pipeline to build the nga client for
     * @param details
     *            the job details including the access details if required
     * @return the {@link NgaClient} with the correct access details
     */
    protected NgaClient getNgaClient(final PipelineDeclaration pipeline, final JobDetails details) {
        final Properties config = this.client.getNgaConfigurationParametersFromJob(pipeline, details);

        final String clientId = (String) config.get(NgaCodePipelineClient.CLIENT_ID_KEY);
        final String clientSecret = (String) config.get(NgaCodePipelineClient.CLIENT_SECRET_KEY);
        final String ngaFullUrl = (String) config.get(NgaCodePipelineClient.NGA_URL_KEY);

        this.logger.log("Client id is " + clientId);
        this.logger.log("Client secret is " + clientSecret);
        this.logger.log("URL is " + ngaFullUrl);

        return new DefaultNgaClient(ngaFullUrl, clientId, clientSecret);
    }

}
