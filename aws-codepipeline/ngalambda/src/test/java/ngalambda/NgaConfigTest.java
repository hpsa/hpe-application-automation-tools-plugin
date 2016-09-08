package ngalambda;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.hamcrest.core.StringContains;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.codepipeline.model.ActionCategory;
import com.amazonaws.services.codepipeline.model.ActionOwner;
import com.amazonaws.services.codepipeline.model.ActionState;
import com.amazonaws.services.codepipeline.model.Job;
import com.amazonaws.services.codepipeline.model.JobData;
import com.amazonaws.services.codepipeline.model.JobDetails;
import com.amazonaws.services.codepipeline.model.PipelineContext;
import com.amazonaws.services.codepipeline.model.PipelineDeclaration;
import com.amazonaws.services.codepipeline.model.StageContext;
import com.amazonaws.services.codepipeline.model.StageDeclaration;
import com.amazonaws.services.codepipeline.model.StageState;
import com.amazonaws.services.lambda.runtime.Context;

import net.jadler.Jadler;
import ngalambda.aws.NgaCodePipelineClient;

public class NgaConfigTest {

    private static Properties config;

    static {
        config = new Properties();
        config.put(NgaCodePipelineClient.CLIENT_ID_KEY, "clientId");
        config.put(NgaCodePipelineClient.CLIENT_SECRET_KEY, "clientSecret");
        config.put(NgaCodePipelineClient.NGA_URL_KEY, "http://localhost:8080/ui/?p=1001/1002");
    }

    NgaConfig configAction;
    Context context;
    NgaCodePipelineClient client;

    @Before
    public void setUp() throws Exception {
        Jadler.initJadlerListeningOn(8080);
        this.context = Mockito.mock(Context.class);
        this.client = Mockito.mock(NgaCodePipelineClient.class);
        when(this.context.getInvokedFunctionArn()).thenReturn("lambda:::us-west-2::");

        this.configAction = new NgaConfig(this.context, this.client);
        Jadler.onRequest().havingMethodEqualTo("POST").havingPathEqualTo("/authentication/sign_in").respond()
                .withStatus(200);
        Jadler.onRequest().havingMethodEqualTo("GET")
                .havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/ci_servers").respond().withStatus(200)
                .withBody(
                        "{ \"total_count\":1, \"data\":[{ \"name\":\"AWS CodePipeline\",\"id\":1, \"type\":\"ci_server\",\"creation_time\":\"2016-06-01T00:00:00Z\", \"last_modified\":\"2016-06-01T00:00:00Z\"}]}");
        Jadler.onRequest().havingMethodEqualTo("POST")
                .havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/pipelines").respond().withStatus(200)
                .withBody(
                        "{ \"total_count\":1, \"data\":[{ \"name\":\"pipeline\",\"id\":1, \"type\":\"pipeline\",\"creation_time\":\"2016-06-01T00:00:00Z\", \"last_modified\":\"2016-06-01T00:00:00Z\"}]}");
        Jadler.onRequest().havingMethodEqualTo("PUT")
                .havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/analytics/ci/builds").respond()
                .withStatus(200);
    }

    @Test
    public void testSetUpFromContext() {

        this.configAction.isSetUp = false;
        this.configAction.setUpFromContext(this.context);

        assertThat("lambda not set up correctly", this.configAction.isSetUp, is(true));
        assertThat("lambda does not have the NGA client set", this.configAction.client, not(nullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoJobsFound() {
        when(this.client.pollForJobs(eq(ActionCategory.Build), eq(ActionOwner.Custom), eq("NextGenALM"), eq("5")))
                .thenReturn(Collections.<Job> emptyList());
        this.configAction.handleRequest(null, this.context);
        verify(this.client).pollForJobs(eq(ActionCategory.Build), eq(ActionOwner.Custom), eq("NextGenALM"), eq("5"));
        verifyNoMoreInteractions(this.client);
    }

    @Test
    public void testJobFound() {
        final Job job = new Job().withId("job").withNonce("3").withData(new JobData().withPipelineContext(
                new PipelineContext().withPipelineName("pipeline").withStage(new StageContext().withName("stage"))));
        when(this.client.pollForJobs(eq(ActionCategory.Build), eq(ActionOwner.Custom), eq("NextGenALM"), eq("5")))
                .thenReturn(Arrays.asList(job));
        when(this.client.getPipelineState(eq("pipeline"))).thenReturn(Arrays.asList(
                new StageState().withStageName("stage").withActionStates(new ActionState().withActionName("action"))));
        when(this.client.getPipeline(eq("pipeline"))).thenReturn(
                new PipelineDeclaration().withName("pipeline").withStages(new StageDeclaration().withName("stage")));
        when(this.client.getJobDetails(eq(job.getId()))).thenReturn(new JobDetails());
        when(this.client.getNgaConfigurationParametersFromJob(any(PipelineDeclaration.class), any(JobDetails.class)))
                .thenReturn(config);
        when(this.client.getRevision(Mockito.anyList())).thenReturn("revision");
        this.configAction.handleRequest(null, this.context);
        verify(this.client).pollForJobs(eq(ActionCategory.Build), eq(ActionOwner.Custom), eq("NextGenALM"), eq("5"));
        verify(this.client).acknowledgeJob(eq(job.getId()), eq(job.getNonce()));
        verify(this.client).putJobSuccess(eq(job.getId()));
        verify(this.client).getJobDetails(eq(job.getId()));
        verify(this.client).getPipelineState(eq("pipeline"));
        verify(this.client).getRevision(Mockito.anyList());
        verify(this.client).getPipeline(eq("pipeline"));
        verify(this.client).getNgaConfigurationParametersFromJob(any(PipelineDeclaration.class), any(JobDetails.class));
        Jadler.verifyThatRequest().havingMethodEqualTo("POST")
                .havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/pipelines")
                .havingBody(new StringContains("pipeline-stage")).receivedOnce();
        Jadler.verifyThatRequest().havingMethodEqualTo("PUT")
                .havingPathEqualTo("/api/shared_spaces/1001/workspaces/1002/analytics/ci/builds")
                .havingBody(new StringContains("revision-pipeline-stage")).receivedOnce();
        verifyNoMoreInteractions(this.client);

    }

    @After
    public void tearDown() {
        Jadler.closeJadler();
    }
}
