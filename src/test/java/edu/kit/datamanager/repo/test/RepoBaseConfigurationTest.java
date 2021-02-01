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
package edu.kit.datamanager.repo.test;

import edu.kit.datamanager.configuration.RepoBaseConfiguration;
import java.net.URI;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class RepoBaseConfigurationTest{

  @Test
  public void testRepoBaseConfiguration() throws Exception{
    RepoBaseConfiguration props = new RepoBaseConfiguration();
    props.setBasepath(URI.create("file:///tmp/").toURL());
    Assert.assertEquals("file:/tmp/", props.getBasepath().toString());
  }
//
//  @Test
//  public void testEqualsAndHashCode() throws Exception{
//    RepoBaseConfiguration props1 = new RepoBaseConfiguration();
//    props1.setBasepath(URI.create("file:///tmp/").toURL());
//    RepoBaseConfiguration props2 = new RepoBaseConfiguration();
//    props2.setBasepath(URI.create("file:///tmp/").toURL());
//    Assert.assertTrue(props1.equals(props2));
//
//    props1.setBasepath(URI.create("file:///otherFolder/").toURL());
//    Assert.assertFalse(props1.equals(props2));
//  }
}
