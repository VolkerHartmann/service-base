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

import edu.kit.datamanager.entities.BaseEnum;
import edu.kit.datamanager.util.EnumUtils;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class EnumUtilsTest{

  private enum TEST_ENUM implements BaseEnum{
    FIRST("first"),
    SECOND("second");
    private final String value;

    TEST_ENUM(String value){
      this.value = value;
    }

    @Override
    public String getValue(){
      return value;
    }
  }

  @Test
  public void testEquals(){
    Assertions.assertTrue(EnumUtils.equals(TEST_ENUM.FIRST, TEST_ENUM.FIRST));
    Assertions.assertFalse(EnumUtils.equals(TEST_ENUM.FIRST, TEST_ENUM.SECOND));
    Assertions.assertFalse(EnumUtils.equals(null, TEST_ENUM.SECOND));
    Assertions.assertFalse(EnumUtils.equals(TEST_ENUM.FIRST, null));
    Assertions.assertTrue(EnumUtils.equals(null, null));
  }

  @Test
  public void testHashCode(){
    Assertions.assertEquals(EnumUtils.hashCode(TEST_ENUM.FIRST), Objects.hashCode("first"));
  }

  @Test
  public void testHashCodeForNullValue(){
    Assertions.assertEquals(EnumUtils.hashCode(null), Objects.hashCode(null));
  }
}
