package ngaclient;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.ClientProtocolException;

/**
 * This interface defines the behavior required by a client for the NGA REST API. The client it is
 * a statefull object, which can be NOT_OPEN, OPEN or CLOSED. Once created the client will be open after a sucessfull
 * call of the login method. The client will get closed after calling the close method. However after calling logout,
 * or having the current session expired, a new login will be required
 * 
 * @author Romulus Pa&#351;ca
 *
 */
public interface NgaClient extends Closeable {

	/**
	 * The state of the client
	 */
	public enum State {
		NOT_OPEN, OPEN, CLOSED;
	}

	/**
	 * Login operation
	 * @return <code>true</code> if succeeds <code>false otherwise</code>
	 */
	boolean login() throws ClientProtocolException, IOException;

	void logout() throws ClientProtocolException, IOException;

	State getState();

	boolean isLogggedIn();

	ServerInfo createServer(String instanceId, String name, String url,
			String serverType) throws ClientProtocolException, IOException;

	ServerInfo[] getServers() throws ClientProtocolException, IOException;

	boolean deleteServer(int id) throws ClientProtocolException, IOException;

	PipelineInfo[] getPipelines() throws ClientProtocolException, IOException;

	PipelineInfo createPipeline(String name, EntityInfo server, JobInfo[] jobIds, String rootJobId)
			throws ClientProtocolException, IOException;

	boolean deletePipeline(int id) throws ClientProtocolException, IOException;

	PipelineInfo updatePipeline(int id, String name, EntityInfo newRelease)
			throws ClientProtocolException, IOException;

	void recordBuild(BuildInfo buildInfo) throws ClientProtocolException, IOException;

	void submitTestResults(InputStream testResult) throws ClientProtocolException, IOException;
}