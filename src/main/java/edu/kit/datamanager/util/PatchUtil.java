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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.kit.datamanager.util.json.JsonPatch;
import edu.kit.datamanager.util.json.JsonPatchUtil;
import edu.kit.datamanager.util.json.exceptions.JsonPatchProcessingException;
import edu.kit.datamanager.annotations.SecureUpdate;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.exceptions.PatchApplicationException;
import edu.kit.datamanager.exceptions.UpdateForbiddenException;
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;

/**
 * Helper class for applying patch operations to objects.
 *
 * @author jejkal
 */
public class PatchUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatchUtil.class);

    private PatchUtil() {
    }

    public static <C> C applyPatch(C resource, JsonPatch patch, Class<C> resourceClass, Collection<? extends GrantedAuthority> authorities) {
        ObjectMapper tmpObjectMapper = new ObjectMapper();
        tmpObjectMapper.registerModule(new JavaTimeModule());
        C updated;
        try {
            // Convert patch to String (which is independent of any implementations of ObjectMapper)
            String patchAsString = tmpObjectMapper.writeValueAsString(patch);
            // Apply the patch
            //applyJsonPatch(T original, String patchAsString, Class<T> type, PatchOptions options
            updated = JsonPatchUtil.applyPatch(resource, patchAsString , resourceClass);
        } catch (JsonProcessingException | JsonPatchProcessingException ex) {
            LOGGER.error("Failed to apply patch '" + patch.toString() + " to resource " + resource, ex);
            throw new PatchApplicationException("Failed to apply patch to resource.");
        }

        if (!PatchUtil.canUpdate(resource, updated, authorities)) {
            String message = "Patch not applicable.";
            LOGGER.warn(message);
            throw new UpdateForbiddenException(message);
        }

        return updated;
    }

    public static boolean canUpdate(Object originalObj, Object patched, Collection<? extends GrantedAuthority> authorities) {
        for (Field field : patched.getClass().getDeclaredFields()) {
            SecureUpdate secureUpdate = field.getAnnotation(SecureUpdate.class);
            if (secureUpdate != null) {
                try {
                    field.setAccessible(true);
                    Object persistedField = field.get(patched);
                    Object originalField = field.get(originalObj);
                    String[] allowedRoles = secureUpdate.value();

                    if (!Objects.equals(persistedField, originalField)) {
                        boolean canUpdate = false;
                        for (String role : allowedRoles) {//go though all roles allowed to update
                            for (GrantedAuthority authority : authorities) {//check owned authorities

                                String auth = authority.getAuthority();
                                if (auth.toLowerCase().startsWith("role") && role.toLowerCase().startsWith("role")) {//compare two roles
                                    if (authority.getAuthority().equalsIgnoreCase(role)) {//just use string comparison as the roles can be either user or group roles
                                        canUpdate = true;
                                        break;
                                    }
                                } else if (auth.toLowerCase().startsWith("permission") && role.toLowerCase().startsWith("permission")) {//compare two permissions
                                    PERMISSION userPermission = PERMISSION.fromValue(auth);
                                    PERMISSION permissionAccepted = PERMISSION.fromValue(role);
                                    if (userPermission.atLeast(permissionAccepted)) {
                                        canUpdate = true;
                                        break;
                                    }
                                } else {
                                    if (authority.getAuthority().equalsIgnoreCase(role)) {//comparison of plain strings...for testing
                                        canUpdate = true;
                                        break;
                                    }
                                }
                            }
                            if (canUpdate) {
                                //this field can be updated
                                break;
                            }
                        }
                        if (!canUpdate) {
                            //at least one field cannot be updated
                            LOGGER.warn("Updating of field " + field + " is allowed by " + Arrays.asList(allowedRoles) + ", but caller only offered the following authorities: " + authorities + ".");
                            return false;
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException | SecurityException e) {
                    LOGGER.error("Failed to check update applicability.", e);
                    throw new CustomInternalServerError("Unable to check if update is applicable. Message: " + e.getMessage());
                }
            }
        }

        return true;
    }

}
