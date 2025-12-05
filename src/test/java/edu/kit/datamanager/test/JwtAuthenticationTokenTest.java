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

import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.entities.RepoServiceRole;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.JwtTemporaryToken;
import edu.kit.datamanager.security.filter.JwtUserToken;
import edu.kit.datamanager.security.filter.ScopedPermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public class JwtAuthenticationTokenTest {

    @Test
    public void testEmptyToken() {
        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123");
        Assertions.assertTrue(token.getAuthorities().isEmpty());
        Assertions.assertEquals("test123", token.getToken());
        Assertions.assertNull(token.getPrincipal());
        Assertions.assertEquals(JwtAuthenticationToken.NOT_AVAILABLE, token.getCredentials());
        Assertions.assertFalse(token.isAuthenticated());
    }

    @Test
    public void testTokenTypeFromString() {
        Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.UNSUPPORTED, JwtAuthenticationToken.TOKEN_TYPE.fromString("invalid"));
    }

    @Test
    public void testGrantedAuthoritiesFromNull() {
        Assertions.assertNotNull(JwtAuthenticationToken.grantedAuthorities(null));
        Assertions.assertTrue(JwtAuthenticationToken.grantedAuthorities(null).isEmpty());
    }

    @Test
    public void testFactoryUnsupportedToken() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", "UNSUPPORTED");
        claimMap.put("username", "tester");
        claimMap.put("firstname", "test");
        claimMap.put("lastname", "user");
        claimMap.put("email", "test@mail.org");
        claimMap.put("groups", Arrays.asList("USERS"));
        claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoUserRole.ADMINISTRATOR.getValue()}));

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Token " + token + " should not have been created due to invalid token type.");
      });
    }

    @Test
    public void testUserToken() throws JacksonException {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.USER.toString());
        claimMap.put("username", "tester");
        claimMap.put("firstname", "test");
        claimMap.put("lastname", "user");
        claimMap.put("email", "test@mail.org");
        claimMap.put("groups", Arrays.asList("USERS", "MANAGERS"));
        claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoUserRole.ADMINISTRATOR.getValue()}));

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.assertTrue(token instanceof JwtUserToken);
        Assertions.assertEquals(1, token.getAuthorities().size());
        Assertions.assertEquals("tester", token.getPrincipal());
        Assertions.assertEquals("test", ((JwtUserToken) token).getFirstname());
        Assertions.assertEquals("user", ((JwtUserToken) token).getLastname());
        Assertions.assertEquals("test@mail.org", ((JwtUserToken) token).getEmail());
        Assertions.assertTrue(token.getGroups().contains("USERS"));
        Assertions.assertTrue(token.getGroups().contains("MANAGERS"));
        Assertions.assertEquals(RepoUserRole.ADMINISTRATOR.getValue(), ((SimpleGrantedAuthority) token.getAuthorities().toArray()[0]).getAuthority());
        Assertions.assertEquals("test123", token.getToken());
        Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.USER, token.getTokenType());
        Assertions.assertTrue(token.isAuthenticated());
    }

    @Test
    public void testServiceToken() throws JacksonException {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.SERVICE.toString());
        claimMap.put("servicename", "testService");
        claimMap.put("groups", Arrays.asList("USERS", "ADMINS"));
        claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoServiceRole.SERVICE_READ.getValue()}));

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);

        //should work but nothing happens as claim is invalid
        token.setValueFromClaim("invalid", "value");

        Assertions.assertEquals("testService", token.getPrincipal());
        Assertions.assertTrue(token.getGroups().contains("USERS"));
        Assertions.assertTrue(token.getGroups().contains("ADMINS"));
        Assertions.assertEquals("test123", token.getToken());
        Assertions.assertEquals(RepoServiceRole.SERVICE_READ.getValue(), ((SimpleGrantedAuthority) token.getAuthorities().toArray()[0]).getAuthority());
        Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.SERVICE, token.getTokenType());
        Assertions.assertTrue(token.isAuthenticated());
    }

    @Test
    public void testServiceTokenWithInvalidSources() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.SERVICE.toString());
        claimMap.put("servicename", "testService");
        claimMap.put("groups", Arrays.asList("USERS"));
        claimMap.put("roles", new ObjectMapper().writeValueAsString(new String[]{RepoServiceRole.SERVICE_READ.getValue()}));
        claimMap.put("sources", "invalidValue");

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Token " + token + " should not have been created due to invalid sources claim.");
      });
    }

    @Test
    public void testTemporaryToken() throws JacksonException {
        ScopedPermission[] perms = new ScopedPermission[]{ScopedPermission.factoryScopedPermission(JwtTemporaryToken.class, "1", PERMISSION.READ)};

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("principalname", "test@mail.org");
        claimMap.put("permissions", new ObjectMapper().writeValueAsString(perms));

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);

        //should work but nothing happens as claim is invalid
        token.setValueFromClaim("invalid", "value");

        Assertions.assertTrue(token instanceof JwtTemporaryToken);
        Assertions.assertEquals("test@mail.org", token.getPrincipal());
        Assertions.assertEquals(1, ((JwtTemporaryToken) token).getScopedPermissions().length);
        Assertions.assertEquals("JwtTemporaryToken", ((JwtTemporaryToken) token).getScopedPermissions()[0].getResourceType());
        Assertions.assertEquals("1", ((JwtTemporaryToken) token).getScopedPermissions()[0].getResourceId());
        Assertions.assertEquals(PERMISSION.READ, ((JwtTemporaryToken) token).getScopedPermissions()[0].getPermission());
        Assertions.assertEquals("test123", token.getToken());
        Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY, token.getTokenType());
        Assertions.assertTrue(token.isAuthenticated());
    }

    @Test
    public void testTemporaryTokenWithNoPermissions() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("principalname", "test@mail.org");

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Token " + token + " should not have been created due to missing scoped permissions.");
    });
    }

    @Test
    public void testTemporaryTokenWithInvalidPermissions() throws JacksonException {
      Assertions.assertThrows(StreamReadException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("principalname", "test@mail.org");
        claimMap.put("permissions", "invalid value");

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Token " + token + " should not have been created due to missing scoped permissions.");
      });
    }

    @Test
    public void testNoType() throws JacksonException {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("username", "tester");

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.assertTrue(token instanceof JwtUserToken);
        Assertions.assertEquals("tester", token.getPrincipal());
        Assertions.assertTrue(token.getAuthorities().stream().filter(a -> a.getAuthority().equals(RepoUserRole.GUEST.getValue())).count() > 0);
        Assertions.assertTrue(token.isAuthenticated());
    }

    @Test
    public void testInvalidClaimType() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("principalname", "test@mail.org");
        claimMap.put("permissions", 12);//wrong type

        JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Claim type check succeeded unexpectedly.");
      });
    }

    @Test
    public void testTemporaryTokenWithoutPermissions() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("principalname", "test@mail.org");

        JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Creation of temporary token without permissions should fail.");
      });
    }

    @Test
    public void testTokenWithoutPrincipal() throws JacksonException {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        ScopedPermission[] perms = new ScopedPermission[]{ScopedPermission.factoryScopedPermission(JwtTemporaryToken.class, "1", PERMISSION.READ)};

        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY.toString());
        claimMap.put("permissions", new ObjectMapper().writeValueAsString(perms));

        JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.fail("Creation of temporary token without principal should fail.");
      });
    }

    @Test
    public void testInvalidRolesValue() throws JacksonException {
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("tokenType", JwtAuthenticationToken.TOKEN_TYPE.USER.toString());
        claimMap.put("username", "tester");
        claimMap.put("firstname", "test");
        claimMap.put("lastname", "user");
        claimMap.put("email", "test@mail.org");
        claimMap.put("groups", Arrays.asList("USERS"));
        claimMap.put("roles", new ObjectMapper().writeValueAsString("INVALID_VALUE"));

        JwtAuthenticationToken token = JwtAuthenticationToken.factoryToken("test123", claimMap);
        Assertions.assertEquals(1, token.getAuthorities().size());
        Assertions.assertEquals(RepoUserRole.GUEST.getValue(), token.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority());
    }
}
