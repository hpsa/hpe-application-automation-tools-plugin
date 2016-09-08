package ngalambda.aws;

import java.util.List;
import java.util.Properties;

import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;

/**
 * NGA-specific extension of the generic {@link CodePipelineClient} interface.
 *
 * @author Robert Roth
 *
 */
public interface NgaCodePipelineClient extends CodePipelineClient {
    /** The provider used in the custom action. */
    String NGA_PROVIDER = "NextGenALM";
    /**
     * The name of the configuration file in the codepipeline bucket's pipeline
     * folder.
     */
    String NGA_CONFIG_FILE = "nextgenalm-cfg.properties";
    /** The temporary path to use. */
    String NGA_CONFIG_PATH = "/tmp/";
    /**
     * The key to the client id, as specified in the custom action description.
     */
    String CLIENT_ID_KEY = "Client id";
    /**
     * The key for the client secret, as specified in the custom action
     * description.
     */
    String CLIENT_SECRET_KEY = "Client secret";
    /**
     * The key for the server url, as specified in the custom action
     * description.
     */
    String NGA_URL_KEY = "Server-URL";

    /**
     * Get NGA access configuration for a given pipeline and a given job. In
     * case the current job is an NGA-configuration job, saves the NGA access
     * details to the pipeline's s3 folder, otherwise it loads the config from
     * the folder the config action has saved it into.
     *
     * @param pipeline
     *            the pipeline to get the configuration for
     * @param job
     *            information about the job requesting the configuration details
     * @return the {@link Properties} object containing the NGA configuration
     *         details, {@link #NGA_URL_KEY}, {@link #CLIENT_ID_KEY} and
     *         {@link #CLIENT_SECRET_KEY} values.
     */
    Properties getNgaConfigurationParametersFromJob(PipelineDeclaration pipeline, JobDetails job);

    /**
     * Get the revision id from the pipeline state, to be used as part of the
     * unique build identifier.
     *
     * @param states
     *            the pipeline state
     * @return a {@link String} uniquely identifying the revision.
     */
    String getRevision(List<StageState> states);

}
