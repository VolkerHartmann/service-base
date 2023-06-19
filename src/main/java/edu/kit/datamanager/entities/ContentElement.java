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
package edu.kit.datamanager.entities;

import lombok.Data;

/**
 * Entity for a single content element.
 * 
 * @author jejkal
 */
@Data
@SuppressWarnings("UnnecessarilyFullyQualified")
public class ContentElement{

  private String resourceId;
  private String relativePath;
  private String fileVersion;
  private long contentLength;
  private String contentUri;
  private String checksum;
  private String repositoryLocation;
  private String versioningService;

  private ContentElement(String resourceId, String relativePath, String contentUri, String version, String versioningService, String checksum, String repositoryLocation, Long contentLength){
    this.resourceId = resourceId;
    this.relativePath = relativePath;
    this.contentUri = contentUri;
    this.fileVersion = version;
    this.versioningService = versioningService;
    this.checksum = checksum;
    this.repositoryLocation = repositoryLocation;
    this.contentLength = contentLength;
  }

  public static ContentElement createContentElement(String resourceId, String relativePath, String contentUri, String fileVersion, String versioningService, String checksum, String repositoryLocation, Long contentLength){
    return new ContentElement(resourceId, relativePath, contentUri, fileVersion, versioningService, checksum, repositoryLocation, contentLength);
  }

  public static ContentElement createContentElement(String resourceId, String relativePath, String fileVersion, String versioningService, String checksum, String repositoryLocation, Long contentLength){
    return new ContentElement(resourceId, relativePath, null, fileVersion, versioningService, checksum, repositoryLocation, contentLength);
  }

  public static ContentElement createContentElement(String resourceId, String relativePath, String fileVersion, String versioningService, String repositoryLocation, Long contentLength){
    return new ContentElement(resourceId, relativePath, null, fileVersion, versioningService, null, repositoryLocation, contentLength);
  }

  public static ContentElement createContentElement(String resourceId, String relativePath, String fileVersion, String versioningService){
    return new ContentElement(resourceId, relativePath, null, fileVersion, versioningService, null, null, 0l);
  }
}
