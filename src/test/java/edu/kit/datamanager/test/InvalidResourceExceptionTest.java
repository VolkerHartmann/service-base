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

import edu.kit.datamanager.entities.dc40.exceptions.InvalidResourceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class InvalidResourceExceptionTest{

  @Test
  public void testInvalidResourceException(){

    for(InvalidResourceException.ERROR_TYPE error : InvalidResourceException.ERROR_TYPE.values()){
      try{
        throw new InvalidResourceException(error);
      } catch(InvalidResourceException e){
        Assertions.assertEquals(error, e.getType());
        Assertions.assertEquals(e.getMessage(), error.getMessage());
      }
    }
  }
}
