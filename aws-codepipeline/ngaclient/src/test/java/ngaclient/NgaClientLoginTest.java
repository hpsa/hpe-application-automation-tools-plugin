package ngaclient;

import static ngaclient.NgaClient.State.CLOSED;
import static ngaclient.NgaClient.State.NOT_OPEN;
import static ngaclient.NgaClient.State.OPEN;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public abstract class NgaClientLoginTest {

	protected abstract NgaClient createGoodNgaClient();

	protected abstract NgaClient createBadNgaClient();

	protected abstract NgaClient createNoNgaClient();

	protected abstract void expireLoginSession(NgaClient client);

	@Test
	public void basicTest() throws IOException {
		try (NgaClient ngaClient = createGoodNgaClient()) {
			assertTrue(ngaClient.getState() == NOT_OPEN);
			ngaClient.login();
			assertTrue(ngaClient.getState() == OPEN);
			ngaClient.logout();
			assertTrue(!ngaClient.isLogggedIn());
			ngaClient.close();
			assertTrue(ngaClient.getState() == CLOSED);
		}
	}

	@Test
	public void testAfterCokiesExpire() throws IOException {
		try (NgaClient ngaClient = createGoodNgaClient()) {
			assertTrue(ngaClient.getState() == NOT_OPEN);
			ngaClient.login();
			assertTrue(ngaClient.getState() == OPEN);
			expireLoginSession(ngaClient);
			assertTrue(!ngaClient.isLogggedIn());
		}
	}

	@Test
	public void testLoginWrongCredentials() throws ClientProtocolException, IOException {
		try (NgaClient ngaClient = createBadNgaClient()) {
			assertTrue(!ngaClient.login());
		}
	}

	@Test
	public void testLogoutWhitoutLogin() throws ClientProtocolException, IOException {
		try (NgaClient ngaClient = createGoodNgaClient()) {
			ngaClient.logout();
		}
	}

	@Test
	public void testRepetedLogin() throws ClientProtocolException, IOException {
		try (NgaClient ngaClient = createGoodNgaClient()) {
			assertTrue(ngaClient.getState() == NOT_OPEN);
			for (int i = 0; i < 10; i++) {
				ngaClient.login();
				assertTrue(ngaClient.getState() == OPEN);
			}
		}
	}

	@Test(expected = IOException.class)
	public void loginToADownServer() throws ClientProtocolException, IOException {
		try (NgaClient ngaClient = createNoNgaClient()) {
			ngaClient.login();
		}
	}

	@Test
	public void logoutToADownServer() throws ClientProtocolException, IOException {
		try (NgaClient ngaClient = createNoNgaClient()) {
			ngaClient.logout();
		}
	}

	public void loginThenServerTimeout() {

	}
}
