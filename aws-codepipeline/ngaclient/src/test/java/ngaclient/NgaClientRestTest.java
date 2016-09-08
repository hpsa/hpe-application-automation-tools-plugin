package ngaclient;

public class NgaClientRestTest extends NgaClientLoginTest {

	@Override
	protected NgaClient createGoodNgaClient() {
		return new NgaRestClient();
	}

	@Override
	protected NgaClient createBadNgaClient() {
		return new NgaRestClient(NgaRestClient.TEST_URL, NgaRestClient.TEST_CLIENT_ID, "NotWelcome");
	}

	@Override
	protected NgaClient createNoNgaClient() {
		return new NgaRestClient("ttt.tttt", "user", "passwd");
	}

	@Override
	protected void expireLoginSession(NgaClient client) {
		((NgaRestClient) client).clearCookies();
	}

}
