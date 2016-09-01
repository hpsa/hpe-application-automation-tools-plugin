package ngaclient;

import static ngaclient.JsonUtils.getArray;
import static ngaclient.JsonUtils.getObject;
import static ngaclient.JsonUtils.readInt;
import static ngaclient.JsonUtils.readString;
import static ngaclient.JsonUtils.toText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A POJO used to store information realted to a pipeline
 * @author Romulus Pa&#351;ca
 *
 */
public class PipelineInfo {

	private static final ThreadLocal<DateTimeFormatter> formatter = new ThreadLocal<DateTimeFormatter>() {
		@Override
		protected DateTimeFormatter initialValue() {
			return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		}
	};

	private LocalDateTime creationTime;
	private String name;
	private int id;
	private LocalDateTime lastModified;
	private int versionStamp;
	private EntityInfo currentRelease;
	private EntityInfo rootJob;
	private EntityInfo ciServer;
	private EntityInfo currentModel;
	private EntityInfo rootNode;
	private EntityInfo[] nodes;

	public PipelineInfo() {

	}

	/**
	 * Builds a pipeline from it's JSON representation
	 * @param jsonNode - a JSON node that contains information about a pipeline
	 * @return - the pipeline read from it's serialized JSOn format
	 * @throws  IllegalArgumentException  if the JSON node does not describes a pipeline
	 */
	public static PipelineInfo fromJson(ObjectNode jsonNode) {
		PipelineInfo pipeline = new PipelineInfo();
		String type = readString(jsonNode, "type");
		if (!type.equals("pipeline")) {
			throw new IllegalArgumentException("Invalid json node for a server " + toText(jsonNode));
		}
		pipeline.setCreationTime(LocalDateTime.parse(readString(jsonNode, "creation_time"), formatter.get()));
		pipeline.setName(readString(jsonNode, "name"));
		pipeline.setId(readInt(jsonNode, "id"));
		pipeline.setLastModified(LocalDateTime.parse(readString(jsonNode, "last_modified"), formatter.get()));
		pipeline.setVersionStamp(readInt(jsonNode, "version_stamp"));

		ObjectNode objNode = getObject(jsonNode, EntityInfo.CURRENT_RELEASE);
		if (objNode != null) {
			pipeline.setCurrentRelease(EntityInfo.newByType(EntityInfo.RELEASE, objNode));
		}
		objNode = getObject(jsonNode, "root_job");
		if (objNode != null) {
			pipeline.setRootJob(EntityInfo.newByType(EntityInfo.CI_JOB, objNode));
		}
		objNode = getObject(jsonNode, "ci_server");
		if (objNode != null) {
			pipeline.setCiServer(EntityInfo.newByType(EntityInfo.CI_SERVER, objNode));
		}
		objNode = getObject(jsonNode, "current_model");
		if (objNode != null) {
			pipeline.setCurrentModel(EntityInfo.newByType(EntityInfo.PIPELINE_MODEL, objNode));
		}
		objNode = getObject(jsonNode, "root_job");
		if (objNode != null) {
			pipeline.setCurrentModel(EntityInfo.newByType(EntityInfo.CI_JOB, objNode));
		}
		objNode = getObject(jsonNode, "nodes");
		if (objNode != null) {
			int nodesCount = readInt(objNode, "total_count");
			pipeline.nodes = new EntityInfo[nodesCount];
			ArrayNode data = getArray(objNode, "data");
			for (int i = 0; i < nodesCount; i++) {
				ObjectNode dataItem = (ObjectNode) data.get(i);
				pipeline.nodes[i] = EntityInfo.newByType(EntityInfo.PIPELINE_NODE, dataItem);
			}

		}
		return pipeline;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntityInfo getCurrentRelease() {
		return currentRelease;
	}

	public void setCurrentRelease(EntityInfo currentRelease) {
		this.currentRelease = currentRelease;
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

	public int getVersionStamp() {
		return versionStamp;
	}

	public void setVersionStamp(int versionStamp) {
		this.versionStamp = versionStamp;
	}

	public EntityInfo getRootJob() {
		return rootJob;
	}

	public void setRootJob(EntityInfo rootJob) {
		this.rootJob = rootJob;
	}

	public EntityInfo getCiServer() {
		return ciServer;
	}

	public void setCiServer(EntityInfo ciServer) {
		this.ciServer = ciServer;
	}

	public EntityInfo getCurrentModel() {
		return currentModel;
	}

	public void setCurrentModel(EntityInfo currentModel) {
		this.currentModel = currentModel;
	}

	public EntityInfo getRootNode() {
		return rootNode;
	}

	public void setRootNode(EntityInfo rootNode) {
		this.rootNode = rootNode;
	}

	public EntityInfo[] getNodes() {
		return nodes;
	}

	public void setNodes(EntityInfo[] nodes) {
		this.nodes = nodes;
	}

	@Override
	public String toString() {
		return "PipelineInfo [name=" + name + ", id=" + id + ", versionStamp=" + versionStamp + "]";
	}

}
