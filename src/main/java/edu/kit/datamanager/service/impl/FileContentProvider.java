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
package edu.kit.datamanager.service.impl;

import edu.kit.datamanager.entities.ContentElement;
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import edu.kit.datamanager.service.IRepoVersioningService;
import edu.kit.datamanager.service.IContentProvider;
import edu.kit.datamanager.service.IRepoVersioningService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
@Component
public class FileContentProvider implements IContentProvider{

  @Autowired
  private Logger logger;
  @Autowired(required = false)
  private IRepoVersioningService[] versioningServices;

  @Override
  public void provide(ContentElement contentElement, MediaType mediaType, String filename, HttpServletResponse response){

    if(versioningServices == null){
      throw new CustomInternalServerError("No versioning service found. Unable to provide any content.");
    }

    logger.trace("Providing content element {}.", contentElement);
    try{
      logger.trace("Checking for proper versioning service named {}.", contentElement.getVersioningService());
      for(IRepoVersioningService versioningService : versioningServices){
        if(versioningService.getServiceName().equals(contentElement.getVersioningService())){
          logger.trace("Versioning service found. Building response.");
          response.setStatus(HttpStatus.OK.value());
          if(mediaType != null){
            response.setHeader("Content-Type", mediaType.toString());
          }
          if(contentElement.getContentLength() > 0){
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentElement.getContentLength()));
          }
          Map<String, String> options = new HashMap<>();
          options.put("contentUri", contentElement.getContentUri());
          options.put("checksum", contentElement.getChecksum());
          options.put("size", Long.toString(contentElement.getContentLength()));
          options.put("mediaType", (mediaType != null) ? mediaType.toString() : "<unknown>");
          logger.trace("Forwarding request to versioning service.");
          versioningService.read(contentElement.getResourceId(), null, contentElement.getRelativePath(), contentElement.getFileVersion(), response.getOutputStream(), options);
          break;
        }
      }
    } catch(IOException ex){
      logger.error("Failed to send content to response.", ex);
      throw new CustomInternalServerError("Failed to read content from repository.");
    } catch(Throwable t){
      logger.error("Unknown error while reading content from versioning service.", t);
      throw t;
    }
  }

  @Override
  public boolean canProvide(String schema){
    return "file".equals(schema);
  }

}
