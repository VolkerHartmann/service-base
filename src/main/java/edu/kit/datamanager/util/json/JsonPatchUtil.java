// src/main/java/edu/kit/datamanager/util/json/JsonPatchUtil.java
package edu.kit.datamanager.util.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
public class JsonPatchUtil {
  /** JSON Patch field names */
  public static final String OP_FIELD = "op";;
  public static final String PATH_FIELD = "path";;
  public static final String FROM_FIELD = "from";;
  public static final String VALUE_FIELD = "value";;
  /** JSON Patch operation names */
  public static final String ADD_OP = "add";
  public static final String REMOVE_OP = "remove";
  public static final String REPLACE_OP = "replace";
  public static final String MOVE_OP = "move";
  public static final String COPY_OP = "copy";
  public static final String TEST_OP = "test";

  private static final Logger logger = LoggerFactory.getLogger(JsonPatchUtil.class);

  private final JsonMapper mapper;

  public JsonPatchUtil() {
    this.mapper = JsonMapper.builder().build();
  }

  public static JsonNode applyPatch(JsonNode original, JsonPatch patch) {
    JsonMapper mapper = JsonMapper.builder().build();
    JsonNode patchNode = mapper.valueToTree(patch);
    logger.trace("Applying patch '{}' to original: '{}'", patchNode.toString());
    return applyPatch(original, patchNode);
  }

  public static JsonNode applyPatch(JsonNode original, JsonNode patch) {
    JsonNode result = original.deepCopy();

    for (JsonNode operation : patch) {
      String op = operation.get(OP_FIELD).asText();
      String path = operation.get(PATH_FIELD).asText();
      JsonNode value = operation.get(VALUE_FIELD);

      switch (op) {
        case ADD_OP:
        case REPLACE_OP:
          setValue(result, path, value);
          break;
        case REMOVE_OP:
          removeValue(result, path);
          break;
        case COPY_OP:
          JsonNode fromValue = getValue(result, operation.get(FROM_FIELD).asText());
          setValue(result, path, fromValue);
          break;
        case MOVE_OP:
          JsonNode movedValue = getValue(result, operation.get(FROM_FIELD).asText());
          removeValue(result, operation.get(FROM_FIELD).asText());
          setValue(result, path, movedValue);
          break;
        case TEST_OP:
          JsonNode tested = getValue(result, path);
          if (tested == null || !tested.equals(value)) {
            throw new IllegalStateException("Test operation failed at path: " + path);
          }
          break;
        default:
          throw new UnsupportedOperationException("Unsupported op: " + op);
      }
    }
    return result;
  }

  private static JsonNode getValue(JsonNode node, String path) {
    String[] parts = path.split("/");
    JsonNode current = node;
    for (String part : parts) {
      if (part.isEmpty()) continue;
      if (current == null) return null;
      current = current.get(part);
    }
    return current;
  }

  private static void setValue(JsonNode node, String path, JsonNode value) {
    String[] parts = path.split("/");
    List<String> segments = new ArrayList<>();
    for (String p : parts) if (!p.isEmpty()) segments.add(p);

    if (segments.isEmpty()) {
      // replace whole document
      if (node.isObject() && value.isObject()) {
        ((ObjectNode) node).removeAll();
        ((ObjectNode) node).setAll((ObjectNode) value);
      }
      return;
    }

    ObjectNode current = (ObjectNode) node;
    for (int i = 0; i < segments.size() - 1; i++) {
      current = current.withObject(segments.get(i));
    }
    current.set(segments.get(segments.size() - 1), value);
  }

  private static void removeValue(JsonNode node, String path) {
    String[] parts = path.split("/");
    List<String> segments = new ArrayList<>();
    for (String p : parts) if (!p.isEmpty()) segments.add(p);

    if (segments.isEmpty()) {
      // do nothing for root remove
      return;
    }

    JsonNode current = node;
    for (int i = 0; i < segments.size() - 1; i++) {
      if (current == null) return;
      current = current.get(segments.get(i));
    }
    if (current != null && current.isObject()) {
      ((ObjectNode) current).remove(segments.get(segments.size() - 1));
    }
  }
}
