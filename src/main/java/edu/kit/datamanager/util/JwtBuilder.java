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
package edu.kit.datamanager.util;

import tools.jackson.databind.json.JsonMapper;
import edu.kit.datamanager.entities.RepoRole;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.security.filter.ScopedPermission;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

/**
 * Builder for internal JWT.
 *
 * @author jejkal
 */
public class JwtBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtBuilder.class);

    private final Map<String, Object> claims;
    private final List<String> roles;
    private final List<ScopedPermission> permissions;
    private String principal;
    private JwtAuthenticationToken.TOKEN_TYPE type = JwtAuthenticationToken.TOKEN_TYPE.USER;

    JwtBuilder() {
        claims = new HashMap<>();
        roles = new ArrayList<>();
        permissions = new ArrayList<>();
    }

    public static JwtBuilder createUserToken(String userName, RepoRole... roles) {
        JwtBuilder builder = new JwtBuilder();
        builder.setTokenType(JwtAuthenticationToken.TOKEN_TYPE.USER);
        builder.principal = userName;
        return builder.setRoles(roles);
    }

    public static JwtBuilder createServiceToken(String serviceName, RepoRole... roles) {
        JwtBuilder builder = new JwtBuilder();
        builder.setTokenType(JwtAuthenticationToken.TOKEN_TYPE.SERVICE);
        builder.principal = serviceName;
        return builder.setRoles(roles);
    }

    public static JwtBuilder createTemporaryToken(String principalName, ScopedPermission... permissions) {
        JwtBuilder builder = new JwtBuilder();
        builder.setTokenType(JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY);
        builder.principal = principalName;
        return builder.setScopedPermissions(permissions);
    }

    private void setTokenType(JwtAuthenticationToken.TOKEN_TYPE type) {
        claims.put("tokenType", type.toString());
        this.type = type;
    }

    private JwtBuilder setRoles(RepoRole... roles) {
        if (roles == null || type.equals(JwtAuthenticationToken.TOKEN_TYPE.TEMPORARY)) {
            return this;
        }
        for (RepoRole role : roles) {
            this.roles.add(role.getValue());
        }
        return this;
    }

    private JwtBuilder setScopedPermissions(ScopedPermission... permissions) {
        if (permissions == null || permissions.length == 0) {
            return this;
        }
        this.permissions.addAll(Arrays.asList(permissions));
        return this;
    }

    public JwtBuilder addSimpleClaim(String claimName, String value) {
        claims.put(claimName, value);
        return this;
    }

    public JwtBuilder addSimpleClaim(String claimName, Boolean value) {
        claims.put(claimName, value);
        return this;
    }

    public JwtBuilder addSimpleClaim(String claimName, Integer value) {
        claims.put(claimName, value);
        return this;
    }

    public JwtBuilder addObjectClaim(String claimName, Object value) {
        claims.put(claimName, value);
        return this;
    }

    public Map<String, Object> getClaimMap() {
        //put principal, roles or permissions into map
        try {
            switch (type) {
                case USER:
                    addSimpleClaim("username", principal);
                    claims.put("roles", new JsonMapper().writeValueAsString(roles.isEmpty() ? new String[]{RepoUserRole.GUEST.getValue()} : roles.toArray(new String[]{})));
                    break;
                case SERVICE:
                    addSimpleClaim("servicename", principal);
                    claims.put("roles", new JsonMapper().writeValueAsString(roles.isEmpty() ? new String[]{RepoUserRole.GUEST.getValue()} : roles.toArray(new String[]{})));
                    break;
                case TEMPORARY:
                    addSimpleClaim("principalname", principal);
                    claims.put("permissions", new JsonMapper().writeValueAsString(permissions.toArray(new ScopedPermission[]{})));
                    break;
                default:
                    LOGGER.warn("Invalid type {}. Leaving claims unchanged.", type);
            }
        } catch (JacksonException ex) {
            LOGGER.warn("Failed to create claim map.", ex);
        }
        return claims;
    }

    public Claims getClaims() {
        Map<String, Object> claimsMap = new HashMap<>();
        Map<String, Object> claimMap = getClaimMap();

        Set<Entry<String, Object>> entries = claimMap.entrySet();
        entries.forEach((entry) -> {
            claimsMap.put(entry.getKey(), entry.getValue());
        });
        return new DefaultClaims(claimsMap);
    }

    public String getCompactToken(String secret) {
        return getCompactToken(secret, null);
    }

    public String getCompactToken(String secret, Date expiresAt) {
        if (expiresAt != null) {
            return Jwts.builder().setClaims(getClaims()).setExpiration(expiresAt).signWith(SignatureAlgorithm.HS256, secret).compact();
        } else {
            LOGGER.debug("Warn: Creating JWT token without expiration time.");
            return Jwts.builder().setClaims(getClaims()).signWith(SignatureAlgorithm.HS256, secret).compact();
        }
    }

    public JwtAuthenticationToken getJwtAuthenticationToken(String secret) {
        return JwtAuthenticationToken.factoryToken(getCompactToken(secret), getClaimMap());
    }

}
