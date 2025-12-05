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
package edu.kit.datamanager.security.filter;

import com.nimbusds.jose.util.StandardCharset;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Filter adding security context for unauthenticated user.
 *
 * User has role 'ROLE_ANONYMOUS' and username 'anonymousUser'.
 *
 * It has to be added after other filters:  <code>
 *    logger.info("Add keycloak filter!");
 *     httpSecurity.addFilterAfter(keycloaktokenFilterBean.get(), BasicAuthenticationFilter.class);
 *     logger.info("Add public authentication filter!");
 *     httpSecurity = httpSecurity.addFilterAfter(new PublicAuthenticationFilter(applicationProperties.getJwtSecret()), BasicAuthenticationFilter.class);
 * </code>
 */
public class PublicAuthenticationFilter extends OncePerRequestFilter {

    public static final String PUBLIC_USER = "anonymousUser";
    public static final String ROLE_PUBLIC_READ = "ROLE_ANONYMOUS";

    private static final Logger LOG = LoggerFactory.getLogger(PublicAuthenticationFilter.class);

    private final String secretKey;

    private static final String USERS_GROUP = "PUBLIC";

    public PublicAuthenticationFilter(String secretKey) {
        this.secretKey = secretKey;
    }

    @SuppressWarnings("JavaUtilDate")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException, AuthenticationException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            LOG.trace("Set public authorization!");
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put(JwtAuthenticationToken.GROUPS_CLAIM, Arrays.asList(USERS_GROUP));
            claimsMap.put(JwtAuthenticationToken.TOKENTYPE_CLAIM, JwtAuthenticationToken.TOKEN_TYPE.USER.toString());
            claimsMap.put(JwtAuthenticationToken.USERNAME_CLAIM, PUBLIC_USER);

            Set<String> rolesAsString = new HashSet<>();
            rolesAsString.add(ROLE_PUBLIC_READ);
            try {
                claimsMap.put(JwtAuthenticationToken.ROLES_CLAIM, new ObjectMapper().writeValueAsString(rolesAsString.toArray(String[]::new)));
            } catch (JacksonException ex) {
                throw new InvalidAuthenticationException("Failed to create JWToken.", ex);
            }
            /* Set<Map.Entry<String, Object>> claimEntries = claims.entrySet();
      Map<String, Object> claimMap = new HashMap<>();
      claimEntries.forEach(entry -> claimMap.put(entry.getKey(), entry.getValue()));
             */
            Key key = new SecretKeySpec(secretKey.getBytes(StandardCharset.UTF_8), "HmacSHA256");
            String token = Jwts.builder().claims(claimsMap).expiration(Date.from(Instant.now().plus(1l, ChronoUnit.HOURS))).signWith(key).compact();
            JwtAuthenticationToken res = JwtAuthenticationToken.factoryToken(token, claimsMap);
            SecurityContextHolder.getContext().setAuthentication(res);
        } else {
            LOG.trace("Nothing to do as user is already authenticated!");
        }
        //continue filtering
        chain.doFilter(request, response);

    }
}
