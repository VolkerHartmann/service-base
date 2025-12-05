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

import edu.kit.datamanager.SpringTestConfig;
import edu.kit.datamanager.entities.repo.ContentInformation;
import edu.kit.datamanager.entities.repo.DataResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestGatewaySupport;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 *
 * @author jejkal
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = SpringTestConfig.class)
@ContextConfiguration(classes = SpringTestConfig.class)
public class SimpleRepositoryClientTest{

  @Autowired
  private RestTemplate restTemplate;
  private MockRestServiceServer mockServer;
  private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void init(){
    RestGatewaySupport gateway = new RestGatewaySupport();
    gateway.setRestTemplate(restTemplate);
    mockServer = MockRestServiceServer.createServer(gateway);
  }

  @Test
  public void testGetResources() throws Exception{
    DataResource res = new DataResource();
    res.setId("test123");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withQueryParam("page", "0").withQueryParam("size", "20");
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/?page=0&size=20"))
            .andRespond(withSuccess(mapper.writeValueAsString(new DataResource[]{res}), MediaType.APPLICATION_JSON));

    DataResource[] result = client.getResource(DataResource[].class);

    mockServer.verify();
    assertEquals(1, result.length);
    assertEquals("test123", result[0].getId());
  }

  @Test
  public void testGetResourcesWith100Elements() throws Exception{
    DataResource res = new DataResource();
    res.setId("test123");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withQueryParam("page", "0").withQueryParam("size", "100");
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/?page=0&size=100"))
            .andRespond(withSuccess(mapper.writeValueAsString(new DataResource[]{res}), MediaType.APPLICATION_JSON));

    DataResource[] result = client.getResource(DataResource[].class);

    mockServer.verify();
    assertEquals(1, result.length);
    assertEquals("test123", result[0].getId());
  }

  @Test
  public void testGetResourcesWithPage10() throws Exception{
    DataResource res = new DataResource();
    res.setId("test123");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withQueryParam("page", "10");
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/?page=10"))
            .andRespond(withSuccess(mapper.writeValueAsString(new DataResource[]{res}), MediaType.APPLICATION_JSON));

    DataResource[] result = client.getResource(DataResource[].class);

    mockServer.verify();
    assertEquals(1, result.length);
    assertEquals("test123", result[0].getId());
  }

  @Test
  public void testGetResource() throws Exception{
    DataResource res = new DataResource();
    res.setId("test123");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withResourcePath("test123");
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123"))
            .andRespond(withSuccess(mapper.writeValueAsString(res), MediaType.APPLICATION_JSON));

    DataResource result = client.getResource(DataResource.class);

    mockServer.verify();
    assertEquals("test123", result.getId());
  }

  @Test
  public void testGetResourceAsString() throws Exception{
    DataResource res = new DataResource();
    res.setId("test123");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withResourcePath("test123");
    client = client.accept(MediaType.TEXT_PLAIN);
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123"))
            .andRespond(withSuccess(mapper.writeValueAsString(res), MediaType.APPLICATION_JSON));

    String result = client.getResource(String.class);
    mockServer.verify();
    res = mapper.readValue(result, DataResource.class);
    Assertions.assertNotNull(res);
  }
 
  @Test
  public void testGetResourceAsStringViaRedirect() throws Exception{
    String schemaRef = "http://json-schema.org/draft-04/schema#";
    SimpleServiceClient client = new SimpleServiceClient(schemaRef);
    client = client.accept(MediaType.TEXT_PLAIN);
    String content = client.getResource(String.class);
    Assertions.assertTrue(content.length() > 100);
    Assertions.assertTrue(content.contains(schemaRef), "Response should contain schema reference!");
  }

  @Test
  public void testListContentInformation() throws Exception{
    ContentInformation res = new ContentInformation();
    res.setRelativePath("myFile.txt");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withResourcePath("test123/data/");
    client = client.accept(ContentInformation.CONTENT_INFORMATION_MEDIA_TYPE);
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123/data/"))
            .andRespond(withSuccess(mapper.writeValueAsString(new ContentInformation[]{res}), ContentInformation.CONTENT_INFORMATION_MEDIA_TYPE));

    ContentInformation[] result = client.getResource(ContentInformation[].class);

    mockServer.verify();
    assertEquals(1, result.length);
    assertEquals("myFile.txt", result[0].getRelativePath());
  }

  @Test
  public void testSingleContentInformation() throws Exception{
    ContentInformation res = new ContentInformation();
    res.setRelativePath("myFile.txt");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    client = client.withResourcePath("test123/data/myFile.txt");
    client = client.accept(ContentInformation.CONTENT_INFORMATION_MEDIA_TYPE);
    client.setRestTemplate(restTemplate);

    mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123/data/myFile.txt"))
            .andRespond(withSuccess(mapper.writeValueAsString(res), MediaType.APPLICATION_JSON));

    ContentInformation result = client.getResource(ContentInformation.class);

    mockServer.verify();
    assertEquals("myFile.txt", result.getRelativePath());
  }

  @Test
  public void testUploadContent() throws Exception{
    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    Path tmp = Paths.get("testFile.txt");
    try{

      Files.write(tmp, "This is a test".getBytes());

      client = client.withResourcePath("test123/data/testFile.txt").withQueryParam("force", "false").withFormParam("file", tmp.toFile());
      client.setRestTemplate(restTemplate);

      mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123/data/testFile.txt?force=false"))
              .andRespond(MockRestResponseCreators.withCreatedEntity(URI.create("http://localhost:8080/api/v1/dataresources/test123/data/testFile.txt")));

      HttpStatus status = client.postForm();

      mockServer.verify();
      assertEquals(HttpStatus.CREATED, status);
    } finally{
      try{
        Files.delete(tmp);
      } catch(Exception e){
      }
    }
  }

  @Test
  public void testUploadWithNullFile() throws Exception{
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");

    client.withResourcePath("test123/data/testFile.txt").withFormParam("file", null);

  });
  }

  @Test
  public void testUploadContentWithMetadata() throws Exception{
    ContentInformation res = new ContentInformation();
    res.getMetadata().put("test", "OK");

    SimpleServiceClient client = new SimpleServiceClient("http://localhost:8080/api/v1/dataresources/");
    Path tmp = Paths.get("testFile.txt");
    try{

      Files.write(tmp, "This is a test".getBytes());

      client = client.withResourcePath("test123/data/testFile.txt").withFormParam("file", tmp.toFile()).withFormParam("metadata", res).withQueryParam("force", "true");
      client.setRestTemplate(restTemplate);

      mockServer.expect(once(), requestTo("http://localhost:8080/api/v1/dataresources/test123/data/testFile.txt?force=true"))
              .andRespond(MockRestResponseCreators.withCreatedEntity(URI.create("http://localhost:8080/api/v1/dataresources/test123/data/testFile.txt")));

      HttpStatus status = client.postForm();

      mockServer.verify();
      assertEquals(HttpStatus.CREATED, status);
    } finally{
      try{
        Files.delete(tmp);
      } catch(Exception e){
      }
    }
  }
}
