package ngaclient;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

public class FakeNgaClient implements NgaClient {

	private State state = State.NOT_OPEN;
	private boolean loggedIn = false;
	private boolean sessionExpired = true;

	@Override
	public boolean login() {
		loggedIn = true;
		state = State.OPEN;
		sessionExpired = false;
		return true;
	}

	@Override
	public void logout() {
		if (state != State.OPEN) {
			return;
		}
		loggedIn = false;
		sessionExpired = true;
	}

	@Override
	public void close() throws IOException {
		state = State.CLOSED;
		loggedIn = false;
		sessionExpired = true;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public boolean isLogggedIn() {
		return loggedIn && !sessionExpired;
	}

	void expireSession() {
		sessionExpired = true;
	}

	@Override
	public ServerInfo[] getServers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServerInfo createServer(String instanceId, String name, String url,
			String serverType) throws ClientProtocolException, IOException {
		return null;
	}

	@Override
	public boolean deleteServer(int id) throws ClientProtocolException, IOException {
		return false;
	}

	@Override
	public PipelineInfo[] getPipelines() throws ClientProtocolException, IOException {
		return null;
	}

	@Override
	public PipelineInfo createPipeline(String name, EntityInfo server, JobInfo[] jobIds,
			String rootJobId) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deletePipeline(int id) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PipelineInfo updatePipeline(int id, String name, EntityInfo newRelease)
			throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void recordBuild(BuildInfo buildInfo) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void submitTestResults(InputStream testResult) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub

	}

}