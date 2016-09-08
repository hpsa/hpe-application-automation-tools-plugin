package ngalambda.aws.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClient;
import com.amazonaws.services.codepipeline.model.AcknowledgeJobRequest;
import com.amazonaws.services.codepipeline.model.AcknowledgeJobResult;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.ActionTypeId;
import com.amazonaws.services.codepipeline.model.EncryptionKey;
import com.amazonaws.services.codepipeline.model.EncryptionKeyType;
import com.amazonaws.services.codepipeline.model.GetJobDetailsRequest;
import com.amazonaws.services.codepipeline.model.GetJobDetailsResult;
import com.amazonaws.services.codepipeline.model.GetPipelineRequest;
import com.amazonaws.services.codepipeline.model.GetPipelineResult;
import com.amazonaws.services.codepipeline.model.GetPipelineStateRequest;
import com.amazonaws.services.codepipeline.model.GetPipelineStateResult;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.ListPipelinesRequest;
import com.amazonaws.services.codepipeline.model.ListPipelinesResult;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.PipelineSummary;
import com.amazonaws.services.codepipeline.model.PollForJobsRequest;
import com.amazonaws.services.codepipeline.model.PollForJobsResult;
import com.amazonaws.services.codepipeline.model.PutJobSuccessResultRequest;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;

import ngalambda.aws.NgaCodePipelineClient;

public class DefaultCodePipelineClient implements NgaCodePipelineClient {

    private final AWSCodePipeline codePipeline;

    private final AmazonS3 s3;

    private final LambdaLogger logger;

    public DefaultCodePipelineClient(final AWSCodePipeline codePipeline, final AmazonS3 s3, final LambdaLogger logger) {
        super();
        this.codePipeline = codePipeline;
        this.s3 = s3;
        this.logger = logger;
    }

    public static void main(final String... args) {
        final DefaultCodePipelineClient cp = new DefaultCodePipelineClient(
                new AWSCodePipelineClient().withRegion(Regions.US_WEST_2),
                new AmazonS3Client().withRegion(Regions.US_WEST_2), new LambdaLogger() {

                    @Override
                    public void log(final String string) {
                        System.out.println(string);
                    }
                });
        final PipelineDeclaration pipeline = cp.getPipeline("NGA-pipeline");
        final JobDetails details = cp.getJobDetails("f8555096-9195-4738-868c-728853493354");
        final Properties props = cp.getNgaConfigurationParametersFromJob(pipeline, details);
        System.out.println(props);
    }

    @Override
    public List<Job> pollForJobs(final ActionCategory category, final ActionOwner owner, final String provider,
            final String version) {
        final ActionTypeId id = new ActionTypeId().withCategory(category).withOwner(owner).withProvider(provider)
                .withVersion(version);
        final PollForJobsRequest request = new PollForJobsRequest().withActionTypeId(id);
        this.logger.log(Instant.now() + " Polling for jobs");
        final PollForJobsResult result = this.codePipeline.pollForJobs(request);
        this.logger.log(Instant.now() + " Got " + result.getJobs().size() + " jobs");
        return result.getJobs();
    }

    @Override
    public String acknowledgeJob(final String jobId, final String nonce) {
        final AcknowledgeJobRequest request = new AcknowledgeJobRequest().withJobId(jobId).withNonce(nonce);
        this.logger.log(Instant.now() + " Acking job " + jobId + " with nonce " + nonce);
        final AcknowledgeJobResult result = this.codePipeline.acknowledgeJob(request);
        this.logger.log(Instant.now() + " Acked job " + jobId + " status is " + result.getStatus());
        return result.getStatus();
    }

    @Override
    public void putJobSuccess(final String jobId) {
        final PutJobSuccessResultRequest request = new PutJobSuccessResultRequest().withJobId(jobId);
        this.logger.log(Instant.now() + " Put job success for " + jobId);
        this.codePipeline.putJobSuccessResult(request);
        this.logger.log(Instant.now() + " Put job success finished for " + jobId);
    }

    @Override
    public PipelineDeclaration getPipeline(final String pipelineName) {
        final GetPipelineRequest request = new GetPipelineRequest().withName(pipelineName);
        this.logger.log(Instant.now() + " Get pipeline for " + pipelineName);
        final GetPipelineResult result = this.codePipeline.getPipeline(request);
        this.logger.log(Instant.now() + " Get pipeline finished for " + pipelineName);
        return result.getPipeline();
    }

    @Override
    public JobDetails getJobDetails(final String jobId) {
        final GetJobDetailsRequest request = new GetJobDetailsRequest().withJobId(jobId);
        this.logger.log(Instant.now() + " Get job details for " + jobId);
        final GetJobDetailsResult result = this.codePipeline.getJobDetails(request);
        this.logger.log(Instant.now() + " Get job details finished for " + jobId);
        return result.getJobDetails();
    }

