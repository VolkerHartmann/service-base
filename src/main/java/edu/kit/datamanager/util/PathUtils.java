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

import edu.kit.datamanager.configuration.RepoBaseConfiguration;
import edu.kit.datamanager.domain.DataResource;
import edu.kit.datamanager.exceptions.CustomInternalServerError;
import edu.kit.datamanager.service.IRepoStorageService;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author jejkal
 */
public class PathUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(PathUtils.class);
  @Autowired(required = true)
  private static IRepoStorageService[] storageServices;

  private PathUtils() {
  }

  /**
   * Obtain the absolute data uri for the provided data resource and relative
   * data path. The final URI will contain the relative data data path appended
   * to a base URI depending on the repository configuration. In addition, the
   * current timestamp is appended in order to ensure that an existing data is
   * not touched until the transfer has finished. After the transfer has
   * finished, previously existing data can be removed securely.
   *
   * @param relativeDataPath The relative data path used to access the data.
   * @param parentResource The parent data resource.
   * @param basePath The base path of the repo. data base path.
   *
   * @return The data URI.
   */
  public static URI getDataUri(DataResource parentResource, String relativeDataPath, RepoBaseConfiguration properties) {
    try {
      String internalIdentifier = DataResourceUtils.getInternalIdentifier(parentResource);
      if (internalIdentifier == null) {
        throw new CustomInternalServerError("Data integrity error. No internal identifier assigned to resource.");
      }
      LOGGER.trace("Getting data URI for resource with id {} and relative path {}.", internalIdentifier, relativeDataPath);
      URIBuilder uriBuilder = new URIBuilder(properties.getBasepath().toURI());
      //uriBuilder.setCharset(Charset.forName("UTF-8"));
      uriBuilder.setPath(uriBuilder.getPath() + (!properties.getBasepath().toString().endsWith("/") ? "/" : "") + substitutePathPattern(parentResource, properties) + "/" + internalIdentifier + "/" + relativeDataPath + "_" + System.currentTimeMillis());
      URI result = uriBuilder.build();
      LOGGER.trace("Returning data URI {}.", result);
      return result;
    } catch (URISyntaxException ex) {
      throw new CustomInternalServerError("Failed to transform configured basepath to URI.");
    }
  }

  /**
   * Create path on base of actual date.
   *
   * @param properties
   * @return
   */
  public static String substitutePathPattern(DataResource resource, RepoBaseConfiguration properties) {
    String substitutePath = "dump";
    IRepoStorageService versioningService = properties.getStorageService();
    if (versioningService != null) {
      LOGGER.trace("Repo storage service found. Building relative path.");
      substitutePath = versioningService.createPath(resource);
    }
    return substitutePath;
  }

  public static String normalizePath(String path) {
    return normalizePath(path, true);
  }

  public static String normalizePath(String path, boolean removeOuterSlashes) {
    String normalizedPath = path.replaceAll("/+", "/");
    if (removeOuterSlashes) {
      //remove leading slash
      normalizedPath = normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
      //remove trailing slash
      normalizedPath = normalizedPath.endsWith("/") ? normalizedPath.substring(0, normalizedPath.length() - 1) : normalizedPath;
    }
    return normalizedPath;
  }

  public static int getDepth(String relativePath) {
    String normalizedPath = PathUtils.normalizePath(relativePath);
    return normalizedPath.split("/").length;
  }
}
