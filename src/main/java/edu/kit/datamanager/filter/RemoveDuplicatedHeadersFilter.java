/*
 * Copyright 2025 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.filter;

import edu.kit.datamanager.configuration.SearchConfiguration;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class RemoveDuplicatedHeadersFilter extends OncePerRequestFilter {

  @Autowired
  private SearchConfiguration searchConfiguration;


  /**
   * Logger for this class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoveDuplicatedHeadersFilter.class);

  private static final Pattern SEARCH_PATTERN = Pattern.compile("(/[^/]+)?/api/v1(/metadata)?(/[^/]+)?/_?search");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
      Set<String> definedHeaders = new HashSet<>();

      @Override
      public void setHeader(String name, String value) {
        boolean setHeader = true;
        String headerName = name.toLowerCase();
        LOGGER.trace("Deduplication headers: '{}'", searchConfiguration.getDedupHeaders());
        LOGGER.trace("Headername: '{}'", headerName);
        LOGGER.trace("Defined headers: '{}'", definedHeaders);
        LOGGER.trace("Setting header '{}' to value '{}'.", name, value);
        // Check if header should be deduplicated
        if (searchConfiguration.getDedupHeaders().contains(headerName)) {
          LOGGER.trace("Header '{}' is in deduplication list.", name);
          // Check if header is already defined
          if (definedHeaders.contains(headerName)) {
            LOGGER.trace("Header '{}' already defined. Skip setting it again to value '{}'.", name, value);
            setHeader = false;
          } else {
            LOGGER.trace("Header '{}' not yet defined. Add it to defined headers.", name);
            definedHeaders.add(headerName);
          }
        }
        if (setHeader) {
          super.setHeader(name, value);
        }
      }

      @Override
      public void addHeader(String name, String value) {
        setHeader(name, value);
      }
    };

    filterChain.doFilter(request, responseWrapper);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Deduplication headers: '{}'", searchConfiguration.getDedupHeaders());
      for (String header : responseWrapper.getHeaderNames()) {
        LOGGER.trace("Header '{}': '{}'", header, responseWrapper.getHeader(header));
      }
    }
  }

  @Override
  public void destroy() {
    // Cleanup code, if needed
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    boolean doNotFilter = !SEARCH_PATTERN.matcher(path).matches();
    LOGGER.trace("Filter this request for path '{}': '{}'", path, !doNotFilter);

    return doNotFilter;
  }
}