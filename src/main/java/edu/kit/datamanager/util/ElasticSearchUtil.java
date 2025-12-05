/*
 * Copyright 2022 Karlsruhe Institute of Technology.
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import static edu.kit.datamanager.controller.SearchController.POST_FILTER;
import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.validator.SearchIndexValidator;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Util class for checking for a running Elastic instance at a given Url.
 *
 * @author jejkal
 */
public class ElasticSearchUtil {

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(ElasticSearchUtil.class);

  final static JsonNodeFactory factory = JsonNodeFactory.instance;

  public static final String RESULTS_FROM = "from";
  public static final String RESULTS_SIZE = "size";
  static final String SID_READ = "read";

  private static final int NO_OF_RETRIES = 3;

  /**
   * Test URL for pointing to a running elasticsearch instance.
   *
   * @param elasticsearchURL the given URL to check for an elasticsearch
   * instance.I
   * @return true if server is available.
   */
  public static boolean testForElasticsearch(URL elasticsearchURL) {
    boolean validElasticSearchServer = false;
    if (elasticsearchURL != null) {
      String baseUrl = elasticsearchURL.toString();
      // test for trailing '/'
      if (baseUrl.trim().endsWith("/")) {
        LOGGER.error("Invalid elasticsearch URL. Please remove trailing '/' from URL '{}'!", baseUrl);
      } else {
        String accessUrl = baseUrl + "/_search";
        RestTemplate restTemplate = new RestTemplate();
        int retries = 1;
        LOGGER.trace("Trying to connect to elasticsearch instance.");
        while (retries <= NO_OF_RETRIES) {
          try {
            ResponseEntity<String> entity = restTemplate.getForEntity(accessUrl,
                    String.class,
                    baseUrl);
            LOGGER.trace("Status code value: " + entity.getStatusCode().value());
            LOGGER.trace("HTTP Header 'ContentType': " + entity.getHeaders().getContentType());
            if (entity.getStatusCode().value() == HttpStatus.OK.value()) {
              LOGGER.info("Elasticsearch server at '{}' seems to be up and running!", baseUrl);
              validElasticSearchServer = true;
              break;
            } else {
              LOGGER.debug("Invalid response from elasticsearch server. Expected HTTP 200, received HTTP " + entity.getStatusCode().value() + ". Aborting.");
            }
          } catch (RestClientException ex) {
            LOGGER.warn("Failed accessing elasticsearch server.", ex);
          }
          LOGGER.warn("Attempt {}/{} failed!", retries, NO_OF_RETRIES);
          if (retries < NO_OF_RETRIES) {
            LOGGER.warn("Retrying in 5 seconds...");
            try {
              Thread.sleep(5000);
            } catch (InterruptedException ie) {
            }
          }
          retries++;
        }
      }
      if (!validElasticSearchServer) {
        LOGGER.trace("Unable to connect to elasticsearch instance at '{}' within '{}' attempts!", baseUrl, NO_OF_RETRIES);
      }
    } else {
      LOGGER.warn("No elasticsearch URL provided. Aborting.");
    }
    return validElasticSearchServer;
  }

  /**
   * Test if string is a valid elasticsearch index. If not - change to lower
   * case - replace all invalid characters by '_'
   *
   * @param elasticsearchIndex index to test.
   * @return valid index
   */
  public static String testForValidIndex(String elasticsearchIndex) {
    String validIndex = elasticsearchIndex;

    boolean valid = new SearchIndexValidator().isValid(validIndex, null);
    if (!valid) {
      String pattern = "[" + SearchIndexValidator.SPECIAL_CHARACTERS + "]";
      validIndex = validIndex.toLowerCase().replaceAll(pattern, "_");
    }
    return validIndex;
  }

  /**
   * Build post filter to restrict only to authorized results.
   *
   * @param queryNode Node holding query.
   */
  public static void buildPostFilter(ObjectNode queryNode) {
    // No need to add/edit post filter if user is an ADMINISTRATOR.
    if (!AuthenticationHelper.hasAuthority(RepoUserRole.ADMINISTRATOR.getValue())) {
      boolean havePostFilter = false;
      if (queryNode.has(POST_FILTER)) {
        LOGGER.warn("PostFilter found in provided query. Filter will be replaced!");
        havePostFilter = true;
      }

      JsonNode postFilter;
      /* Post filter may look like this: 
     {
       "bool" : {
         "should" : [
           { "match" : { "read" : "me" } },
           { "match" : { "read" : "everybody" } }
         ],
         "minimum_should_match" : 1
       }
     } 
       */
      LOGGER.trace("Adding PostFilter to elastic query.");
      ArrayNode arrayNode = factory.arrayNode();
      for (String sid : AuthenticationHelper.getAuthorizationIdentities()) {
        JsonNode match = factory.objectNode().set("match", factory.objectNode().put(SID_READ, sid));
        arrayNode.add(match);
      }
      ObjectNode should = factory.objectNode().set("should", arrayNode);
      should.put("minimum_should_match", 1);
      postFilter = factory.objectNode().set("bool", should);
      if (havePostFilter) {
        ArrayNode mustNode = (ArrayNode) queryNode.get(POST_FILTER).get("bool").get("must");
        mustNode.add(postFilter);
      } else {
        LOGGER.trace("PostFilter: '{}'", postFilter);
        queryNode.replace(POST_FILTER, postFilter);
      }
    }
  }

  /**
   * Add pagination to query if not already present.
   *
   * @param queryNode Node holding query.
   * @param page Number of the page.
   * @param size Size of the page.
   */
  public static void addPaginationInformation(ObjectNode queryNode, int page, int size) {
    if (queryNode.has(RESULTS_FROM) || queryNode.has(RESULTS_SIZE)) {
      LOGGER.trace("Provided query already specifies 'from' and/or 'size'. Ignoring pagination information from request.");
    } else {
      LOGGER.trace("Provided query does not specify 'from' and/or 'size'. Using pagination information with page {} and size {}", page, size);
      queryNode.replace(RESULTS_FROM, factory.numberNode(page * size));
      queryNode.replace(RESULTS_SIZE, factory.numberNode(size));
    }

  }
}
