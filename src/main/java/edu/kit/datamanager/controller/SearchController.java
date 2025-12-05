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
package edu.kit.datamanager.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import edu.kit.datamanager.configuration.SearchConfiguration;
import edu.kit.datamanager.util.ElasticSearchUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller proxying the access to an Elastic search backend via an endpoint
 * at /api/v1/search. This endpoint is only available, if property
 * &lt;i&gt;repo.search.enabled&lt;/i&gt; is set 'true' in the service's
 * application.properties. Otherwise, this endpoint is not offered.
 *
 * @author jejkal
 */
@RestController
@RequestMapping(value = "/api/v1/")
@Schema(description = "Search controller for Elastic integration")
@ConditionalOnExpression("${repo.search.enabled:false}")
public class SearchController {

  static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private SearchConfiguration searchConfiguration;

  public static final String POST_FILTER = "post_filter";

  @Operation(operationId = "search",
          summary = "Search for resources.",
          description = "Search for resources using the configured Elastic backend. This endpoint serves as direct proxy to the RESTful endpoint of Elastic. "
          + "In the body, a query document following the Elastic query format has to be provided. Format errors are returned directly from Elastic. "
          + "This endpoint also supports authentication and authorization. User information obtained via JWT is applied to the provided query as "
          + "post filter. If a post filter was already provided with the query it will be replaced. Furthermore, this endpoint supports pagination. "
          + "'page' and 'size' query parameters are translated into the Elastic attributes 'from' and 'size' automatically, "
          + "if not already provided within the query by the caller.", security = {
            @SecurityRequirement(name = "bearer-jwt")})
  @RequestMapping(value = "/{index}/_search", method = RequestMethod.POST)
  @ResponseBody
  @PageableAsQueryParam
  public ResponseEntity<?> proxy(
          @PathVariable("index") final String index,
          @RequestBody JsonNode body,
          ProxyExchange<JsonNode> proxy,
          @Parameter(hidden = true) final Pageable pgbl) throws Exception {
    LOG.trace("Provided Elastic query: '{}'", body.toString());

    // Set or replace post-filter
    ObjectNode on = (ObjectNode) body;
    ElasticSearchUtil.addPaginationInformation(on, pgbl.getPageNumber(), pgbl.getPageSize());
      ElasticSearchUtil.buildPostFilter(on);

    LOG.trace("Forwarding Elastic query to {}.", searchConfiguration.getUrl() + "/" + index + "/_search");
    return proxy.uri(searchConfiguration.getUrl() + "/" + index + "/_search").post();
  }

  @Operation(operationId = "search",
          summary = "Search for resources.",
          description = "This endpoint is identical to _search but kept for "
          + "legacy reasons. In future implementations _search should "
          + "be used as Elatic also offers _search and some libraries "
          + "expect _search as default endpoint.", security = {
            @SecurityRequirement(name = "bearer-jwt")})
  @RequestMapping(value = "/search", method = RequestMethod.POST)
  @ResponseBody
  @PageableAsQueryParam
  public ResponseEntity<?> proxy_legacy(
          @RequestBody JsonNode body,
          ProxyExchange<JsonNode> proxy,
          @Parameter(hidden = true) final Pageable pgbl) throws Exception {
    return proxy(searchConfiguration.getIndex(), body, proxy, pgbl);
  }
}
