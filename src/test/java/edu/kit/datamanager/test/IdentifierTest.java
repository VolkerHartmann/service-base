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

import edu.kit.datamanager.entities.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class IdentifierTest{

  @Test
  public void testInternalIdentifierWithValue(){
    Identifier id = Identifier.factoryInternalIdentifier("test123");
    Assertions.assertNull(id.getId());
    Assertions.assertEquals("test123", id.getValue());
    Assertions.assertEquals(Identifier.IDENTIFIER_TYPE.INTERNAL, id.getIdentifierType());
  }

  @Test
  public void testIdentifierEquals(){
    Identifier id1 = Identifier.factoryInternalIdentifier("test123");
    Identifier id2 = Identifier.factoryInternalIdentifier("test123");

    //both should be equal as 'id' is still null
    Assertions.assertEquals(id1, id2);
    Assertions.assertEquals(id1.hashCode(), id2.hashCode());
    //change id and check again
    id1.setId(Long.MIN_VALUE);
    Assertions.assertNotEquals(id1, id2);
  }

  @Test
  public void testInternalIdentifier(){
    Identifier id = Identifier.factoryInternalIdentifier();

    Assertions.assertNull(id.getId());
    Assertions.assertNotNull(id.getValue());
    Assertions.assertEquals(Identifier.IDENTIFIER_TYPE.INTERNAL, id.getIdentifierType());

    Identifier id2 = Identifier.factoryInternalIdentifier();
    //should not be equal due to random identifier value
    Assertions.assertNotEquals(id, id2);
  }

  @Test
  public void testIdentifierWithValueAndType(){
    Identifier id = Identifier.factoryIdentifier("test123", Identifier.IDENTIFIER_TYPE.DOI);
    Assertions.assertNull(id.getId());
    Assertions.assertEquals("test123", id.getValue());
    Assertions.assertEquals(Identifier.IDENTIFIER_TYPE.DOI, id.getIdentifierType());
  }

  @Test
  public void testIdentifierEqualsWithNull(){
    Assertions.assertFalse(Identifier.factoryInternalIdentifier().equals(null));
  }

  @Test
  public void testIdentifierEqualsWithItself(){
    Identifier id = Identifier.factoryInternalIdentifier();
    Assertions.assertTrue(id.equals(id));
  }

  @Test
  public void testIdentifierEqualsWithDifferentClass(){
    Assertions.assertFalse(Identifier.factoryInternalIdentifier().equals("ThisIsAString"));
  }

  @Test
  public void testToString(){
    Assertions.assertTrue(Identifier.factoryInternalIdentifier().toString().startsWith(Identifier.class.getSimpleName()));
    Assertions.assertTrue(Identifier.factoryInternalIdentifier().toString().contains("id=null"));
    Assertions.assertTrue(Identifier.factoryInternalIdentifier().toString().contains("value="));
    Assertions.assertTrue(Identifier.factoryInternalIdentifier().toString().contains("identifierType=" + Identifier.IDENTIFIER_TYPE.INTERNAL.toString()));
  }

}
