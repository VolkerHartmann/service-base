package edu.kit.datamanager.util.json;


public class JsonPatchOperation {
  private String op;      // Operation: add, remove, replace, move, copy, test
  private String path;    // Zielpfad im JSON-Dokument
  private String from;    // Quelle für copy/move
  private Object value;   // Wert für add/replace/test

  public JsonPatchOperation() {}

  public JsonPatchOperation(String op, String path, String from, Object value) {
    this.op = op;
    this.path = path;
    this.from = from;
    this.value = value;
  }

  public String getOp() { return op; }
  public void setOp(String op) { this.op = op; }

  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }

  public String getFrom() { return from; }
  public void setFrom(String from) { this.from = from; }

  public Object getValue() { return value; }
  public void setValue(Object value) { this.value = value; }
}
