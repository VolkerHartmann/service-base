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
package edu.kit.datamanager.controller;

import edu.kit.datamanager.util.json.JsonPatch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.Instant;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Generic resource controller providing basic CRUD and search operations.
 *
 * @author jejkal
 * @param <C> Resource class
 */
public interface IGenericResourceController<C> {

    @Operation(operationId = "createResource",
            summary = "Create a new resource.",
            description = "Create a new resource and return it to the caller in case of success. "
            + "Creating new resources may or may not be restricted to users possessing specific roles.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<C> create(@Parameter(description = "Json representation of the resource to create.", required = true) @RequestBody C resource,
            final WebRequest request,
            final HttpServletResponse response);

    @Operation(operationId = "getResourceById",
            description = "Get a resource by id.",
            summary = "Obtain is single resource by its identifier. Depending on a user's role, accessing a specific resource may be allowed or forbidden.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<C> getById(@Parameter(description = "The resource identifier.", required = true) @PathVariable("id") final String id,
            @Parameter(description = "The version of the resource, if supported.", required = false) @RequestParam("version") final Long version,
            final WebRequest request,
            final HttpServletResponse response);


    @Operation(operationId = "listResources",
            summary = "List all resources.",
            description = "List all resources in a paginated and/or sorted form. Possible queries are: listing with default values (X elements on first page sorted by database), "
            + "listing page wise, sorted query page wise, and combinations of the options above. "
            + "The total number of resources may differ between callers if single resources have access restrictions. "
            + "Furthermore, anonymous listing of resources may or may not be supported.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    @PageableAsQueryParam
    public ResponseEntity<List<C>> findAll(
            @Parameter(description = "The UTC time of the earliest update of a returned resource.", example = "2017-05-10T10:41:00Z", required = false) @RequestParam(value = "The UTC time of the earliest update of a returned resource.", name = "from", required = false) final Instant lastUpdateFrom,
            @Parameter(description = "The UTC time of the latest update of a returned resource.", example = "2017-05-10T10:41:00Z", required = false) @RequestParam(name = "until", required = false) final Instant lastUpdateUntil,
            @Parameter(hidden = true) final Pageable pgbl,
            final WebRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder);

    @Operation(operationId = "findResources",
            summary = "List resources by example.",
            description = "List all resources in a paginated and/or sorted form by example using an example document provided in the request body. "
            + "The example is a normal instance of the resource. However, search-relevant top level primitives are marked as 'Searchable' within the implementation. "
            + "For string values, '%' can be used as wildcard character. "
            + "If the example document is omitted, the response is identical to listing all resources with the same pagination parameters. "
            + "As well as listing of all resources, the number of total results might be affected by the caller's role.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    @PageableAsQueryParam
    public abstract ResponseEntity<List<C>> findByExample(
            @Parameter(description = "Json representation of the resource serving as example for the search operation. Typically, only first level primitive attributes are evaluated while building queries from examples.", required = true) @RequestBody C example,
            @Parameter(description = "The UTC time of the earliest update of a returned resource.", example = "2017-05-10T10:41:00Z", required = false) @RequestParam(name = "from", required = false) final Instant lastUpdateFrom,
            @Parameter(description = "The UTC time of the latest update of a returned resource.", example = "2017-05-10T10:41:00Z", required = false) @RequestParam(name = "until", required = false) final Instant lastUpdateUntil,
            @Parameter(hidden = true) final Pageable pgbl,
            final WebRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder);

    @Operation(operationId = "patchResource",
            summary = "Patch a resource by id.",
            description = "Patch a single or multiple fields of a resource. Patching information are provided in JSON Patch format using Content-Type 'application/json-patch+json'. "
            + "Patching a resource requires privileged access to the resource to patch or ADMIN permissions of the caller. "
            + "Depending on the resource, single fields might be protected and cannot be changed, e.g. the unique identifier. "
            + "If the patch tries to modify a protected field, HTTP BAD_REQUEST will be returned before persisting the result.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = "application/json-patch+json")
    @ResponseBody
    public ResponseEntity patch(
            @Parameter(description = "The resource identifier.", required = true) @PathVariable("id") final String id,
            @Parameter(description = "Json representation of a json patch document. The document must comply with RFC 6902 specified by the IETF.", required = true) @RequestBody JsonPatch patch,
            final WebRequest request,
            final HttpServletResponse response);

    @Operation(operationId = "updateResource",
            summary = "Replace a resource.",
            description = "Replace a resource by a new resource provided by the user."
            + "Putting a resource requires privileged access to the resource to patch or ADMIN permissions of the caller. "
            + "Some resource fields might be protected and cannot be changed, e.g. the unique identifier. "
            + "If at least one protected field in the new resource does not match with the current value, HTTP BAD_REQUEST will be returned before persisting the result."
            + "Attention: Due to the availability of PATCH, PUT support is optional! If a resource won't provide PUT support, HTTP NOT_IMPLEMENTED is returned.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json")
    @ResponseBody
    public ResponseEntity put(
            @Parameter(description = "The resource identifier.", required = true) @PathVariable("id") final String id,
            @Parameter(description = "Json representation of the new representation of the resource.", required = true) @RequestBody C resource,
            final WebRequest request,
            final HttpServletResponse response);

    @Operation(operationId = "deleteResource",
            summary = "Delete a resource by id.",
            description = "Delete a single resource. Deleting a resource typically requires the caller to have ADMIN permissions. "
            + "In some cases, deleting a resource can also be available for the owner or other privileged users or can be forbidden. "
            + "For resources whose deletion may affect other resources or internal workflows, physical deletion might not be possible at all. "
            + "In those cases, the resource might be disabled/hidden but not removed from the database. This can then happen optionally at "
            + "a later point in time, either automatically or manually.", security = {
                @SecurityRequirement(name = "bearer-jwt")})
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity delete(
            @Parameter(description = "The resource identifier.", required = true) @PathVariable("id") final String id,
            final WebRequest request,
            final HttpServletResponse response);
}
