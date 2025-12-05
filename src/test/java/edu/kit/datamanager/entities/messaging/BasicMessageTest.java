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
package edu.kit.datamanager.entities.messaging;

import edu.kit.datamanager.exceptions.MessageValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public class BasicMessageTest{

  @Test
  public void test() throws JacksonException, IOException{
    BasicMessage msg = new BasicMessage(){
      @Override
      public String getEntityName(){
        return "Test";
      }
    };

    msg.setPrincipal("tester");
    msg.setSender("localhost");
    msg.setCurrentTimestamp();

    msg.setEntityId("1");
    msg.setAction("create");

    Assertions.assertEquals("Test", msg.getEntityName());
    Assertions.assertEquals("tester", msg.getPrincipal());
    Assertions.assertEquals("localhost", msg.getSender());
    Assertions.assertNotNull(msg.getTimestamp());

    Assertions.assertEquals("1", msg.getEntityId());
    Assertions.assertEquals("create", msg.getAction());

    //test routing key creation and lowercase entity name
    Assertions.assertEquals("test.create", msg.getRoutingKey());

    //test lowercase action
    msg.setAction("Create");
    Assertions.assertEquals("test.create", msg.getRoutingKey());

    //test subcategory
    msg.setSubCategory("data");
    Assertions.assertEquals("test.create.data", msg.getRoutingKey());

    //test lowercase subcategory
    msg.setSubCategory("Data");
    Assertions.assertEquals("test.create.data", msg.getRoutingKey());

    Map<String, String> properties = new HashMap<>();
    properties.put("key", "value");
    properties.put("key2", "anotherValue");
    msg.setMetadata(properties);

    Assertions.assertNotNull(msg.getMetadata());
    Assertions.assertEquals(2, msg.getMetadata().size());

    String toJson = msg.toJson();
    System.out.println(toJson);
    BasicMessage msg2 = BasicMessage.fromJson(toJson);

    //some fields are not ignored and must be equal
    Assertions.assertEquals(msg.getEntityId(), msg2.getEntityId());
    Assertions.assertEquals(msg.getAction(), msg2.getAction());
    Assertions.assertEquals(msg.getSubCategory(), msg2.getSubCategory());
    Assertions.assertEquals(msg.getSender(), msg2.getSender());
    Assertions.assertEquals(msg.getTimestamp(), msg2.getTimestamp());
    Assertions.assertEquals(msg.getMetadata(), msg2.getMetadata());
    
  }

  @Test
  public void testInvalidEntityName(){
    Assertions.assertThrows(MessageValidationException.class, () -> {
      BasicMessage msg = new BasicMessage() {
        @Override
        public String getEntityName() {
          return null;
        }
      };

      msg.validate();
    });
  }

  @Test
  public void testInvalidAction(){
    Assertions.assertThrows(MessageValidationException.class, () -> {
      BasicMessage msg = new BasicMessage() {
        @Override
        public String getEntityName() {
          return "test";
        }
      };

      msg.setAction(null);
      msg.validate();
    });
  }

  @Test
  public void testEntityId(){
    Assertions.assertThrows(MessageValidationException.class, () -> {
      BasicMessage msg = new BasicMessage() {
        @Override
        public String getEntityName() {
          return "test";
        }
      };

      msg.setAction("create");
      msg.setEntityId(null);
      msg.validate();
    });
  }
}
