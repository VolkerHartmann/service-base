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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator implementation for keycloak bearer tokens.
 *
 * @author akoserwa@redhat.com
 */
public class KeycloakTokenValidator {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakTokenValidator.class);

    public static final String JWT_AUD = "aud";
    public static final String KEYCLOAK_GIVENNAME_CLAIM = "given_name";
    public static final String KEYCLOAK_FAMILYNAME_CLAIM = "family_name";
    public static final String KEYCLOAK_EMAIL_CLAIM = "email";
    public static final String KEYCLOAK_USERNAME_CLAIM = "preferred_user";
    public static final String KEYCLOAK_REALMACCESS_CLAIM = "realm_access";
    

    /**
     * keycloak certs url
     */
    private String jwkUrl;

    /**
     * Client ID
     */
    private String resource;

    /**
     * defined in keycloak mapper for client id: preferred_username or username
     */
    private String jwtClaim;
    /**
     * defined in keycloak mapper for group ids: default is groups
     */
    private String groupsClaim;
    /**
     * The HTTP connects timeout, in milliseconds, zero for infinite. Must not
     * be negative.
     */
    private int connectTimeoutms = 0;
    /**
     * The HTTP read timeout, in milliseconds, zero for infinite. Must not be
     * negative.
     */
    private int readTimeoutms = 0;
    /**
     * The HTTP entity size limit, in bytes, zero for infinite. Must not be
     * negative.
     */
    private int sizeLimit = 0;
    private boolean initialized = false;

    private String jwtLocalSecret = null;

    public void setJwtProcessor(ConfigurableJWTProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    private ConfigurableJWTProcessor jwtProcessor;

    private JWSKeySelector keySelector(JWKSource keySource) {
        return new JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource);
    }

    private void init() {
        if (jwkUrl != null) {
            LOG.info("Initializing JWK set from {}.", jwkUrl);
            try {
                JWKSet jwkSet = getJwkSet(jwkUrl);
                JWKSource keySource = new ImmutableJWKSet(jwkSet);
                jwtProcessor.setJWSKeySelector(keySelector(keySource));
                LOG.info("JWK set initialized successfully.");
                initialized = true;
            } catch (IOException | ParseException e) {
                throw new RuntimeException("Failed to initialize KeycloakTokenValidator.", e);
            }
        }
    }

    public JWKSet getJwkSet(String jwkUrl) throws IOException, ParseException {
        return JWKSet.load(new URL(jwkUrl), connectTimeoutms, readTimeoutms, sizeLimit);
    }

    public JwtAuthenticationToken validate(String accessToken) throws BadJOSEException {
        SecurityContext ctx = null;
        try {
            JWTClaimsSet claimsSet = getJwtClaimsSet(accessToken, ctx);
            if (claimsSet != null) {

                List<String> aud = claimsSet.getAudience();
                if (aud == null || !aud.contains(resource)) {
                    throw new BadJWTException("Invalid Keycloak Resource. Audience claim 'aud' is missing.");
                }

                //build auth token
                ArrayList aRoles = null;

                Map<String, Object> o = claimsSet.getJSONObjectClaim(KEYCLOAK_REALMACCESS_CLAIM);
                if (o != null) {
                    aRoles = (ArrayList) o.get(JwtAuthenticationToken.ROLES_CLAIM);
                    LOG.trace("Obtained roles {} from JWT.", aRoles);
                }

                if (aRoles == null) {
                    aRoles = new ArrayList();
                    aRoles.add("GUEST");
                    LOG.trace("No roles found in JWT. Using default roles {}.", aRoles);
                }

                String[] groupIds = claimsSet.getStringArrayClaim((groupsClaim == null) ? JwtAuthenticationToken.GROUPS_CLAIM : groupsClaim);
                if (groupIds != null) {
                    LOG.trace("Obtained group ids {} from JWT.", Arrays.asList(groupIds));
                }

                //token type? Service, Temp, User?
                Map<String, Object> claims = new HashMap<>();
                String roles = new ObjectMapper().writeValueAsString(aRoles);
                LOG.trace("Adding roles string {} to claims.", roles);
                claims.put(JwtAuthenticationToken.USERNAME_CLAIM, claimsSet.getStringClaim((jwtClaim == null) ? KEYCLOAK_USERNAME_CLAIM : jwtClaim));
                claims.put(JwtAuthenticationToken.FIRSTNAME_CLAIM, claimsSet.getStringClaim(KEYCLOAK_GIVENNAME_CLAIM));
                claims.put(JwtAuthenticationToken.LASTNAME_CLAIM, claimsSet.getStringClaim(KEYCLOAK_FAMILYNAME_CLAIM));
                claims.put(JwtAuthenticationToken.EMAIL_CLAIM, claimsSet.getStringClaim(KEYCLOAK_EMAIL_CLAIM));
                claims.put(JwtAuthenticationToken.ROLES_CLAIM, roles);
                if (groupIds != null) {
                    claims.put(JwtAuthenticationToken.GROUPS_CLAIM, Arrays.asList(groupIds));
                }
                JwtAuthenticationToken returnValue = null;
                returnValue = JwtAuthenticationToken.factoryToken(accessToken, claims);
                return returnValue;
            }
        } catch (RemoteKeySourceException e) {
            LOG.error("Failed to obtain remote key for JWT validation.", e);
        } catch (BadJWTException e) {
            LOG.warn("Invalid JWT received.", e);
        } catch (ParseException | JOSEException | JsonProcessingException e) {
            LOG.error("Failed to parse JWT.", e);
        }
        return null;
    }

    private JWTClaimsSet getJwtClaimsSet(String accessToken, SecurityContext ctx) throws ParseException, BadJOSEException, JOSEException {
        return jwtProcessor.process(accessToken, ctx);
    }

    protected Jws<Claims> getJwsClaims(String accessToken) {
        return Jwts.parser().verifyWith(new SecretKey(jwtLocalSecret)).build().parseClaimsJwt(accessToken);
    }

    public boolean supportsLocalJwt() {
        return Objects.nonNull(jwtLocalSecret);
    }

    public boolean isValid() {
        return initialized;
    }

    // Fluent API Builder
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        KeycloakTokenValidator accessTokenValidator;

        private Builder() {
            accessTokenValidator = new KeycloakTokenValidator();
        }

        public Builder connectTimeout(final int connectTimeout) {
            accessTokenValidator.connectTimeoutms = connectTimeout;
            return this;
        }

        public Builder readTimeout(final int readTimeout) {
            accessTokenValidator.readTimeoutms = readTimeout;
            return this;
        }

        public Builder sizeLimit(final int sizeLimit) {
            accessTokenValidator.sizeLimit = sizeLimit;
            return this;
        }

        public Builder jwtProcessor(ConfigurableJWTProcessor jwtProcessor) {
            accessTokenValidator.jwtProcessor = jwtProcessor;
            return this;
        }

        public Builder jwtLocalSecret(final String jwtLocalSecret) {
            accessTokenValidator.jwtLocalSecret = jwtLocalSecret;
            return this;
        }

        public KeycloakTokenValidator build(final String jwksetUrl, final String resource, final String jwt_username_claim) {
            accessTokenValidator.resource = resource;
            accessTokenValidator.jwtClaim = jwt_username_claim;
            accessTokenValidator.jwkUrl = jwksetUrl;

            if (accessTokenValidator.jwtProcessor == null && jwksetUrl != null) {
                accessTokenValidator.jwtProcessor = new DefaultJWTProcessor();
                accessTokenValidator.init();
            }

            return accessTokenValidator;
        }

        public KeycloakTokenValidator build(final String jwksetUrl, final String resource, final String jwt_username_claim, final String jwt_groups_claim) {
            accessTokenValidator.resource = resource;
            accessTokenValidator.jwtClaim = jwt_username_claim;
            accessTokenValidator.groupsClaim = jwt_groups_claim;
            accessTokenValidator.jwkUrl = jwksetUrl;

            if (accessTokenValidator.jwtProcessor == null && jwksetUrl != null) {
                accessTokenValidator.jwtProcessor = new DefaultJWTProcessor();
                accessTokenValidator.init();
            }

            return accessTokenValidator;
        }

    }
}
