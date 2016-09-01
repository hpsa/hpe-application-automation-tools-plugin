package ngaworker.scheduling;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.codepipeline.AWSCodePipeline;
import com.amazonaws.services.codepipeline.AWSCodePipelineClient;
import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;
import ngaclient.JobInfo;
import ngalambda.aws.NgaCodePipelineClient;
import ngalambda.aws.impl.DefaultCodePipelineClient;
import ngalambda.nga.NgaClient;
import ngalambda.nga.impl.DefaultNgaClient;

public class NgaWorkerApp {

	// Client ID: CodePipeline_p6rmx8e008wqrfdqp7krpxqeg
	// Client secret: ?3f85a78dbff877cM
	// NGA url: http://code.nextgenalm.com:8080/ui/?p=1001/1002

	private static final AWSCodePipeline awsclient = new AWSCodePipelineClient().withRegion(Regions.US_WEST_2);

	private static final AmazonS3 s3client = new AmazonS3Client().withRegion(Regions.US_WEST_2);

    private static final NgaCodePipelineClient client = new DefaultCodePipelineClient(awsclient, s3client,
            new LambdaLogger() {

                @Override
                public void log(final String string) {
                    System.out.println(string);
                }
            });

	// final static NgaRestClient ngaClient = new
	// NgaRestClient("http://code.nextgenalm.com:8080",
	// "CodePipeline_p6rmx8e008wqrfdqp7krpxqeg", "?3f85a78dbff877cM");

	private static final ScheduledExecutorService pollExecutor = Executors.newSingleThreadScheduledExecutor();

	protected static void processJob(final Job job) throws Exception {
		System.out.println("Job ack returned " + client.acknowledgeJob(job.getId(), job.getNonce()));

		syncPipelineToNga(job);
		System.out.println("Put job success for job with id " + job.getId());
		client.putJobSuccess(job.getId());
	}

	private static void syncPipelineToNga(final Job job) throws Exception {
		final String pipelineName = job.getData().getPipelineContext().getPipelineName();

		final PipelineDeclaration pipeline = client.getPipeline(pipelineName);

		final JobDetails details = client.getJobDetails(job.getId());
		final Properties config = client.getNgaConfigurationParametersFromJob(pipeline, details);

		final String clientId = (String) config.get(NgaCodePipelineClient.CLIENT_ID_KEY);
		final String clientSecret = (String) config.get(NgaCodePipelineClient.CLIENT_SECRET_KEY);
		final String ngaFullUrl = (String) config.get(NgaCodePipelineClient.NGA_URL_KEY);

		final List<JobInfo> stages = new ArrayList<JobInfo>(pipeline.getStages().size());
		for (final StageDeclaration stage : pipeline.getStages()) {
			System.out.println("Processing stage " + stage.getName());
			stages.add(new JobInfo(pipelineName + "-" + stage.getName(), stage.getName()));
		}

		final List<StageState> states = client.getPipelineState(pipelineName);
		try (NgaClient ngaClient = new DefaultNgaClient(ngaFullUrl, clientId, clientSecret)) {
			ngaClient.getCiServerId();
			ngaClient.syncPipeline(pipelineName, stages);
			ngaClient.updateBuildStatus(stages.get(0).getId(), client.getRevision(states) + "-" + stages.get(0).getId(),
                    BuildStatus.FINISHED, Instant.now(), BuildResult.SUCCESS, 0, null, null);
			ngaClient.updateBuildStatus(stages.get(1).getId(), client.getRevision(states) + "-" + stages.get(1).getId(),
                    BuildStatus.RUNNING, Instant.now(), BuildResult.UNAVAILABLE, 0, null, null);
		}

	}

	final static Runnable POLLING_RUNNABLE = new Runnable() {

		@Override
		public void run() {
			final List<Job> jobs = client.pollForJobs(ActionCategory.Build, ActionOwner.Custom, "NextGenALM", "1");

			System.out.println("Polling for jobs");
			for (final Job job : jobs) {
				try {
					processJob(job);

				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private static final long POLLING_INTERVAL = 10;

	public static void main(final String[] args) throws IOException {
		pollExecutor.scheduleAtFixedRate(POLLING_RUNNABLE, 0, POLLING_INTERVAL, TimeUnit.SECONDS);

		// final S3Object object =
		// s3client.getObject("codepipeline-us-west-2-718358941455",
		// "NGA-pipeline" + "/" + NgaCodePipelineClient.NGA_CONFIG_FILE);
		// try (BufferedReader reader = new BufferedReader(new
		// InputStreamReader(object.getObjectContent()))) {
		// String line = null;
		// while ((line = reader.readLine()) != null) {
		// System.out.println(line);
		// }
		// }
		// final String clientId = "CodePipeline_p6rmx8e008wqrfdqp7krpxqeg";
		// final String clientSecret = "?3f85a78dbff877cM";
		// final String ngaFullUrl =
		// "http://code.nextgenalm.com:8080/ui/?p=1001/1002";
		//
		// final String[] split = ngaFullUrl.split("[?&]");
		//
		// final String ngaUrl = split[0].replaceAll("/\\w+/", "");
		//
		// System.out.println("Using id " + clientId);
		// System.out.println("Using secret " + clientSecret);
		// System.out.println("Using url " + ngaUrl);
	}

}
