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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.entities.RepoServiceRole;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.JwtTemporaryToken;
import edu.kit.datamanager.security.filter.JwtUserToken;
import edu.kit.datamanager.security.filter.ScopedPermission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 *
 * @author jejkal
 */
public class JwtAuthenticationTokenTest{

  @Test
  public void testEmptyToken(){
    JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123");
    Assert.assertTrue(token.getAuthorities().isEmpty());
    Assert.assertEquals("test123", token.getToken());
    Assert.assertNull(token.getPrincipal());
    Assert.assertEquals(JwtAuthenticationToken.NOT_AVAILABLE, token.getCredentials());
    Assert.assertFalse(token.isAuthenticated());
  }

  @Test
  public void testUserToken() throws JsonProcessingException{
    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.USER.toString());
    claimMap.put("username", "tester");
    claimMap.put("firstname", "test");
    claimMap.put("lastname", "user");
    claimMap.put("email", "test@mail.org");
    claimMap.put("groupid", "USERS");
    claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoUserRole.ADMINISTRATOR.getValue()}));

    JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.assertTrue(token instanceof JwtUserToken);
    Assert.assertEquals(1, token.getAuthorities().size());
    Assert.assertEquals("tester", token.getPrincipal());
    Assert.assertEquals("test", ((JwtUserToken) token).getFirstname());
    Assert.assertEquals("user", ((JwtUserToken) token).getLastname());
    Assert.assertEquals("test@mail.org", ((JwtUserToken) token).getEmail());
    Assert.assertEquals("USERS", token.getGroupId());
    Assert.assertEquals(RepoUserRole.ADMINISTRATOR.getValue(), ((SimpleGrantedAuthority) token.getAuthorities().toArray()[0]).getAuthority());
    Assert.assertEquals("test123", token.getToken());
    Assert.assertTrue(token.isAuthenticated());
  }

  @Test
  public void testServiceToken() throws JsonProcessingException{
    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.SERVICE.toString());
    claimMap.put("servicename", "testService");
    claimMap.put("groupid", "USERS");
    claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoServiceRole.SERVICE_READ.getValue()}));

    JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.assertEquals("testService", token.getPrincipal());
    Assert.assertEquals("USERS", token.getGroupId());
    Assert.assertEquals("test123", token.getToken());
    Assert.assertEquals(RepoServiceRole.SERVICE_READ.getValue(), ((SimpleGrantedAuthority) token.getAuthorities().toArray()[0]).getAuthority());
    Assert.assertTrue(token.isAuthenticated());
  }

  @Test
  public void testTemporaryToken() throws JsonProcessingException{
    ScopedPermission[] perms = new ScopedPermission[]{ScopedPermission.factoryScopedPermission(JwtTemporaryToken.class, "1", PERMISSION.READ)};

    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
    claimMap.put("principalname", "test@mail.org");
    claimMap.put("permissions", new ObjectMapper().writeValueAsString(perms));

    JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.assertTrue(token instanceof JwtTemporaryToken);
    Assert.assertEquals("test@mail.org", token.getPrincipal());
    Assert.assertEquals(1, ((JwtTemporaryToken) token).getScopedPermissions().length);
    Assert.assertEquals("JwtTemporaryToken", ((JwtTemporaryToken) token).getScopedPermissions()[0].getResourceType());
    Assert.assertEquals("1", ((JwtTemporaryToken) token).getScopedPermissions()[0].getResourceId());
    Assert.assertEquals(PERMISSION.READ, ((JwtTemporaryToken) token).getScopedPermissions()[0].getPermission());
    Assert.assertEquals("test123", token.getToken());
    Assert.assertTrue(token.isAuthenticated());
  }

  @Test
  public void testNoType() throws JsonProcessingException{
    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("username", "tester");

    JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.assertTrue(token instanceof JwtUserToken);
    Assert.assertEquals("tester", token.getPrincipal());
    Assert.assertTrue(token.getAuthorities().stream().filter(a -> a.getAuthority().equals(RepoUserRole.GUEST.getValue())).count() > 0);
    Assert.assertTrue(token.isAuthenticated());
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testInvalidClaimType() throws JsonProcessingException{
    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
    claimMap.put("principalname", "test@mail.org");
    claimMap.put("permissions", 12);//wrong type

    JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.fail("Claim type check succeeded unexpectedly.");
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testTemporaryTokenWithoutPermissions() throws JsonProcessingException{
    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
    claimMap.put("principalname", "test@mail.org");

    JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.fail("Creation of temporary token without permissions should fail.");
  }

  @Test(expected = InvalidAuthenticationException.class)
  public void testTokenWithoutPrincipal() throws JsonProcessingException{
    ScopedPermission[] perms = new ScopedPermission[]{ScopedPermission.factoryScopedPermission(JwtTemporaryToken.class, "1", PERMISSION.READ)};

    Map<String, Object> claimMap = new HashMap<>();
    claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
    claimMap.put("permissions", new ObjectMapper().writeValueAsString(perms));

    JwtAuthenticationToken.factoryToken("test123", claimMap);
    Assert.fail("Creation of temporary token without principal should fail.");
  }
}
