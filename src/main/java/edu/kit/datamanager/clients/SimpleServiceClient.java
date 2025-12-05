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
package edu.kit.datamanager.clients;

import tools.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.repo.DataResource;
import edu.kit.datamanager.util.ControllerUtils;
import edu.kit.datamanager.util.ControllerUtils.ContentRange;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Helper class to access services via REST.
 *
 * @author jejkal
 */
@SuppressWarnings("UnnecessarilyFullyQualified")
public class SimpleServiceClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServiceClient.class);
  // define some log messages used multiple times. 
  private static final String OBTAINING_RESOURCE = "Obtaining resource from resource URI {}.";
  private static final String RETURN_STATUS = "Request returned with status {}. Returning response body.";

  private RestTemplate restTemplate = new RestTemplate();

  private final String resourceBaseUrl;
  private String resourcePath = null;
  private HttpHeaders headers;
  private Map<String, String> requestedResponseHeaders = null;

  MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
  MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();

  SimpleServiceClient(String resourceBaseUrl) {
    this.resourceBaseUrl = resourceBaseUrl;
    headers = new HttpHeaders();
  }

  /**
   * Set template for REST access.
   *
   * @param restTemplate Template for REST Access.
   */
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;

  }

  /**
   * Create service client.
   *
   * @param baseUrl Base URL of the service.
   * @return Service client.
   */
  public static SimpleServiceClient create(String baseUrl) {
    return new SimpleServiceClient(baseUrl);
  }

  /**
   * Add bearer token to service client.
   *
   * @param bearerToken Bearer token.
   * @return Service client with authentication.
   */
  public SimpleServiceClient withBearerToken(String bearerToken) {
    if (bearerToken != null) {
      return withHeader("Authorization", "Bearer " + bearerToken);
    }
    headers.remove("Authorization");
    return this;
  }

  /**
   * Add header to service client.
   *
   * @param field Key of the header field.
   * @param value Value of the header field.
   * @return Service client with header.
   */
  public SimpleServiceClient withHeader(String field, String value) {
    this.headers.add(field, value);
    return this;
  }

  /**
   * Set accepted mimetypes.
   *
   * @param mediaType Array of valid mimetypes.
   * @return Service client with accept-Header.
   */
  public SimpleServiceClient accept(MediaType... mediaType) {
    headers.setAccept(Arrays.asList(mediaType));
    return this;
  }

  /**
   * Add map for response header.
   *
   * @param container Map for response header.
   * @return Service client.
   */
  public SimpleServiceClient collectResponseHeader(Map<String, String> container) {
    requestedResponseHeaders = container;
    return this;
  }

  /**
   * Set content type.
   *
   * @param contentType Content type.
   * @return Service client.
   */
  public SimpleServiceClient withContentType(MediaType contentType) {
    headers.setContentType(contentType);
    return this;
  }

  /**
   * Add path for resources.
   *
   * @param resourcePath Resource path.
   * @return Service client.
   */
  public SimpleServiceClient withResourcePath(String resourcePath) {
    LOGGER.trace("Creating SingleResourceAccessClient with resourcePath {}.", resourcePath);
    this.resourcePath = resourcePath;
    return this;
  }

  /**
   * Add form parameter.
   *
   * @param name Name of the parameter.
   * @param object Object containing parameter.
   * @return Service client.
   * @throws IOException Error while reading parameter.
   */
  public SimpleServiceClient withFormParam(String name, Object object) throws IOException {
    if (name == null || object == null) {
      throw new IllegalArgumentException("Form element key and value must not be null.");
    }
    if (object instanceof File) {
      body.add(name, new FileSystemResource((File) object));
    } else if (object instanceof InputStream) {
      body.add(name, new ByteArrayResource(IOUtils.toByteArray((InputStream) object)) {
        //overwriting filename required by spring (see https://medium.com/@voziv/posting-a-byte-array-instead-of-a-file-using-spring-s-resttemplate-56268b45140b)
        @Override
        public String getFilename() {
          return "stream#" + UUID.randomUUID();
        }
      });
    } else {
      String metadataString = new ObjectMapper().writeValueAsString(object);
      LOGGER.trace("Adding argument from JSON document {}.", metadataString);
      body.add(name, new ByteArrayResource(metadataString.getBytes()) {
        //overwriting filename required by spring (see https://medium.com/@voziv/posting-a-byte-array-instead-of-a-file-using-spring-s-resttemplate-56268b45140b)
        @Override
        public String getFilename() {
          return "metadata#" + UUID.randomUUID() + ".json";
        }
      });
    }
    return this;
  }

  /**
   * Add query parameter.
   *
   * @param name Name of query parameter.
   * @param value Value of query parameter.
   * @return Service client.
   */
  public SimpleServiceClient withQueryParam(String name, String value) {
    queryParams.add(name, value);
    return this;
  }

  /**
   * Get Resource of response.
   *
   * @param <C> Type of response.
   * @param responseType Class of response.
   * @return Instance of response class.
   */
  public <C> C getResource(Class<C> responseType) {
    C returnValue = null;
    LOGGER.trace("Calling getResource().");
    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);
    LOGGER.trace(OBTAINING_RESOURCE, uriBuilder.toUriString());
    ResponseEntity<C> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), responseType);
    HttpStatusCode statusCode = response.getStatusCode();
    LOGGER.trace("Request to '{}' returned with status {}.", destinationUri, statusCode.value());
    if (statusCode.is2xxSuccessful()) {
      collectResponseHeaders(response.getHeaders());
      returnValue = response.getBody();
    }
    if (statusCode.is3xxRedirection()) {
      SimpleServiceClient redirectedClient = cloneRequestForRedirect(response.getHeaders().getLocation());
      returnValue = redirectedClient.getResource(responseType);
    }
    LOGGER.trace("Returning response body.");
    return returnValue;
  }

  /**
   * Get multiple resources.
   *
   * @param <C> Type of response.
   * @param responseType Class of response.
   * @return Page holding all responses.
   */
  public <C> ResultPage<C> getResources(Class<C[]> responseType) {
    ResultPage<C> returnValue = null;
    LOGGER.trace("Calling getResources().");
    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);
    LOGGER.trace(OBTAINING_RESOURCE, uriBuilder.toUriString());
    ResponseEntity<C[]> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), responseType);
    HttpStatusCode statusCode = response.getStatusCode();
    LOGGER.trace("Request to '{}' returned with status {}.", destinationUri, response.getStatusCode().value());
    if (statusCode.is2xxSuccessful()) {
      ContentRange contentRange = ControllerUtils.parseContentRangeHeader(response.getHeaders().getFirst("Content-Range"));
      collectResponseHeaders(response.getHeaders());
      returnValue = new ResultPage<>(response.getBody(), contentRange);
    }
    if (statusCode.is3xxRedirection()) {
      SimpleServiceClient redirectedClient = cloneRequestForRedirect(response.getHeaders().getLocation());
      returnValue = redirectedClient.getResources(responseType);
    }
    LOGGER.trace("Returning response body.");

    return returnValue;
  }

  /**
   * Find resource using provided example.
   *
   * @param <C> Type of response.
   * @param resource Example instance.
   * @param responseType Class of response.
   * @return Page holding all responses.
   */
  public <C> ResultPage<C> findResources(C resource, Class<C[]> responseType) {
    LOGGER.trace("Calling findResources().");
    String destinationUri = resourceBaseUrl + "search";
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);
    LOGGER.trace(OBTAINING_RESOURCE, uriBuilder.toUriString());
    ResponseEntity<C[]> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST, new HttpEntity<>(resource, headers), responseType);
    LOGGER.trace("Request returned with status {}. Returning response body.", response.getStatusCode().value());
    ContentRange contentRange = ControllerUtils.parseContentRangeHeader(response.getHeaders().getFirst("Content-Range"));
    collectResponseHeaders(response.getHeaders());
    return new ResultPage<>(response.getBody(), contentRange);
  }

  /**
   * Get resource.
   *
   * @param outputStream OutputStream for the resource.
   * @return Status.
   */
  public int getResource(OutputStream outputStream) {
    String sourceUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(sourceUri).queryParams(queryParams);
    LOGGER.trace("Downloading content from source URI {}.", uriBuilder.toUriString());

    RequestCallback requestCallback = request -> {
      Set<Entry<String, List<String>>> entries = headers.headerSet();
      entries.forEach(
              entry -> request.getHeaders().addAll(entry.getKey(), entry.getValue())
      );
    };

    ResponseExtractor<ClientHttpResponse> responseExtractor = response -> {
      IOUtils.copy(response.getBody(), outputStream);

      return response;
    };

    ClientHttpResponse response = restTemplate.execute(uriBuilder.toUriString(), HttpMethod.GET, requestCallback, responseExtractor);
    int status = -1;
    try {
      status = response.getStatusCode().value();
      LOGGER.trace("Download returned with status {}.", status);
      collectResponseHeaders(response.getHeaders());
    } catch (IOException ex) {
      LOGGER.error("Failed to extract raw status from response.", ex);
    }
    return status;
  }

  /**
   * Post resource.
   *
   * @param <C> Type of response.
   * @param resource Instance to post.
   * @param responseType Class of response.
   * @return Posted resource.
   */
  public <C> C postResource(C resource, Class<C> responseType) {
    LOGGER.trace("Calling createResource(#DataResource).");

    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);

    LOGGER.trace("Sending POST request for resource.");
    ResponseEntity<C> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST, new HttpEntity<>(resource, headers), responseType);
    LOGGER.trace("Request returned with status {}. Returning response body.", response.getStatusCode().value());
    collectResponseHeaders(response.getHeaders());
    return response.getBody();
  }

  /**
   * Post form.
   *
   * @return Status of post.
   */
  public HttpStatus postForm() {
    return postForm(MediaType.MULTIPART_FORM_DATA);
  }

  /**
   * Post form with given content type.
   *
   * @param contentType Content type.
   * @return Status of post.
   */
  public HttpStatus postForm(MediaType contentType) {
    LOGGER.trace("Adding content type header with value {}.", contentType);
    headers.setContentType(contentType);

    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);

    LOGGER.trace("Uploading content to destination URI {}.", uriBuilder.toUriString());
    ResponseEntity<String> response = restTemplate.postForEntity(uriBuilder.toUriString(), new HttpEntity<>(body, headers), String.class);
    LOGGER.trace("Upload returned with status {}.", response.getStatusCode().value());
    collectResponseHeaders(response.getHeaders());
    return HttpStatus.resolve(response.getStatusCode().value());

  }

  /**
   * Put resource.
   *
   * @param <C> Type of response.
   * @param resource Instance to put.
   * @param responseType Class of response.
   * @return Puted resource.
   */
  public <C> C putResource(C resource, Class<C> responseType) {
    LOGGER.trace("Calling updateResource(#DataResource).");

    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);
    LOGGER.trace(OBTAINING_RESOURCE, uriBuilder.toUriString());
    ResponseEntity<C> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), responseType);
    LOGGER.trace("Reading ETag from response header.");
    String etag = response.getHeaders().getFirst("ETag");
    LOGGER.trace("Sending PUT request for resource with ETag {}.", etag);
    headers.setIfMatch(etag);
    response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.PUT, new HttpEntity<>(resource, headers), responseType);
    collectResponseHeaders(response.getHeaders());
    LOGGER.trace("Request returned with status {}. Returning response body.", response.getStatusCode().value());
    return response.getBody();
  }

  /**
   * Delete a resource. This call, if supported and authorized, should always
   * return without result.
   */
  public void deleteResource() {
    LOGGER.trace("Calling delete().");
    String destinationUri = resourceBaseUrl + ((resourcePath != null) ? resourcePath : "");
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(destinationUri).queryParams(queryParams);
    LOGGER.trace(OBTAINING_RESOURCE, uriBuilder.toUriString());
    ResponseEntity<DataResource> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), DataResource.class);
    LOGGER.trace("Reading ETag from response header.");
    String etag = response.getHeaders().getFirst("ETag");
    LOGGER.trace("Obtained ETag value {}.", etag);

    LOGGER.trace("Sending DELETE request for resource with ETag {}.", etag);
    headers.setIfMatch(etag);
    response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.DELETE, new HttpEntity<>(headers), DataResource.class);
    collectResponseHeaders(response.getHeaders());
    LOGGER.trace("Request returned with status {}. No response body expected.", response.getStatusCode().value());
  }

  /**
   * Collect all response headers.
   *
   * @param responseHeaders Response headers.
   */
  private void collectResponseHeaders(HttpHeaders responseHeaders) {
    if (requestedResponseHeaders != null) {
      Set<Entry<String, String>> entries = requestedResponseHeaders.entrySet();

      entries.forEach(
              entry -> requestedResponseHeaders.put(entry.getKey(), responseHeaders.getFirst(entry.getKey()))
      );
    }
  }

  /**
   * Clone the request for the redirected URI.
   *
   * @param newUri redirected URI
   * @return new client.
   */
  private SimpleServiceClient cloneRequestForRedirect(URI newUri) {
    SimpleServiceClient redirectedService = create(newUri.toString());
    redirectedService.body = body;
    redirectedService.headers = headers;
    redirectedService.queryParams = queryParams;
    redirectedService.resourcePath = resourcePath;
    redirectedService.restTemplate = restTemplate;
    return redirectedService;
  }

  /**
   * Result page holding instance of type 'C'.
   *
   * @param <C> Type of response:
   */
  @Data
  public static class ResultPage<C> {

    /**
     * Constructor.
     *
     * @param resources Array holding all resources.
     * @param range Given range if number of resources is restricted.
     */
    public ResultPage(C[] resources, ControllerUtils.ContentRange range) {
      this.resources = resources;
      this.contentRange = range;
    }

    ControllerUtils.ContentRange contentRange;
    C[] resources;
  }

  /**
   * Sort response.
   */
  @Data
  public static class SortField {

    /**
     * Select direction for sorting.
     */
    public enum DIR {

      /**
       * Ascending
       */
      ASC,
      /**
       * Descending
       */
      DESC;
    }

    String fieldName;
    DIR direction;

    /**
     * Define field for sorting!
     *
     * @param fieldName Field to sort.
     * @param direction Ascending or descending.
     */
    public SortField(String fieldName, DIR direction) {
      this.fieldName = fieldName;
      this.direction = direction;
    }

    /**
     * Transform sort to query parameter.
     *
     * @return Query part of URL.
     */
    public String toQueryParam() {
      return fieldName + ((direction != null) ? "," + direction.toString().toLowerCase() : "");
    }

  }
}
