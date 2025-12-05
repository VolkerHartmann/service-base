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
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.ScopedPermission;
import edu.kit.datamanager.util.AuthenticationHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import tools.jackson.core.JacksonException;

import java.util.Arrays;

/**
 *
 * @author jejkal
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationHelperTest {

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    private String key = "vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb";

    @Test
    public void testJwtUserToken() throws JacksonException {
        mockJwtUserAuthentication();

        Assertions.assertTrue(AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue()));
        Assertions.assertEquals("test", AuthenticationHelper.getFirstname());
        Assertions.assertEquals("user", AuthenticationHelper.getLastname());
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("tester"));
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("anonymousUser"));
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("USERS"));
        Assertions.assertFalse(AuthenticationHelper.isAuthenticatedAsService());
        Assertions.assertEquals(PERMISSION.NONE, AuthenticationHelper.getScopedPermission(String.class.getSimpleName(), "1"));
    }

    @Test
    public void testOtherAuthentication() {
        mockNoAuthentication();

        Assertions.assertNull(AuthenticationHelper.getFirstname());
        Assertions.assertNull(AuthenticationHelper.getLastname());
        Assertions.assertEquals("anonymous", AuthenticationHelper.getPrincipal());
    }

    @Test
    public void testJwtServiceToken() throws JacksonException {
        mockJwtServiceAuthentication();

        Assertions.assertEquals("metadata_extractor", AuthenticationHelper.getPrincipal());
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("metadata_extractor"));
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("USERS"));
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("SERVICE"));
        Assertions.assertTrue(AuthenticationHelper.hasAuthority(RepoServiceRole.SERVICE_READ.getValue()));
        Assertions.assertTrue(AuthenticationHelper.isAuthenticatedAsService());

    }

    @Test
    public void testJwtTemporaryToken() throws JacksonException {
        mockJwtTemporaryAuthentication();

        Assertions.assertEquals("test@mail.org", AuthenticationHelper.getPrincipal());
        Assertions.assertTrue(AuthenticationHelper.hasIdentity("test@mail.org"));
        Assertions.assertEquals(PERMISSION.READ, AuthenticationHelper.getScopedPermission(String.class.getSimpleName(), "1"));
    }

    private void mockNoAuthentication() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(new AnonymousAuthenticationToken("test", "anonymous", Arrays.asList(new SimpleGrantedAuthority("anonymous"))));
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockJwtUserAuthentication() throws JacksonException {
        JwtAuthenticationToken userToken = edu.kit.datamanager.util.JwtBuilder.
                createUserToken("tester", RepoUserRole.ADMINISTRATOR).
                addSimpleClaim("firstname", "test").
                addSimpleClaim("lastname", "user").
                addSimpleClaim("email", "test@mail.org").
                addObjectClaim("groups", Arrays.asList("USERS")).
                getJwtAuthenticationToken(key);

        Mockito.when(securityContext.getAuthentication()).thenReturn(userToken);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockJwtServiceAuthentication() throws JacksonException {
        JwtAuthenticationToken serviceToken = edu.kit.datamanager.util.JwtBuilder.
                createServiceToken("metadata_extractor", RepoServiceRole.SERVICE_READ).
                addObjectClaim("groups", Arrays.asList("USERS", "SERVICE")).
                getJwtAuthenticationToken(key);
        Mockito.when(securityContext.getAuthentication()).thenReturn(serviceToken);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockJwtTemporaryAuthentication() throws JacksonException {
        ScopedPermission[] perms = new ScopedPermission[]{ScopedPermission.factoryScopedPermission("String", "1", PERMISSION.READ)};

        JwtAuthenticationToken temporaryToken = edu.kit.datamanager.util.JwtBuilder.createTemporaryToken("test@mail.org", perms).
                getJwtAuthenticationToken(key);
        Mockito.when(securityContext.getAuthentication()).thenReturn(temporaryToken);
        SecurityContextHolder.setContext(securityContext);
    }
}
