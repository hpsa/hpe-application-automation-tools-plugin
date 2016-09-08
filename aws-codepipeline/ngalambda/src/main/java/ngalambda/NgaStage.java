package ngalambda;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;

import ngaclient.BuildInfo;
import ngaclient.JenkinsToNga;
import ngaclient.JobInfo;
import ngaclient.NgaRestClient;
import ngalambda.nga.NgaClient;

/**
 * NGA Lambda to be used in the pipeline for near-real-time stage status
 * reporting, when the scheduled job is not enough. This action must be added to
 * the end of each stage after the stage containing the NGA configuration custom
 * action.
 *
 * @author Robert Roth
 *
 */
public class NgaStage extends AbstractNgaLambda {

    @Override
    public Object handleRequest(final Map<String, ?> event, final Context context) {
        setUpFromContext(context);
        this.logger.log(Instant.now() + " Event: " + event);
        @SuppressWarnings("rawtypes")
        final Map jobInfo = (Map) event.get("CodePipeline.job");
        final String jobId = (String) jobInfo.get("id");

        final JobDetails details = this.client.getJobDetails(jobId);

        final String pipelineName = details.getData().getPipelineContext().getPipelineName();

        final PipelineDeclaration pipeline = this.client.getPipeline(pipelineName);

        try (NgaClient ngaClient = getNgaClient(pipeline, details)) {
            final String currentStage = details.getData().getPipelineContext().getStage().getName();
            markStageAsFinished(pipelineName, currentStage, ngaClient);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        this.client.putJobSuccess(jobId);
        return "Victorie";
    }

    public static void main(final String... args) throws IOException, SAXException, ParserConfigurationException {
        // final InputStream is = new StringInputStream(INPUT);
        // final JsonNode node = JsonUtils.fromStream(is);
        // System.out.println(JsonUtils.toText(node));
        final AmazonS3Client s3 = new AmazonS3Client().withRegion(Regions.US_WEST_2);
        final S3Object obj = s3.getObject("codepipeline-us-west-2-718358941455", "NGA-pipeline/Reports/ijTgCC9");
        final ZipInputStream zis = new ZipInputStream(obj.getObjectContent());
        final BuildInfo info = new BuildInfo();
        info.setServerCiId("AWS_CodePipeline");
        info.setBuildCiId("pPGbcjQq4Vd2vo8ESAuH5D.kZf49oTsi-1464342920-NGA-pipeline-Test");
        info.setBuildName(info.getBuildCiId());
        info.setJobInfo(new JobInfo("NGA-pipeline-Test", "NGA-pipeline-Test"));
        JenkinsToNga.transform(info, zis, new FileOutputStream("results"));
        final ngaclient.NgaClient client = new NgaRestClient("http://code.nextgenalm.com:8080/ui/?p=1001/1002",
                "CodePipeline_p6rmx8e008wqrfdqp7krpxqeg", "?3f85a78dbff877cM");
        System.out.println(client.login());
        client.submitTestResults(new FileInputStream("results"));
        client.logout();
        client.close();

    }

}
