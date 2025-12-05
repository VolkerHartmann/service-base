/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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

import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.JwtEmptyToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class JwtEmptyTokenTest{

  @Test
  public void testEmptyToken(){
    JwtEmptyToken token = new JwtEmptyToken("test123");
    Assertions.assertArrayEquals(token.getSupportedClaims(), new String[0]);
    Assertions.assertEquals(token.getClassForClaim("123"), Object.class);
    Assertions.assertEquals(token.getTokenType(), JwtAuthenticationToken.TOKEN_TYPE.USER);

    //should not do anything
    int hash = token.hashCode();
    token.setValueFromClaim("something", "another thing");
    Assertions.assertEquals(hash, token.hashCode());

    //should work aka. do nothing
    token.validate();
  }
}
