package ngaclient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import ngaclient.NgaClient.State;

public class NgaClientMockTest extends NgaClientLoginTest {

	@Override
	protected NgaClient createGoodNgaClient() {
		return new FakeNgaClient();
	}

	@Override
	protected NgaClient createBadNgaClient() {
		NgaClient mockClient = mock(NgaClient.class);
		try {
			when(mockClient.login()).thenReturn(false);
			when(mockClient.isLogggedIn()).thenReturn(false);
			when(mockClient.getState()).thenReturn(State.NOT_OPEN);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return mockClient;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected NgaClient createNoNgaClient() {
		NgaClient mockClient = mock(NgaClient.class);
		try {
			when(mockClient.login()).thenThrow(IOException.class);
			when(mockClient.isLogggedIn()).thenReturn(false);
			// doThrow(IOException.class).when(mockClient).logout();
			when(mockClient.getState()).thenReturn(State.NOT_OPEN);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return mockClient;
	}

	@Override
	protected void expireLoginSession(NgaClient client) {
		if (client instanceof FakeNgaClient) {
			((FakeNgaClient) client).expireSession();
		} else {
			when(client.isLogggedIn()).thenReturn(false);
		}
	}

}
