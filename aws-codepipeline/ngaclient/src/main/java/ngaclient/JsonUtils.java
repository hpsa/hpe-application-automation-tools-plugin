package ngaclient;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An utility class to simplify work with Jackson library used for JSOn serialization by this project.
 * Most of the details of working with the Jackson library can't be ignored by using this class.
 * @author Romulus Pa&#351;ca
 *
 */
public final class JsonUtils {

	private JsonUtils() {
		throw new UnsupportedOperationException("This class can't be instantiated");
	}

	private final static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Reads a complete JSON object from a stream
	 */
	public static JsonNode fromStream(InputStream inStream) throws IOException {
		return mapper.readTree(inStream);
	}

	/**
	 * Creates a new JSON object which may be the root of a REST request or could be attached to an already created JSON object.
	 */
	public static ObjectNode newObject() {
		return new ObjectNode(JsonNodeFactory.instance);
	}

	/**
	 *	Creates a new JSON array
	 */
	public static ArrayNode newArray() {
		return new ArrayNode(JsonNodeFactory.instance);
	}

	/**
	 * Transforms an existing JSON object to it's text JSON representation
	 * @param objectNode - the JSON object
	 * @return - it's JSON representation as String
	 */
	public static String toText(JsonNode objectNode) {
		try {
			return mapper.writeValueAsString(objectNode);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads an object with the given key from the given JSON root object
	 * @param objectNode - the root object to read from
	 * @param key - the key of the json node to read
	 * @return - the JSON object with the given key from the root object, or null if no object exist with that key
	 * @throws ClassCastException if the JSON node of the given key has an unexpected type
	 */
	public static ObjectNode getObject(ObjectNode objectNode, String key) {
		JsonNode objNode = objectNode.get(key);
		if (objNode != null && !objNode.isNull()) {
			if (objNode.isObject()) {
				return (ObjectNode) objNode;
			} else {
				throw new ClassCastException("Invalid JSON ObjectNode " + objNode.toString());
			}
		} else {
			return null;
		}
	}

	/**
	 * Reads an array with the given key from the given JSON root object
	 * @param objectNode - the root object to read from
	 * @param key - the key of the json node to read
	 * @return - the JSON array with the given key from the root object, or null if no json node exists with that key
	 * @throws ClassCastException if the JSON node of the given key has an unexpected type
	 */
	public static ArrayNode getArray(ObjectNode objectNode, String key) {
		JsonNode arrayNode = objectNode.get(key);
		if (arrayNode != null) {
			if (arrayNode.isArray()) {
				return (ArrayNode) arrayNode;
			} else {
				throw new ClassCastException("Invalid JSON ArrayNode " + arrayNode.toString());
			}
		} else {
			return null;
		}
	}

	/**
	 * Reads an int with the given key from the given JSON root object
	 * @param objectNode - the root object to read from
	 * @param key - the key of the json node to read
	 * @return - the int value with the given key from the root object, or zero if no json node exists with that key
	 * @throws ClassCastException if the JSON node of the given key has an unexpected type
	 */
	public static int readInt(ObjectNode jsonNode, String key) {
		JsonNode numberNode = jsonNode.get(key);
		if (numberNode != null) {
			if (numberNode.isNumber()) {
				return numberNode.asInt();
			} else {
				throw new ClassCastException("Key " + key + " " + "doesn't hold a numeric type: " + numberNode.toString());
			}
		} else {
			return 0;
		}
	}

	/**
	 * Reads an String with the given key from the given JSON root object
	 * @param objectNode - the root object to read from
	 * @param key - the key of the json node to read
	 * @return - the String value with the given key from the root object, or null if no json node exists with that key
	 * @throws ClassCastException if the JSON node of the given key has an unexpected type
	 */
	public static String readString(ObjectNode jsonNode, String key) {
		JsonNode strNode = jsonNode.get(key);
		if (strNode != null) {
			if (strNode.isTextual()) {
				return strNode.asText();
			} else {
				throw new ClassCastException("Key " + key + " " + "doesn't hold a string type: " + strNode.toString());
			}
		} else {
			return null;
		}
	}

	/**
	 * Reads an boolean with the given key from the given JSON root object
	 * @param objectNode - the root object to read from
	 * @param key - the key of the json node to read
	 * @return - the boolean value with the given key from the root object, or false if no json node exists with that key
	 * @throws ClassCastException if the JSON node of the given key has an unexpected type
	 */
	public static boolean readBoolean(ObjectNode jsonNode, String key) {
		JsonNode boolNode = jsonNode.get(key);
		if (boolNode != null) {
			if (boolNode.isBoolean()) {
				return boolNode.asBoolean();
			} else {
				throw new ClassCastException("Key " + key + " " + "doesn't hold a boolean type: " + boolNode.toString());
			}
		} else {
			return false;
		}
	}
}
