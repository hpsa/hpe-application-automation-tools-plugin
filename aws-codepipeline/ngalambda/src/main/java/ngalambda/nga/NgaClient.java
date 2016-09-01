package ngalambda.nga;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;
import ngaclient.JobInfo;

/**
 * Client interface for interacting with NGA from lambda function.
 *
 * @author Robert Roth
 *
 */
public interface NgaClient extends AutoCloseable {

    /**
     * The CI server name used for the integration.
     */
    String CODEPIPELINE_CI_SERVER = "AWS CodePipeline";
    /**
     * The CI server id used for the integration.
     */
    String CODEPIPELINE_CI_SERVER_ID = CODEPIPELINE_CI_SERVER.replaceAll(" ", "_");
    /**
     * The CI server url used for the integration.
     */
    String CODEPIPELINE_URL = "https://console.aws.amazon.com/codepipeline/home";

    /**
     * Get the id of the CodePipeline CI server. If it does not exist, create
     * it.
     *
     * @return the id of the codepipeline server
     * @throws IOException
     */
    public int getCiServerId() throws IOException;

    /**
     * Sync the pipeline from codepipeline to NGA.
     *
     * @param name
     *            the name of the pipeline
     * @param stages
     *            the information about the jobs of the pipeline
     * @return the id of the pipeline
     * @throws IOException
     */
    public int syncPipeline(String name, List<JobInfo> stages) throws IOException;

    public ngaclient.NgaClient getClient();

    /**
     * Put the build status for a given job and given build context.
     * @param job the job to push the build status for
     * @param build the build id we are pushing the status for
     * @param status the status to push
     * @param startTime the start tim
     * @param result
     * @param duration
     * @param causedByJob
     * @param causedByBuild
     * @throws IOException
     */
    public void updateBuildStatus(String job, String build, BuildStatus status, Instant startTime, BuildResult result,
            long duration, final String causedByJob, final String causedByBuild) throws IOException;

    /**
     * Submit test results to NGA.
     * 
     * @param input
     *            the test results stream
     * @param buildId
     *            the id of the build to submit the results to
     * @param jobId
     *            the id of the job to submit the results for
     * @param jobName
     *            the name of the job to submit the results for
     * @param startTime
     *            the start time of the test execution
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void submitTestResults(InputStream input, String buildId, String jobId, String jobName, Instant startTime)
            throws IOException, SAXException, ParserConfigurationException;

}
