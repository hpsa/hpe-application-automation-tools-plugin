package ngaclient;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class NgaPipelineTest {

	@Test
	public void testGetPipelines() throws ClientProtocolException, IOException {
		try (NgaRestClient ngaClient = new NgaRestClient()) {
			ngaClient.login();
			PipelineInfo[] pipelines = ngaClient.getPipelines();
			for (PipelineInfo piInfo : pipelines) {
				System.out.println(piInfo);
			}
			ngaClient.logout();
			ngaClient.close();
		}
	}

	@Test
	public void testCrudPipelines() throws ClientProtocolException, IOException {
		try (NgaRestClient ngaClient = new NgaRestClient()) {
			ngaClient.login();
			ServerInfo testServer = ngaClient.createServer("JustForThisTest", "JustForThisTest",
					"JustForThisTest", "JustForThisTest");
			PipelineInfo newPipeline = ngaClient.createPipeline("Dodo",
					new EntityInfo(testServer.getId(), "ci_server"),
					new JobInfo[] { new JobInfo("ROOT", "ROOT"), new JobInfo("Leaf", "Leaf") }, "ROOT");
			assertTrue(newPipeline.getName().equals("Dodo"));
			PipelineInfo updatedPipeline = ngaClient.updatePipeline(newPipeline.getId(), "Bobo",
					new EntityInfo(1001, EntityInfo.RELEASE));
			assertTrue(updatedPipeline.getId() == newPipeline.getId());
			assertTrue(updatedPipeline.getName().equals("Bobo"));
			assertTrue(updatedPipeline.getCurrentRelease().getId() == 1001);
			ngaClient.deletePipeline(newPipeline.getId());
			ngaClient.deleteServer(testServer.getId());
			ngaClient.logout();
			ngaClient.close();
		}
	}

}
