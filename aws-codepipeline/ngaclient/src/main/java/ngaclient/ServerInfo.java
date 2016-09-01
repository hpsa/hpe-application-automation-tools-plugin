package ngaclient;

import static ngaclient.JsonUtils.readBoolean;
import static ngaclient.JsonUtils.readInt;
import static ngaclient.JsonUtils.readString;
import static ngaclient.JsonUtils.toText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * POJO which holds information about a build server
 * @author Romulus Pa&#351;ca
 *
 */
public class ServerInfo {
	private static final ThreadLocal<DateTimeFormatter> formatter = new ThreadLocal<DateTimeFormatter>() {
		@Override
		protected DateTimeFormatter initialValue() {
			return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}
	};

	private LocalDateTime creationTime;
	private boolean isConnected;
	private String instanceId;
	private int versionStamp;
	private String name;
	private int id;
	private LocalDateTime lastModified;
	private String url;
	private String serverType;

	/**
	 * Create a ServeInfo object from it's JSON representation
	 * @param jsonNode - the JSON node from which the server information is read
	 * @return a ServeInfo with information read from the given JSON Object
	 */
	public static ServerInfo fromJson(ObjectNode jsonNode) {
		ServerInfo server = new ServerInfo();
		String type = readString(jsonNode, "type");
		if (!type.equals("ci_server")) {
			throw new RuntimeException("Invalid json node for a server " + toText(jsonNode));
		}
		server.setCreationTime(LocalDateTime.parse(readString(jsonNode, "creation_time"), formatter.get()));
		server.setConnected(readBoolean(jsonNode, "is_connected"));
		server.setInstanceId(readString(jsonNode, "instance_id"));
		server.setVersionStamp(readInt(jsonNode, "version_stamp"));
		server.setName(readString(jsonNode, "name"));
		server.setId(readInt(jsonNode, "id"));
		server.setLastModified(LocalDateTime.parse(readString(jsonNode, "last_modified"), formatter.get()));
		server.setUrl(readString(jsonNode, "url"));
		server.setServerType(readString(jsonNode, "server_type"));
		return server;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public int getVersionStamp() {
		return versionStamp;
	}

	public void setVersionStamp(int versionStamp) {
		this.versionStamp = versionStamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	@Override
	public String toString() {
		return "ServerInfo [instanceId=" + instanceId + ", name=" + name + ", url=" + url + "]";
	}

}
