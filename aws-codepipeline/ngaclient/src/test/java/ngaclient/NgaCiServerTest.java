package ngaclient;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class NgaCiServerTest {

	@Test
	public void testServerWorkflow() throws ClientProtocolException, IOException {
		try (NgaRestClient ngaClient = new NgaRestClient()) {
			ngaClient.login();
			ServerInfo newServer = ngaClient.createServer("JustForThisTest", "JustForThisTest",
					"JustForThisTest", "JustForThisTest");
			ServerInfo[] servers = ngaClient.getServers();
			boolean found = false;
			for (ServerInfo sinfo : servers) {
				if (sinfo.getName().equals(newServer.getName())) {
					found = true;
					break;
				}
			}
			assertTrue(found);
			ngaClient.deleteServer(newServer.getId());
			servers = ngaClient.getServers();
			found = false;
			for (ServerInfo sinfo : servers) {
				if (sinfo.getName().equals(newServer.getName())) {
					found = true;
					break;
				}
			}
			assertTrue(!found);
			ngaClient.logout();
			ngaClient.close();
		}
	}
}
