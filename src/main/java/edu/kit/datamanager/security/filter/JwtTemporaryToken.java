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

import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;

/**
 *
 * @author jejkal
 */
public class JwtTemporaryToken extends JwtAuthenticationToken{

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTemporaryToken.class);

  private ScopedPermission[] scopedPermissions = new ScopedPermission[0];

  public JwtTemporaryToken(String token, Collection<? extends GrantedAuthority> authorities){
    super(token, authorities);
  }

  @Override
  public String[] getSupportedClaims(){
    return new String[]{PRINCIPALNAME_CLAIM, PERMISSIONS_CLAIM};
  }

  @Override
  public Class getClassForClaim(String claim){
//    switch(claim){
//      case "principalname":
//        return String.class;
//      case "permissions":
//        return String.class;
//    }
    return String.class;
  }

  @Override
  public void setValueFromClaim(String claim, Object value){
    switch(claim){
      case PRINCIPALNAME_CLAIM:
        setPrincipalName((String) value);
        break;
      case PERMISSIONS_CLAIM:
        parsePermissions((String) value);
        break;
      default:
        LOGGER.warn("Invalid claim {} with value {} received. Claim will be ignored.", claim, value);
    }
  }

  @Override
  public void validate() throws InvalidAuthenticationException{
    if(scopedPermissions == null || scopedPermissions.length == 0){
      throw new InvalidAuthenticationException("Invalid token. No permissions found.");
    } else{
      for(ScopedPermission permission : scopedPermissions){
        if(permission.getPermission().atLeast(PERMISSION.WRITE)){
          //TODO: Allow to configure automatic limitation of permission?
          LOGGER.info("Scoped permission for resource type {} and resource id {} has value {}. "
                  + "For security reasons it is highly recommended to grant only READ permissions for temporary access tokens.",
                  permission.getResourceType(), permission.getResourceId(), permission.getPermission());
        }
      }
    }
  }

  private void parsePermissions(String value){
    if(value == null){
      throw new InvalidAuthenticationException("Mandatory claim 'permissions' has value 'null'.");
    }
    ObjectMapper mapper = new ObjectMapper();
//    try{
      scopedPermissions = mapper.readValue(value, ScopedPermission[].class);
//    } catch(IOException ex){
//      throw new InvalidAuthenticationException("Failed to read scoped permissions from claim value " + value + ".");
//    }
  }

  public ScopedPermission[] getScopedPermissions(){
    return scopedPermissions;
  }

  @Override
  public TOKEN_TYPE getTokenType(){
    return TOKEN_TYPE.TEMPORARY;
  }

}
