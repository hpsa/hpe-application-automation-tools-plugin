package ngaclient;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class NgaSubmitTestResultsTest {

	@Test
	public void basicTest() throws IOException {

		try (NgaRestClient ngaClient = new NgaRestClient();
				InputStream t1 = getClass().getResourceAsStream("/ngaclient/test-result1.xml")) {
			ngaClient.login();
			ngaClient.submitTestResults(t1);
			ngaClient.logout();
			ngaClient.close();
		}
	}

}
