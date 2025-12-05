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

import edu.kit.datamanager.entities.RepoServiceRole;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.JwtUserToken;
import edu.kit.datamanager.security.filter.KeycloakTokenFilter;
import edu.kit.datamanager.security.filter.KeycloakTokenValidator;
import edu.kit.datamanager.util.JwtBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

/**
 *
 * @author jejkal
 */
public class JwtAuthenticationFilterTest {

    private final AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
    private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    private final FilterChain filterChain = Mockito.mock(FilterChain.class);
    private final SecurityContext context = Mockito.mock(SecurityContext.class);

    private final String key = "vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb";

    private KeycloakTokenFilter keycloaktokenFilterBean() throws Exception {
        return new KeycloakTokenFilter(KeycloakTokenValidator.builder()
                .jwtLocalSecret(key)
                .build(null, null, null));
    }

    @Test
    public void testNoAuthenticationHeader() throws Exception {
        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn(null);
        keycloaktokenFilterBean().doFilter(request, response, filterChain);
        //expecting filterChain to be invoked
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    public void testNoBearerToken() throws Exception {
        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("test123");

        keycloaktokenFilterBean().doFilter(request, response, filterChain);
        //expecting filterChain to be invoked
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
        //nothing should happen...token is just skipped
    }

    @Test
    public void testInvalidBearerToken() throws Exception {
        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer test123");

        keycloaktokenFilterBean().doFilter(request, response, filterChain);
        //expecting filterChain to be invoked
        Mockito.verify(filterChain, times(1)).doFilter(request, response);
        //nothing should happen...token is just skipped
    }

    @Test
    public void testValidJwtToken() throws Exception {
        //create new token for user 'user' with ADMINISTRATOR role in group USERS which expires in one hour
        final String token = JwtBuilder.createUserToken("user", RepoUserRole.ADMINISTRATOR)
                .addObjectClaim("groups", Arrays.asList("USERS", "TEAM1"))
                .getCompactToken(key, DateUtils.addHours(new Date(), 1));

        //add checks to check for user token
        doAnswer((Answer) new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                //validate token elements
                Authentication answer = (Authentication) invocation.getArguments()[0];

                Assertions.assertTrue(answer instanceof JwtAuthenticationToken);
                Assertions.assertTrue(answer instanceof JwtUserToken);
                Assertions.assertTrue(((JwtAuthenticationToken) answer).getGroups().contains("USERS"));
                Assertions.assertTrue(((JwtAuthenticationToken) answer).getGroups().contains("TEAM1"));
                Assertions.assertEquals(JwtAuthenticationToken.TOKEN_TYPE.USER, ((JwtUserToken) answer).getTokenType());
                Assertions.assertEquals("user", ((JwtUserToken) answer).getPrincipal());
                Assertions.assertTrue(((JwtUserToken) answer).getAuthorities().contains(new SimpleGrantedAuthority(RepoUserRole.ADMINISTRATOR.getValue())));

                Jws<Claims> jws = Jwts.parser().setSigningKey(key).build().parseClaimsJws(((JwtUserToken) answer).getToken());
                DefaultClaims claims = (DefaultClaims) jws.getBody();
                Assertions.assertTrue(claims.containsKey("tokenType"));
                Assertions.assertTrue(claims.containsKey("groups"));
                Assertions.assertTrue(claims.containsKey("username"));
                Assertions.assertTrue(claims.containsKey("roles"));
                Assertions.assertTrue(claims.containsKey("exp"));
                Assertions.assertTrue(claims.get("exp", Date.class).before(DateUtils.addHours(new Date(), 1)));
                return null;
            }
        }).when(context).setAuthentication(any(Authentication.class));
        SecurityContextHolder.setContext(context);

        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer " + token);

        keycloaktokenFilterBean().doFilter(request, response, filterChain);

        //disable checks again in order to allow to set service token
        doAnswer((Answer) new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(context).setAuthentication(any(Authentication.class));

    }

    @Test
    public void testUnauthorizedAccessDetected() throws Exception {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        //create new token for user 'test123' with ADMINISTRATOR role in group USERS which expires in one hour
        final String token = JwtBuilder.createUserToken("test123", RepoUserRole.ADMINISTRATOR).addObjectClaim("groups", Arrays.asList("USERS")).getCompactToken(key, DateUtils.addHours(new Date(), -1));

        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer " + token);
        keycloaktokenFilterBean().doFilter(request, response, filterChain);
      });
    }


    @Test
    public void testAllowedServiceToken() throws Exception {
        final String token = JwtBuilder.createServiceToken("MyService", RepoServiceRole.SERVICE_READ).addSimpleClaim("sources", "[\"localhost\"]").getCompactToken(key);

        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer " + token);

        keycloaktokenFilterBean().doFilter(request, response, filterChain);
    }

    @Test
    public void testUnallowedServiceToken() throws Exception {
      Assertions.assertThrows(InvalidAuthenticationException.class, () -> {
        final String token = JwtBuilder.createServiceToken("MyService", RepoServiceRole.SERVICE_READ).addSimpleClaim("sources", "[\"google.com\"]").getCompactToken(key);
        Mockito.when(request.getHeader(KeycloakTokenFilter.AUTHORIZATION_HEADER)).thenReturn("Bearer " + token);

        keycloaktokenFilterBean().doFilter(request, response, filterChain);
      });
    }

}