    @Override
    public Properties getNgaConfigurationParametersFromJob(final PipelineDeclaration pipeline,
            final JobDetails details) {
        final ActionTypeId actionType = details != null ? details.getData().getActionTypeId() : null;

        if (actionType != null && actionType.getProvider().equals(NGA_PROVIDER)
                && actionType.getCategory().equals(ActionCategory.Build.toString())) {
            // we are in the config action, we have the job details in the
            // context

            final Map<?, ?> config = details.getData().getActionConfiguration().getConfiguration();
            final Properties props = new Properties();
            props.put(CLIENT_ID_KEY, config.get(CLIENT_ID_KEY));
            props.put(CLIENT_SECRET_KEY, config.get(CLIENT_SECRET_KEY));
            props.put(NGA_URL_KEY, config.get(NGA_URL_KEY));
            this.logger.log(Instant.now() + " Store NGA access details");
            storeNgaConfigProperties(pipeline.getArtifactStore().getLocation(), pipeline.getName(), props,
                    details.getData().getEncryptionKey());
            this.logger.log(Instant.now() + " Store NGA access details finished");
            return props;

        } else {
            return getNgaConfigProperties(pipeline.getArtifactStore().getLocation(), pipeline.getName());
        }
    }

    private Properties getNgaConfigProperties(final String location, final String pipelineName) {
        this.logger
                .log(Instant.now() + " Retrieve NGA access details from " + location + " for pipeline " + pipelineName);
        final Properties properties = new Properties();
        try {
            final GetObjectRequest request = new GetObjectRequest(location, pipelineName + "/" + NGA_CONFIG_FILE);
            try (S3ObjectInputStream stream = this.s3.getObject(request).getObjectContent()) {
                properties.load(stream);
            }
            this.logger.log(Instant.now() + " Retrieve NGA access details finished from " + location + " for pipeline "
                    + pipelineName);
        } catch (AmazonClientException | IOException e) {
            System.out.println("Exception while loading stream with properties " + e);

            e.printStackTrace();
        }
        return properties;
    }

    private static SSEAwsKeyManagementParams getSSEAwsKeyManagementParams(final EncryptionKey encryptionKey) {
        if (encryptionKey != null && encryptionKey.getId() != null
                && EncryptionKeyType.KMS.toString().equals(encryptionKey.getType())) {
            // Use a customer-managed encryption key
            return new SSEAwsKeyManagementParams(encryptionKey.getId());
        }
        // Use the default master key
        return new SSEAwsKeyManagementParams();
    }

    private void storeNgaConfigProperties(final String bucket, final String targetFolder, final Properties properties,
            final EncryptionKey encryptionKey) {
        try {
            final File tmpConfig = File.createTempFile("nga-config", ".tmp", new File(NGA_CONFIG_PATH));
            properties.store(new FileWriter(tmpConfig), "nga access details for codepipeline");
            try {
                final PutObjectRequest request = new PutObjectRequest(bucket, targetFolder + "/" + NGA_CONFIG_FILE,
                        tmpConfig).withSSEAwsKeyManagementParams(getSSEAwsKeyManagementParams(encryptionKey));
                this.s3.putObject(request);
            } catch (final Exception e) {
                System.out.println("Error uploading nga config file to s3" + e);

            }
            tmpConfig.delete();
        } catch (final IOException e) {
            System.out.println("Error storing temporary nga config file " + e);
        }

    }

    @Override
    public List<StageState> getPipelineState(final String pipelineName) {
        final GetPipelineStateRequest request = new GetPipelineStateRequest().withName(pipelineName);
        this.logger.log(Instant.now() + " Get pipeline state for " + pipelineName);
        final GetPipelineStateResult result = this.codePipeline.getPipelineState(request);
        this.logger.log(Instant.now() + " Get pipeline state finished for " + pipelineName);
        return result.getStageStates();
    }

    @Override
    public String getRevision(final List<StageState> states) {
        for (final StageState state : states) {
            for (final ActionState aState : state.getActionStates()) {
                if (aState.getCurrentRevision() != null) {
                    return aState.getCurrentRevision().getRevisionId() + "-"
                            + aState.getCurrentRevision().getCreated().toInstant().getEpochSecond();
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getPipelines() {
        this.logger.log(Instant.now() + " Getting pipeline list");
        final ListPipelinesRequest request = new ListPipelinesRequest();
        final ListPipelinesResult result = this.codePipeline.listPipelines(request);
        this.logger.log(Instant.now() + " Got pipeline list with " + result.getPipelines().size() + " pipelines");
        final List<String> pipelines = new ArrayList<String>();
        for (final PipelineSummary summary : result.getPipelines()) {
            pipelines.add(summary.getName());
        }
        return pipelines;

    }

    @Override
    public InputStream getPipelineArtifactStream(final String bucket, final String objectKey) {
        final S3Object result = this.s3.getObject(bucket, objectKey);
        return result.getObjectContent();
    }

}
