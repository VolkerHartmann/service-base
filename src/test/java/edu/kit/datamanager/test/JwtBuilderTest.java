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

import tools.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.entities.RepoServiceRole;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.JwtServiceToken;
import edu.kit.datamanager.security.filter.JwtTemporaryToken;
import edu.kit.datamanager.security.filter.JwtUserToken;
import edu.kit.datamanager.security.filter.ScopedPermission;
import edu.kit.datamanager.util.JwtBuilder;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class JwtBuilderTest{
    private final String key = "vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb";

  @Test
  public void testServiceToken() throws IOException{
    JwtBuilder builder = JwtBuilder.createServiceToken("myservice", RepoServiceRole.SERVICE_ADMINISTRATOR);
    builder.addSimpleClaim("sources", "[\"localhost\"]");

    Map<String, Object> claimMap = builder.getClaimMap();
    Assertions.assertTrue(claimMap.containsKey("servicename"));
    Assertions.assertEquals("myservice", claimMap.get("servicename"));
    Assertions.assertTrue(claimMap.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.SERVICE.toString(), (String) claimMap.get("tokenType"));
    Assertions.assertTrue(claimMap.containsKey("roles"));
    String[] roles = new ObjectMapper().readValue((String) claimMap.get("roles"), String[].class);
    Assertions.assertArrayEquals(new String[]{RepoServiceRole.SERVICE_ADMINISTRATOR.getValue()}, roles);

    JwtAuthenticationToken jwtAuthToken = builder.getJwtAuthenticationToken(key);
    Assertions.assertTrue(jwtAuthToken instanceof JwtServiceToken);
    String compactToken = builder.getCompactToken(key);
    Assertions.assertEquals(compactToken, jwtAuthToken.getToken());

    Claims claims = builder.getClaims();
    Assertions.assertTrue(claims.containsKey("servicename"));
    Assertions.assertEquals("myservice", claims.get("servicename"));
    Assertions.assertTrue(claims.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.SERVICE.toString(), (String) claims.get("tokenType"));
    roles = new ObjectMapper().readValue((String) claims.get("roles"), String[].class);
    Assertions.assertArrayEquals(new String[]{RepoServiceRole.SERVICE_ADMINISTRATOR.getValue()}, roles);
  }

  @Test
  public void testUserToken() throws IOException{
    JwtBuilder builder = JwtBuilder.createUserToken("tester", RepoUserRole.USER).addSimpleClaim("age", 38).addSimpleClaim("active", Boolean.TRUE);

    Map<String, Object> claimMap = builder.getClaimMap();
    Assertions.assertTrue(claimMap.containsKey("username"));
    Assertions.assertEquals("tester", claimMap.get("username"));
    Assertions.assertTrue(claimMap.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.USER.toString(), (String) claimMap.get("tokenType"));
    Assertions.assertTrue(claimMap.containsKey("age"));
    Assertions.assertEquals(38, (int) claimMap.get("age"));
    Assertions.assertTrue(claimMap.containsKey("active"));
    Assertions.assertEquals(true, (boolean) claimMap.get("active"));

    Assertions.assertTrue(claimMap.containsKey("roles"));
    String[] roles = new ObjectMapper().readValue((String) claimMap.get("roles"), String[].class);
    Assertions.assertArrayEquals(new String[]{RepoUserRole.USER.getValue()}, roles);

    JwtAuthenticationToken jwtAuthToken = builder.getJwtAuthenticationToken(key);
    Assertions.assertTrue(jwtAuthToken instanceof JwtUserToken);
    String compactToken = builder.getCompactToken(key);
    Assertions.assertEquals(compactToken, jwtAuthToken.getToken());

    Claims claims = builder.getClaims();
    Assertions.assertTrue(claims.containsKey("username"));
    Assertions.assertEquals("tester", claims.get("username"));
    Assertions.assertTrue(claims.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.USER.toString(), (String) claims.get("tokenType"));
    Assertions.assertTrue(claims.containsKey("age"));
    Assertions.assertEquals(38, (int) claims.get("age"));
    Assertions.assertTrue(claims.containsKey("active"));
    Assertions.assertEquals(true, (boolean) claims.get("active"));
    Assertions.assertTrue(claims.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.USER.toString(), (String) claims.get("tokenType"));
    roles = new ObjectMapper().readValue((String) claims.get("roles"), String[].class);
    Assertions.assertArrayEquals(new String[]{RepoUserRole.USER.getValue()}, roles);
  }

  @Test
  public void testTemporaryToken() throws IOException{
    JwtBuilder builder = JwtBuilder.createTemporaryToken("test@mail.org", ScopedPermission.factoryScopedPermission("DataResource", "1", PERMISSION.WRITE));

    Map<String, Object> claimMap = builder.getClaimMap();
    Assertions.assertTrue(claimMap.containsKey("principalname"));
    Assertions.assertEquals("test@mail.org", claimMap.get("principalname"));

    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString(), (String) claimMap.get("tokenType"));
    Assertions.assertTrue(claimMap.containsKey("permissions"));
    ScopedPermission[] permissions = new ObjectMapper().readValue((String) claimMap.get("permissions"), ScopedPermission[].class);
    Assertions.assertArrayEquals(new ScopedPermission[]{ScopedPermission.factoryScopedPermission("DataResource", "1", PERMISSION.WRITE)}, permissions);

    JwtAuthenticationToken jwtAuthToken = builder.getJwtAuthenticationToken(key);
    Assertions.assertTrue(jwtAuthToken instanceof JwtTemporaryToken);
    String compactToken = builder.getCompactToken(key);
    Assertions.assertEquals(compactToken, jwtAuthToken.getToken());

    Claims claims = builder.getClaims();
    Assertions.assertTrue(claims.containsKey("principalname"));
    Assertions.assertEquals("test@mail.org", claims.get("principalname"));
    Assertions.assertTrue(claims.containsKey("tokenType"));
    Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString(), (String) claims.get("tokenType"));
    permissions = new ObjectMapper().readValue((String) claims.get("permissions"), ScopedPermission[].class);
    Assertions.assertArrayEquals(new ScopedPermission[]{ScopedPermission.factoryScopedPermission("DataResource", "1", PERMISSION.WRITE)}, permissions);
  }

  @Test
  public void testTemporaryTokenWithInvalidScopedPermissions() throws IOException{
    JwtBuilder builder = JwtBuilder.createTemporaryToken("test@mail.org");
    Assertions.assertEquals("[]", builder.getClaimMap().get("permissions"));
    builder = JwtBuilder.createTemporaryToken("test@mail.org", (ScopedPermission[])null);
    Assertions.assertEquals("[]", builder.getClaimMap().get("permissions"));
  }
}
