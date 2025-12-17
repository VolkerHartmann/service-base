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
package edu.kit.datamanager.test;

import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.datamanager.util.json.JsonPatch;
import edu.kit.datamanager.annotations.SecureUpdate;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.PatchApplicationException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.util.PatchUtil;
import java.util.Arrays;
import java.io.IOException;

import edu.kit.datamanager.util.json.exceptions.JsonPatchProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static edu.kit.datamanager.util.JsonMapper.MAPPER;

/**
 *
 * @author jejkal
 */
public class PatchUtilTest{

  @Test
  public void patchUnsecuredField() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");

    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/text", null, "updated");
    JsonPatch replace = new JsonPatch(Arrays.asList(op));

    TestEntity patched = PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
    Assert.assertEquals("updated", patched.getText());
  }

  @Test(expected = UpdateForbiddenException.class)
  public void patchForbiddenField() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");

    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/id", null, "2");
    JsonPatch replace = new JsonPatch(Arrays.asList(op));

    PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
  }

  @Test
  public void patchSecuredFieldWithSufficientPrivileges() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");
    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/number", null, "2");
    JsonPatch replace = new JsonPatch(Arrays.asList(op));

    TestEntity patched = PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
    Assert.assertEquals(2, patched.getNumber());

    e = new TestEntity(0, 1, "test");
    patched = PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority(RepoUserRole.ADMINISTRATOR.getValue())));
    Assert.assertEquals(2, patched.getNumber());

    e = new TestEntity(0, 1, "test");
    patched = PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority(PERMISSION.ADMINISTRATE.getValue())));
    Assert.assertEquals(2, patched.getNumber());

  }

  @Test
  public void patchSecuredFieldWithInsufficientPrivileges() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");

    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/number", null, 2);
    JsonPatch replace = new JsonPatch(Arrays.asList(op));

    try{
      PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("MEMBER")));
      Assert.fail("Patch not expected to be applied with MEMBER privileges.");
    } catch(UpdateForbiddenException ex){
      //expected
    }

    try{
      PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority(RepoUserRole.USER.getValue())));
      Assert.fail("Patch not expected to be applied with USER role.");
    } catch(UpdateForbiddenException ex){
      //expected
    }

    try{
      PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority(PERMISSION.WRITE.getValue())));
      Assert.fail("Patch not expected to be applied with WRITE permissions.");
    } catch(UpdateForbiddenException ex){
      //expected
    }

  }

  @Test(expected = UpdateForbiddenException.class)
  public void patchSecuredFieldWithInsufficientRole() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");

    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/number", null, "2");
    JsonPatch replace = new JsonPatch(Arrays.asList(op));

    PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  @Test(expected = PatchApplicationException.class)
  public void applyInvalidPatch() throws IOException{
    TestEntity e = new TestEntity(0, 1, "test");

    JsonPatch.Operation op = new JsonPatch.Operation(JsonPatch.OperationType.REPLACE, "/invalidField", null, "updated");
    JsonPatch replace = new JsonPatch(Arrays.asList(op));
    PatchUtil.applyPatch(e, replace, TestEntity.class, Arrays.asList(new SimpleGrantedAuthority("ADMINISTRATOR")));
  }

}

class TestEntity{

  @SecureUpdate(value = "FORBIDDEN")
  private int id;
  @SecureUpdate(value = {"ADMINISTRATOR", "ROLE_ADMINISTRATOR", "PERMISSION_ADMINISTRATE"})
  private int number;
  private String text;

  public TestEntity(){
  }

  public TestEntity(int id, int number, String text){
    this.id = id;
    this.number = number;
    this.text = text;
  }

  public int getId(){
    return id;
  }

  public void setId(int id){
    this.id = id;
  }

  public int getNumber(){
    return number;
  }

  public void setNumber(int number){
    this.number = number;
  }

  public String getText(){
    return text;
  }

  public void setText(String text){
    this.text = text;
  }
}
