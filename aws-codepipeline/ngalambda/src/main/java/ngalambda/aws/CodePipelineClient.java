package ngalambda.aws;

import java.io.InputStream;
import java.util.List;

import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;

public interface CodePipelineClient {
    /**
     * Poll for jobs with the given parameters
     *
     * @param category
     *            the category of the jobs to poll
     * @param owner
     *            the job owner of the jobs to poll
     * @param provider
     *            the provider of the jobs to poll
     * @param version
     *            the version of the jobs to poll
     * @return the jobs matching the given criteria, if any
     */
    List<Job> pollForJobs(ActionCategory category, ActionOwner owner, String provider, String version);

    /**
     * Acknowledge a job on codepipeline to let it know that it is being
     * processed, to avoid processing it with another cusotm action processor.
     *
     * @param jobId
     *            the id of the job to ack
     * @param nonce
     *            the nonce of the job to ack
     * @return the current status of the job
     */
    String acknowledgeJob(String jobId, String nonce);

    /**
     * Mark a job as finished with success.
     *
     * @param jobId
     *            the id of the job to mark as finished.
     */
    void putJobSuccess(String jobId);

    /**
     * Get the pipeline declaration of the pipeline with the given name.
     *
     * @param pipelineName
     *            the name of the pipeline to retrieve
     * @return the {@link PipelineDeclaration} object for the pipeline
     */
    PipelineDeclaration getPipeline(String pipelineName);

    /**
     * Get job details including secret configuration.
     *
     * @param jobId
     *            the id of the job to retrieve the details for
     * @return the job details
     */
    JobDetails getJobDetails(String jobId);

    /**
     * Get pipeline state.
     *
     * @param pipelinename
     *            the name of the pipeline to get the state for.
     * @return the list of {@link StageState} objects for the pipeline
     */
    List<StageState> getPipelineState(String pipelinename);

    /**
     * Get the list of pipelines.
     * @return the list of pipelines
     */
    List<String> getPipelines();

    /**
     * Get the inputstream of an artifact for the pipeline.
     *
     * @param bucket
     *            the artifactstore the artifacts are stored in
     * @param objectKey
     *            the object to get
     * @return
     */
    InputStream getPipelineArtifactStream(String bucket, String objectKey);
}
