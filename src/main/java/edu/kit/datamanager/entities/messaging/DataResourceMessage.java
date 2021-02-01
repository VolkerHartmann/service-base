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
package edu.kit.datamanager.entities.messaging;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jejkal
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class DataResourceMessage extends BasicMessage{

  public enum ACTION{
    CREATE("create"),
    UPDATE("update"),
    FIX("fix"),
    REVOKE("revoke"),
    DELETE("delete");

    private final String value;

    ACTION(String value){
      this.value = value;
    }

    public String getValue(){
      return value;
    }

    @Override
    public String toString(){
      return getValue();
    }
  }

  public enum SUB_CATEGORY{
    ACL("acl"),
    DATA("data");

    private final String value;

    SUB_CATEGORY(String value){
      this.value = value;
    }

    public String getValue(){
      return value;
    }

    @Override
    public String toString(){
      return getValue();
    }
  }

  public static final String CONTENT_PATH_PROPERTY = "contentPath";
  public static final String CONTENT_URI_PROPERTY = "contentUri";
  public static final String CONTENT_TYPE_PROPERTY = "contentType";

  public static DataResourceMessage factoryCreateMessage(String dataResourceId, String caller, String sender){
    return createMessage(dataResourceId, ACTION.CREATE, caller, sender);
  }

  public static DataResourceMessage factoryUpdateMessage(String dataResourceId, String caller, String sender){
    return createMessage(dataResourceId, ACTION.UPDATE, caller, sender);
  }

  public static DataResourceMessage factoryFixMessage(String dataResourceId, String caller, String sender){
    return createMessage(dataResourceId, ACTION.FIX, caller, sender);
  }

  public static DataResourceMessage factoryRevokeMessage(String dataResourceId, String caller, String sender){
    return createMessage(dataResourceId, ACTION.REVOKE, caller, sender);
  }

  public static DataResourceMessage factoryDeleteMessage(String dataResourceId, String caller, String sender){
    return createMessage(dataResourceId, ACTION.DELETE, caller, sender);
  }

  public static DataResourceMessage factoryCreateDataMessage(String dataResourceId, String contentPath, String contentUri, String contentType, String caller, String sender){
    Map<String, String> properties = new HashMap<>();
    properties.put(CONTENT_PATH_PROPERTY, contentPath);
    properties.put(CONTENT_URI_PROPERTY, contentUri);
    properties.put(CONTENT_TYPE_PROPERTY, contentType);
    return createSubCategoryMessage(dataResourceId, ACTION.CREATE, SUB_CATEGORY.DATA, properties, caller, sender);
  }

  public static DataResourceMessage factoryUpdateDataMessage(String dataResourceId, String contentPath, String contentUri, String contentType, String caller, String sender){
    Map<String, String> properties = new HashMap<>();
    properties.put(CONTENT_PATH_PROPERTY, contentPath);
    properties.put(CONTENT_URI_PROPERTY, contentUri);
    properties.put(CONTENT_TYPE_PROPERTY, contentType);
    return createSubCategoryMessage(dataResourceId, ACTION.UPDATE, SUB_CATEGORY.DATA, properties, caller, sender);
  }

  public static DataResourceMessage factoryDeleteDataMessage(String dataResourceId, String contentPath, String contentUri, String contentType, String caller, String sender){
    Map<String, String> properties = new HashMap<>();
    properties.put(CONTENT_PATH_PROPERTY, contentPath);
    properties.put(CONTENT_URI_PROPERTY, contentUri);
    properties.put(CONTENT_TYPE_PROPERTY, contentType);
    return createSubCategoryMessage(dataResourceId, ACTION.DELETE, SUB_CATEGORY.DATA, properties, caller, sender);
  }

  public static DataResourceMessage factoryUpdateAclMessage(String dataResourceId, String caller, String sender){
    return createSubCategoryMessage(dataResourceId, ACTION.UPDATE, SUB_CATEGORY.ACL, null, caller, sender);
  }

  public static DataResourceMessage createMessage(String dataResourceId, ACTION action, String principal, String sender){
    DataResourceMessage msg = new DataResourceMessage();
    msg.setEntityId(dataResourceId);
    msg.setAction(action.getValue());
    msg.setPrincipal(principal);
    msg.setSender(sender);
    msg.setCurrentTimestamp();
    return msg;
  }

  public static DataResourceMessage createSubCategoryMessage(String dataResourceId, ACTION action, SUB_CATEGORY subCategory, Map<String, String> properties, String principal, String sender){
    DataResourceMessage msg = new DataResourceMessage();
    msg.setEntityId(dataResourceId);
    msg.setAction(action.getValue());
    msg.setSubCategory(subCategory.getValue());
    msg.setPrincipal(principal);
    msg.setSender(sender);
    if(properties != null){
      msg.setMetadata(properties);
    }
    msg.setCurrentTimestamp();
    return msg;
  }

  @Override
  public String getEntityName(){
    return "dataresource";
  }
  
  public static void main(String[] args) throws Exception{
    DataResourceMessage msg = DataResourceMessage.factoryCreateMessage("test123", "me", "you");
    System.out.println(msg.toJson());
    
  }
  
  }
