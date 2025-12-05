package edu.kit.datamanager.util.json;

import java.util.List;

/**
 * Json structure holding an array of patches.
 */
public class JsonPatch {
  private List<JsonPatchOperation> operations;

  public JsonPatch() {}

  public JsonPatch(List<JsonPatchOperation> operations) {
    this.operations = operations;
  }

  public List<JsonPatchOperation> getOperations() {
    return operations;
  }

  public void setOperations(List<JsonPatchOperation> operations) {
    this.operations = operations;
  }
}

