package ngalambda;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import ngaclient.BuildInfo;
import ngaclient.JenkinsToNga;
import ngaclient.JobInfo;
import ngaclient.NgaRestClient;
import ngalambda.aws.NgaCodePipelineClient;
import ngalambda.nga.NgaClient;

/**
 * Report action sending the test results received as input artifact (a ZIP file
 * including xml test results) to NGA. This job must be preceded somewhere in
 * the pipeline by an NGA configuration custom action persisting the NGA access
 * details.
 *
 * @author Robert Roth
 *
 */
public class NgaReport extends AbstractNgaLambda {

    @Override
    public Object handleRequest(final Map<String, ?> event, final Context context) {
        setUpFromContext(context);
        this.logger.log(Instant.now() + " Event: " + event);
        @SuppressWarnings("rawtypes")
        final Map jobInfo = (Map) event.get("CodePipeline.job");
        final String jobId = (String) jobInfo.get("id");

        @SuppressWarnings("rawtypes")
        final Map jobData = (Map) jobInfo.get("data");
        @SuppressWarnings("rawtypes")
        final List input = (List) jobData.get("inputArtifacts");
        @SuppressWarnings("rawtypes")
        final Map firstArtifact = (Map) input.get(0);
        @SuppressWarnings("rawtypes")
        final Map location = (Map) firstArtifact.get("location");
        @SuppressWarnings("rawtypes")
        final Map s3Location = (Map) location.get("s3Location");

        final String bucket = String.valueOf(s3Location.get("bucketName"));
        final String objectKey = String.valueOf(s3Location.get("objectKey"));
        this.logger.log(Instant.now() + " Input artifact Bucket name is " + bucket + " and object is " + objectKey);

        final JobDetails details = this.client.getJobDetails(jobId);

        final String pipelineName = details.getData().getPipelineContext().getPipelineName();

        final PipelineDeclaration pipeline = this.client.getPipeline(pipelineName);

        final List<StageState> states = this.client.getPipelineState(pipelineName);

        final String ngaJobName = details.getData().getPipelineContext().getStage().getName();
        final String ngaJobId = getJobId(pipelineName, ngaJobName);
        final String revision = this.client.getRevision(states);
        final String buildId = getBuildId(revision, ngaJobId);
        try (NgaClient ngaClient = getNgaClient(pipeline, details)) {
            try (InputStream testArtifact = this.client.getPipelineArtifactStream(bucket, objectKey)) {
                ngaClient.submitTestResults(testArtifact, buildId, ngaJobId, ngaJobName, Instant.now());
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.client.putJobSuccess(jobId);
        return Boolean.TRUE;
    }

    public NgaReport() {
        super();
    }

    public NgaReport(final Context context, final NgaCodePipelineClient client) {
        super(context, client, new LambdaLogger() {

            @Override
            public void log(final String string) {
                System.out.println(string);
            }
        });
    }

    public static void main(final String... args) throws IOException, SAXException, ParserConfigurationException {
        // final InputStream is = new StringInputStream(INPUT);
        // final JsonNode node = JsonUtils.fromStream(is);
        // System.out.println(JsonUtils.toText(node));
        final AmazonS3Client s3 = new AmazonS3Client().withRegion(Regions.US_WEST_2);
        final S3Object obj = s3.getObject("codepipeline-us-west-2-718358941455", "NGA-pipeline/Reports/1xg19su");
        final ZipInputStream zis = new ZipInputStream(obj.getObjectContent());
        final BuildInfo info = new BuildInfo();
        info.setServerCiId("AWS_CodePipeline");
        info.setBuildCiId("F4m7XhXwiBjEFolZ2FRrQ8Q.Wl14BUj1-1464816827-NGA-pipeline-Test");
        info.setBuildName("2016-06-01 14:09:48");
        info.setJobInfo(new JobInfo("NGA-pipeline-Test", "Test"));
        JenkinsToNga.transform(info, zis, new FileOutputStream("results"));
        final ngaclient.NgaClient client = new NgaRestClient("http://sandbox.almoctane.com:8080/ui/?p=1001/1006",
                "RobertIntegration_o2m5xw0mw9oqqbm5rdp37lv1k", "#d450992fba585abM");
        System.out.println(client.login());
        client.submitTestResults(new FileInputStream("results"));
        client.logout();
        client.close();

    }

}
