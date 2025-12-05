/*
 * Copyright 2018 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.perf;

import edu.kit.datamanager.util.json.JsonPatch;
import edu.kit.datamanager.util.json.JsonPatchOperation;
import edu.kit.datamanager.util.json.JsonPatchUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Just for short tests.
 *
 * @author jejkal
 */
@SuppressWarnings("UnnecessarilyFullyQualified")
public class Test {

  private static final Logger logger = LoggerFactory.getLogger(Test.class);

  public static void main(String[] args) throws Exception {

    //if (true) {
    //  return;
    //}
    ObjectMapper mapper = new ObjectMapper();

    Entity toPatch = new Entity("SomeTitle", 0);
    toPatch.addListEntry("Testing");
    toPatch.addListEntry("done");
    toPatch.addListEntry("!");

    JsonPatchOperation op_replace = new JsonPatchOperation(JsonPatchUtil.REPLACE_OP, "title", null, mapper.readTree(mapper.writeValueAsString("Test")));
    List<JsonPatchOperation> ops = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      ops.add(new JsonPatchOperation(JsonPatchUtil.REPLACE_OP, "title", null, mapper.readTree(mapper.writeValueAsString("Test" + i))));
    }

    //   JsonPatchOperation op_add = new ReplaceOperation(JsonPointer.of("list", "2"), mapper.readTree(mapper.writeValueAsString("?")));
    //JsonPatch patch = new JsonPatch(Arrays.asList(op_replace, op_add));
    JsonPatch patch = new JsonPatch(ops);

    JsonNode resourceAsNode = mapper.convertValue(toPatch, JsonNode.class);

    long s = System.currentTimeMillis();
    JsonNode patchedDataResourceAsNode = JsonPatchUtil.applyPatch(resourceAsNode, patch);
    logger.info("Dur: " + (System.currentTimeMillis() - s));

    logger.info("Patched: " + patchedDataResourceAsNode);

    Entity updated = mapper.treeToValue(patchedDataResourceAsNode, Entity.class);

   // JsonPatch patchGenerated = JsonDiff.asJsonPatch(mapper.convertValue(toPatch, JsonNode.class), mapper.convertValue(updated, JsonNode.class));

    logger.info("Input: " + toPatch);
    logger.info("Output: " + updated);
    //logger.info("Patch: " + patchGenerated);

  }

  @Data
  static class Entity {

    private String title;
    private int number;
    private Collection<String> list;

    public Entity() {
      list = new ArrayList<>();
    }

    public Entity(String title, int number) {
      this();
      this.title = title;
      this.number = number;
    }

    public void addListEntry(String entry) {
      list.add(entry);
    }

    public void removeListEntry(String entry) {
      list.remove(entry);
    }

  }
}
