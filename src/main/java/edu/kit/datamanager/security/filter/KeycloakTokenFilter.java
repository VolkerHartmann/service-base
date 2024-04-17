/*
 * Copyright 2021 Karlsruhe Institute of Technology.
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
 *
 * This original version of this code is available at 
 *
 *  https://github.com/akoserwal/keycloak-jwt 
 * 
 * and was modified according to our requirements. 
 */
package edu.kit.datamanager.security.filter;

import com.nimbusds.jose.proc.BadJOSEException;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.util.NetworkUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter implementation for keycloak bearer tokens.
 *
 * @author akoserwa@redhat.com
 */
public class KeycloakTokenFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTokenFilter.class);

    private static final String BEARER = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    
    public static final String BASIC_AUTH_PREFIX = "BASIC";
    
    private final KeycloakTokenValidator tokenValidator;
    private JwtAuthenticationToken jwToken;

    /**
     * Default constructor.
     *
     * @param tokenValidator The token validator
     */
    public KeycloakTokenFilter(KeycloakTokenValidator tokenValidator) {
        this.tokenValidator = tokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        boolean contextSet = false;
        if (token != null && !token.toUpperCase().startsWith(BASIC_AUTH_PREFIX) && token.startsWith(BEARER)) {
            LOG.trace("Starting JWT filtering.");
            try {
                //remove Bearer prefix
                if (token.length() > BEARER.length()) {
                    token = token.substring(BEARER.length());
                }

                boolean localAuthenticationSucceeded = false;
                if (tokenValidator.supportsLocalJwt()) {
                    if (attemptLocalAuthentication(request, response, token)) {
                        LOG.trace("Authenticated using local JWT secret.");
                        localAuthenticationSucceeded = true;
                        contextSet = true;
                    }
                }

                if (!localAuthenticationSucceeded) {
                    //validate and return token
                    LOG.trace("Validating received JWT.");
                    jwToken = tokenValidator.validate(token);

                    LOG.trace("JWT validation finished. Checking result.");
                    if (jwToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        LOG.trace("Authenticated username: {}", jwToken.getPrincipal());

                        if (jwToken.getAuthorities().stream().filter(a -> a.getAuthority().equals(RepoUserRole.INACTIVE.toString())).count() > 0) {
                            LOG.debug("User roles contain ROLE_INACTIVE. Access denied for user.");
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized: User is marked inactive.");
                            return;
                        } else {
                            setContext(request, jwToken);
                            contextSet = true;
                        }
                    } else {
                        LOG.info("Invalid Request: Token is expired or tampered");
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Token is expired or tampered");
                        //stop filter chain, return now in order to stop handling of following requests
                        return;
                    }
                }
            } catch (BadJOSEException | IOException | MalformedJwtException e) {
                LOG.error("Failed to validate JWT.", e);
            }

        } else {
            LOG.info("Bearer token not being sent in Headers.");
        }

        //continue, either with another authentication method or without authentication
        if (!contextSet) {
            LOG.trace("Continue with filterChain as no valid JWT authentication was found.");
        }else{
            LOG.trace("Valid authentication context set from JWT. Continue with filterChain.");
        }
        filterChain.doFilter(request, response);
    }

    private boolean attemptLocalAuthentication(HttpServletRequest request, HttpServletResponse response, String token) {
        boolean authenticated = false;
        try {
            Jws<Claims> claimsJws = tokenValidator.getJwsClaims(token);
            Set<Map.Entry<String, Object>> claims = claimsJws.getBody().entrySet();
            Map<String, Object> claimMap = new HashMap<>();
            claims.forEach((entry) -> {
                claimMap.put(entry.getKey(), entry.getValue());
            });
            jwToken = JwtAuthenticationToken.factoryToken(token, claimMap);

            if (jwToken instanceof JwtServiceToken && ((JwtServiceToken) jwToken).getSources() != null) {
                JwtServiceToken serviceToken = (JwtServiceToken) jwToken;
                LOG.debug("Performing source check for JWToken for service {} and sources {}.", serviceToken.getPrincipal(), Arrays.asList(serviceToken.getSources()));
                String remoteAddr = request.getRemoteAddr();
                LOG.debug("Trying to match remote address {} with at least one allowed source.", remoteAddr);
                boolean matchFound = false;
                for (String source : serviceToken.getSources()) {
                    if (NetworkUtils.matches(remoteAddr, source)) {
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    LOG.warn("Invalid request from remote address {} to service {} found. Request denied.", remoteAddr, serviceToken.getPrincipal());
                    throw new InvalidAuthenticationException("You are not allowed to authenticate using the provided token from your current location.");
                }
            }
            LOG.trace("JWT validation finished. Checking result.");
            if (jwToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                LOG.info("Authenticated username: {}", jwToken.getPrincipal());
                setContext(request, jwToken);
            } else {
                try {
                    LOG.info("Invalid Request: Token is expired or tampered");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Token is expired or tampered");
                } catch (IOException ex) {
                    LOG.error("Failed to send Unauthorized response back to client.", ex);
                }
            }
            authenticated = true;
        } catch (ExpiredJwtException ex) {
            LOG.debug("Provided token has expired. Refresh of login required.", ex);
            throw new InvalidAuthenticationException("Your token has expired. Please refresh your login.");
        } catch (MalformedJwtException ex) {
            LOG.debug("Provided token is malformed.", ex);
            throw new MalformedJwtException("Your token is malformed.");
        } catch (IllegalArgumentException ex) {
            LOG.debug("Illegal argument exception while local authentication attempt.", ex);
        } catch (UnsupportedJwtException ex) {
            LOG.debug("Unsupported JwtException while local authentication attempt.", ex);
        }
        return authenticated;
    }

    /**
     * Set the final context.
     *
     * @param request The initial request.
     * @param token The obtained JWT.
     */
    private void setContext(HttpServletRequest request, JwtAuthenticationToken token) {
        LOG.trace("Setting authentication context.");
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(token);
        LOG.trace("Setting request attribute 'username' to {}.", token.getPrincipal());
        request.setAttribute("username", token.getPrincipal());
    }

    public boolean isValid() {
        return tokenValidator.isValid();
    }
}
