// src/test/java/edu/kit/datamanager/util/json/JsonPatchUtilTest.java
package edu.kit.datamanager.util.json;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {edu.kit.datamanager.SpringTestConfig.class})
class JsonPatchUtilTest {

  private final JsonMapper mapper = JsonMapper.builder().build();
  private final JsonPatchUtil util = new JsonPatchUtil();

  @Test
  void addOperationAddsValue() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1}}");
    JsonNode patch = mapper.readTree("[{\"op\":\"add\",\"path\":\"/a/c\",\"value\":2}]");

    JsonNode result = util.applyPatch(original, patch);
    assertEquals(2, result.get("a").get("c").asInt());
    assertEquals(1, result.get("a").get("b").asInt());
  }

  @Test
  void replaceOperationReplacesValue() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1}}");
    JsonNode patch = mapper.readTree("[{\"op\":\"replace\",\"path\":\"/a/b\",\"value\":5}]");

    JsonNode result = util.applyPatch(original, patch);
    assertEquals(5, result.get("a").get("b").asInt());
  }

  @Test
  void removeOperationRemovesValue() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1,\"c\":2}}");
    JsonNode patch = mapper.readTree("[{\"op\":\"remove\",\"path\":\"/a/b\"}]");

    JsonNode result = util.applyPatch(original, patch);
    assertNull(result.get("a").get("b"));
    assertEquals(2, result.get("a").get("c").asInt());
  }

  @Test
  void copyOperationCopiesValue() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1}}");
    JsonNode patch = mapper.readTree("[{\"op\":\"copy\",\"from\":\"/a/b\",\"path\":\"/a/c\"}]");

    JsonNode result = util.applyPatch(original, patch);
    assertEquals(1, result.get("a").get("c").asInt());
    assertEquals(1, result.get("a").get("b").asInt());
  }

  @Test
  void moveOperationMovesValue() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1}}");
    JsonNode patch = mapper.readTree("[{\"op\":\"move\",\"from\":\"/a/b\",\"path\":\"/a/c\"}]");

    JsonNode result = util.applyPatch(original, patch);
    assertNull(result.get("a").get("b"));
    assertEquals(1, result.get("a").get("c").asInt());
  }

  @Test
  void testOperationSucceedsAndFails() throws Exception {
    JsonNode original = mapper.readTree("{\"a\":{\"b\":1}}");
    JsonNode patchOk = mapper.readTree("[{\"op\":\"test\",\"path\":\"/a/b\",\"value\":1}]");
    JsonNode patchFail = mapper.readTree("[{\"op\":\"test\",\"path\":\"/a/b\",\"value\":2}]");

    // succeeds
    assertDoesNotThrow(() -> util.applyPatch(original, patchOk));
    // fails
    assertThrows(IllegalStateException.class, () -> util.applyPatch(original, patchFail));
  }
}
