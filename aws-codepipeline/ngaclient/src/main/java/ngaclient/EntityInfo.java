package ngaclient;

import static ngaclient.JsonUtils.readInt;
import static ngaclient.JsonUtils.readString;
import static ngaclient.JsonUtils.toText;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *  A simple pair structure with an int and a String fields used to represent various entities
 *  required by NGA REST API, such CiJob, CiServer or PipelineNode
 * 
 *  @author Romulus Pa&#351;ca
 *
 */
public class EntityInfo {
	public static final String RELEASE = "release";
	public static final String CURRENT_RELEASE = "current_release";
	public static final String CI_JOB = "ci_job";
	public static final String CI_SERVER = "ci_server";
	public static final String PIPELINE_MODEL = "pipeline_model";
	public static final String PIPELINE_NODE = "pipeline_node";

	private final int id;
	private final String entityType;

	public EntityInfo(int id, String entityType) {
		this.id = id;
		if (entityType == null || entityType.trim().length() == 0) {
			throw new IllegalArgumentException("Entity type cannot be null or blank string");
		}
		this.entityType = entityType;
	}

	/**
	 * A factory method to create an EntityInfo from it's JSON representation
	 * @param expectedType the expected entity type
	 * @param objNode the JSON object which contains the data for an entity
	 * @return a new EnitiyInfo object of the given type, with date initialized from the given JSON object
	 * @throws IllegalArgumentException if the provided JSON object does not have an entity of the given type
	 */
	public static EntityInfo newByType(String expectedType, ObjectNode objNode) {
		String crtType = readString(objNode, "type");
		if (crtType.equals(expectedType)) {
			return new EntityInfo(readInt(objNode, "id"), crtType);
		} else {
			throw new IllegalArgumentException("Invalid json for type " + expectedType + " " + toText(objNode));
		}
	}

	public int getId() {
		return id;
	}

	public String getEntityType() {
		return entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (entityType == null ? 0 : entityType.hashCode());
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EntityInfo other = (EntityInfo) obj;
		if (!entityType.equals(other.entityType)) {
			return false;
		}
		if (id != other.id) {
			return false;
		}
		return true;
	}

	/**
	 * Serialize the current entity in JSOn format
	 * @param node - a JSON root object, on which the current entity info will be serialized
	 * @return An JSON object which contains this entity info
	 */
	public ObjectNode toJsonNode(ObjectNode node) {
		node.put("type", getEntityType());
		node.put("id", getId());
		return node;
	}

}
