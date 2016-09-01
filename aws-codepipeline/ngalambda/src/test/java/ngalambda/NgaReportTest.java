package ngalambda;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.codepipeline.model.ActionContext;
import com.amazonaws.services.codepipeline.model.ActionRevision;
import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.JobData;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineContext;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageContext;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.Context;

import net.jadler.Jadler;
import ngalambda.aws.NgaCodePipelineClient;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class NgaReportTest {

	private static Properties config;

	private static Map<String, ?> input;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void createInput() throws IOException {
		input = TestUtils.parse("/ngalambda/lambda-codepipeline-event.json", Map.class);
		config = new Properties();
		config.put(NgaCodePipelineClient.CLIENT_ID_KEY, "clientId");
		config.put(NgaCodePipelineClient.CLIENT_SECRET_KEY, "clientSecret");
		config.put(NgaCodePipelineClient.NGA_URL_KEY, "http://localhost:8080/ui/?p=1001/1002");
	}

	private NgaCodePipelineClient ngaCodePipeline;

	private Context createContext() {
		final TestContext ctx = new TestContext();

		ctx.setFunctionName("NgaReportAction");
		return ctx;
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		Jadler.initJadlerListeningOn(8080);
		this.ngaCodePipeline = mock(NgaCodePipelineClient.class);
		when(this.ngaCodePipeline.getNgaConfigurationParametersFromJob(any(PipelineDeclaration.class),
				any(JobDetails.class))).thenReturn(config);
		when(this.ngaCodePipeline.getJobDetails(eq("jobId")))
				.thenReturn(new JobDetails().withData(new JobData().withPipelineContext(new PipelineContext()
						.withPipelineName("pipeline").withAction(new ActionContext().withName("action"))
						.withStage(new StageContext().withName("stage")))));
		when(this.ngaCodePipeline.getPipeline(eq("pipeline"))).thenReturn(new PipelineDeclaration());
		when(this.ngaCodePipeline.getPipelineState(eq("pipeline"))).thenReturn(Arrays.asList(new StageState()
				.withStageName("stage").withActionStates(new ActionState().withActionName("action").withCurrentRevision(
						new ActionRevision().withRevisionId("revision").withCreated(new Date(0l))))));
		when(this.ngaCodePipeline.getRevision(any(java.util.List.class))).thenReturn("revision-0");
		Jadler.onRequest().havingMethodEqualTo("POST").havingPathEqualTo("/authentication/sign_in").respond()
				.withStatus(200);
		Jadler.onRequest().havingMethodEqualTo("POST")
				.havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/test-results").respond().withStatus(200);

	}

	@After
	public void tearDown() {
		Jadler.closeJadler();
	}

	@Test
	public void testReportLambdaFunctionHandler() {

		final Context ctx = createContext();
		final NgaReport handler = new NgaReport(ctx, this.ngaCodePipeline);
		when(this.ngaCodePipeline.getPipelineArtifactStream(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(new InputStream() {

					@Override
					public int read() throws IOException {
						return 0;
					}
				});
		final Object output = handler.handleRequest(input, ctx);

		Mockito.verify(this.ngaCodePipeline, Mockito.atLeastOnce()).getPipelineArtifactStream(eq("bucket"),
				eq("pipeline/report/object"));
		Jadler.verifyThatRequest().havingMethodEqualTo("POST")
				.havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/test-results")
				.havingBody(new StringContains("job_id=\"pipeline-stage\"")).receivedOnce();
		if (output != null) {
			System.out.println(output.toString());
		}
	}
}
