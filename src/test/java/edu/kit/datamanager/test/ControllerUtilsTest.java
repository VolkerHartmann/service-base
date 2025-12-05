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
package edu.kit.datamanager.test;

import edu.kit.datamanager.entities.RepoUserRole;
import edu.kit.datamanager.exceptions.*;
import edu.kit.datamanager.security.filter.JwtAuthenticationToken;
import edu.kit.datamanager.util.ControllerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.WebRequest;

import java.net.InetAddress;
import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author jejkal
 */
public class ControllerUtilsTest {

    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    private String key = "vkfvoswsohwrxgjaxipuiyyjgubggzdaqrcuupbugxtnalhiegkppdgjgwxsmvdb";

    @Test
    public void testCheckPaginationInformation() {
        PageRequest request = PageRequest.of(0, 10, Sort.unsorted());
        request = ControllerUtils.checkPaginationInformation(request);

        Assertions.assertEquals(0, request.getPageNumber());
        Assertions.assertEquals(10, request.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), request.getSort());
    }

    @Test
    public void testCheckPaginationInformationWithTooLargePage() {
        PageRequest request = PageRequest.of(0, 101, Sort.unsorted());
        request = ControllerUtils.checkPaginationInformation(request);

        Assertions.assertEquals(0, request.getPageNumber());
        Assertions.assertEquals(100, request.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), request.getSort());
    }

    @Test
    public void testCheckPaginationInformationWithMaxPageSize() {
        PageRequest request = PageRequest.of(0, 100, Sort.unsorted());
        request = ControllerUtils.checkPaginationInformation(request);

        Assertions.assertEquals(0, request.getPageNumber());
        Assertions.assertEquals(100, request.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), request.getSort());
    }

    @Test
    public void testCheckPaginationInformationWithAlmostMaxPageSize() {
        PageRequest request = PageRequest.of(0, 99, Sort.unsorted());
        request = ControllerUtils.checkPaginationInformation(request);

        Assertions.assertEquals(0, request.getPageNumber());
        Assertions.assertEquals(99, request.getPageSize());
        Assertions.assertEquals(Sort.unsorted(), request.getSort());
    }

    @Test
    public void testCheckPaginationInformationWithTooLargePage1() {
        PageRequest request = PageRequest.of(0, 10, Sort.by("id"));
        request = ControllerUtils.checkPaginationInformation(request);

        Assertions.assertEquals(0, request.getPageNumber());
        Assertions.assertEquals(10, request.getPageSize());
        Assertions.assertEquals(Sort.by("id"), request.getSort());
    }

    @Test
    public void testCheckPaginationInformationWithInvalidPageable() {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        PageRequest request = ControllerUtils.checkPaginationInformation(new Pageable() {
          @Override
          public int getPageNumber() {
            return -1;
          }

          @Override
          public int getPageSize() {
            return 10;
          }

          @Override
          public long getOffset() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Sort getSort() {
            return Sort.unsorted();
          }

          @Override
          public Pageable next() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Pageable previousOrFirst() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Pageable first() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public boolean hasPrevious() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public boolean isPaged() {
            return Pageable.super.isPaged(); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public boolean isUnpaged() {
            return Pageable.super.isUnpaged(); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Sort getSortOr(Sort sort) {
            return Pageable.super.getSortOr(sort); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Pageable withPage(int pageNumber) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public Optional<Pageable> toOptional() {
            return Pageable.super.toOptional(); //To change body of generated methods, choose Tools | Templates.
          }
        });

        Assertions.fail("Test should have already failed but created page request " + request);
      });
    }

    @Test
    public void testCheckAnonymousTrue() {
      Assertions.assertThrows(UnauthorizedAccessException.class, () -> {
        Mockito.when(securityContext.getAuthentication()).thenReturn((JwtAuthenticationToken) null);
        SecurityContextHolder.setContext(securityContext);

        ControllerUtils.checkAnonymousAccess();
      });
    }

    @Test
    public void testCheckAnonymousFalse() {
        JwtAuthenticationToken userToken = edu.kit.datamanager.util.JwtBuilder.
                createUserToken("tester", RepoUserRole.USER).
                addSimpleClaim("firstname", "test").
                addSimpleClaim("lastname", "user").
                addSimpleClaim("email", "test@mail.org").
                addSimpleClaim("groupid", "USERS").
                getJwtAuthenticationToken(key);
        Mockito.when(securityContext.getAuthentication()).thenReturn(userToken);
        SecurityContextHolder.setContext(securityContext);
        ControllerUtils.checkAnonymousAccess();
    }

    @Test
    public void testCheckAdministratorAccessTrue() {
        JwtAuthenticationToken userToken = edu.kit.datamanager.util.JwtBuilder.
                createUserToken("tester", RepoUserRole.ADMINISTRATOR).
                addSimpleClaim("firstname", "test").
                addSimpleClaim("lastname", "user").
                addSimpleClaim("email", "test@mail.org").
                addSimpleClaim("groupid", "USERS").
                getJwtAuthenticationToken(key);
        Mockito.when(securityContext.getAuthentication()).thenReturn(userToken);
        SecurityContextHolder.setContext(securityContext);

        ControllerUtils.checkAdministratorAccess();
    }

    @Test
    public void testCheckAdministratorAccessFalse() {
      Assertions.assertThrows(AccessForbiddenException.class, () -> {
        JwtAuthenticationToken userToken = edu.kit.datamanager.util.JwtBuilder.
                createUserToken("tester", RepoUserRole.USER).
                addSimpleClaim("firstname", "test").
                addSimpleClaim("lastname", "user").
                addSimpleClaim("email", "test@mail.org").
                addSimpleClaim("groupid", "USERS").
                getJwtAuthenticationToken(key);
        Mockito.when(securityContext.getAuthentication()).thenReturn(userToken);
        SecurityContextHolder.setContext(securityContext);
        ControllerUtils.checkAdministratorAccess();
      });
    }

    @Test
    public void testCheckETagNotModified() {
        ControllerUtils.checkEtag(createDummyWebRequest(), () -> "1234");
    }

    @Test
    public void testCheckETagModified() {
      Assertions.assertThrows(EtagMismatchException.class, () -> {
        ControllerUtils.checkEtag(createDummyWebRequest(), () -> "12343");
      });
    }

    @Test
    public void testCheckETagMissing() {
      Assertions.assertThrows(EtagMissingException.class, () -> {
        ControllerUtils.checkEtag(createDummyWebRequest(false), () -> "12343");
      });
    }

    @Test
    public void getGetLocalHostname() throws Exception {
        Assertions.assertEquals(InetAddress.getLocalHost().getHostName(), ControllerUtils.getLocalHostname());
    }

    @Test
    public void testParseIdToLong() {
        Long id = ControllerUtils.parseIdToLong("1");
        Assertions.assertEquals(Long.valueOf(1l), id);
    }

    @Test
    public void testParseIdToLongFailing() {
      Assertions.assertThrows(BadArgumentException.class, () -> {
        Long id = ControllerUtils.parseIdToLong("ab");
        Assertions.fail("Parsing should have been failed before but returned " + id);
      });
    }

    @Test
    public void testGetContentRangeHeaderExactFit() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 10);
        Assertions.assertEquals("0-9/10", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderWrongTotalElements() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 10);
        Assertions.assertNotEquals("0-9/9", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderNoResult() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 0);
        Assertions.assertEquals("*/0", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderPageTwoNoResult() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(1, 10, 0);
        Assertions.assertEquals("*/0", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderOneResult() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 1);
        Assertions.assertEquals("0-0/1", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderTwoResult() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 2);
        Assertions.assertEquals("0-1/2", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderElevenResultsPageTwo() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(1, 10, 11);
        Assertions.assertEquals("10-10/11", contentRangeHeader);
    }

    @Test
    public void testGetContentRangeHeaderBadRequest() {
      Assertions.assertThrows(RangeNotSatisfyableException.class, () -> {
        ControllerUtils.getContentRangeHeader(1, 10, 1);
      });
    }

    @Test
    public void testParseContentRangeHeaderWithResults() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 11);
        ControllerUtils.ContentRange range = ControllerUtils.parseContentRangeHeader(contentRangeHeader);
        Assertions.assertEquals(0, range.getIndexStart());
        Assertions.assertEquals(9, range.getIndexEnd());
    }

      @Test
    public void testParseContentRangeHeaderWithNoResults() {
        String contentRangeHeader = ControllerUtils.getContentRangeHeader(0, 10, 0);
        ControllerUtils.ContentRange range = ControllerUtils.parseContentRangeHeader(contentRangeHeader);
        Assertions.assertEquals(0, range.getIndexStart());
        Assertions.assertEquals(0, range.getIndexEnd());
    }
    
    private WebRequest createDummyWebRequest() {
        return createDummyWebRequest(true);
    }

    private WebRequest createDummyWebRequest(final boolean hasEtag) {
        return new WebRequest() {
            @Override
            public String getHeader(String headerName) {
                if (hasEtag && "If-Match".equals(headerName)) {
                    return "\"1234\"";
                }
                return null;
            }

            @Override
            public String[] getHeaderValues(String headerName) {
                return null;
            }

            @Override
            public Iterator<String> getHeaderNames() {
                return null;
            }

            @Override
            public String getParameter(String paramName) {
                return null;
            }

            @Override
            public String[] getParameterValues(String paramName) {
                return null;
            }

            @Override
            public Iterator<String> getParameterNames() {
                return null;
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public String getContextPath() {
                return null;
            }

            @Override
            public String getRemoteUser() {
                return null;
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public boolean checkNotModified(long lastModifiedTimestamp) {
                return true;
            }

            @Override
            public boolean checkNotModified(String etag) {
                return hasEtag && "1234".equals(etag);
            }

            @Override
            public boolean checkNotModified(String etag, long lastModifiedTimestamp) {
                return hasEtag && "1234".equals(etag);
            }

            @Override
            public String getDescription(boolean includeClientInfo) {
                return null;
            }

            @Override
            public Object getAttribute(String name, int scope) {
                return null;
            }

            @Override
            public void setAttribute(String name, Object value, int scope) {
            }

            @Override
            public void removeAttribute(String name, int scope) {
            }

            @Override
            public String[] getAttributeNames(int scope) {
                return null;
            }

            @Override
            public void registerDestructionCallback(String name, Runnable callback, int scope) {
            }

            @Override
            public Object resolveReference(String key) {
                return null;
            }

            @Override
            public String getSessionId() {
                return null;
            }

            @Override
            public Object getSessionMutex() {
                return null;
            }
        };
    }
}
