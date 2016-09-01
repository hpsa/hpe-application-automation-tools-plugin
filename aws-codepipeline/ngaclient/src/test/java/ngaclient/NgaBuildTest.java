package ngaclient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import ngaclient.BuildInfo.BuildResult;
import ngaclient.BuildInfo.BuildStatus;

public class NgaBuildTest {

	/**
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	@Test
	public void testRecordBuild() throws ClientProtocolException, IOException {
		try (NgaRestClient ngaClient = new NgaRestClient()) {
			ngaClient.login();
			BuildInfo buildInfo = new BuildInfo();
			buildInfo.setServerCiId("1001");
			buildInfo.setJobInfo(new JobInfo("1001"));
			buildInfo.setBuildCiId("1001");
			buildInfo.setStartTime(Instant.now());
			buildInfo.setDuration(Duration.ofMinutes(2));
			buildInfo.setStatus(BuildStatus.FINISHED);
			buildInfo.setResult(BuildResult.SUCCESS);
			buildInfo.setCauses(new BuildCause[] { new BuildCause("1002", "1002") });
			ngaClient.recordBuild(buildInfo);
			ngaClient.logout();
			ngaClient.close();
		}
	}

}
