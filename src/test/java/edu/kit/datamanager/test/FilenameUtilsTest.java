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

import edu.kit.datamanager.util.FilenameUtils;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jejkal
 */
public class FilenameUtilsTest{

  @Test
  public void testFullSetOfSpecialCharacters(){
    String filename = "%\\*/:<>?\\\\|+,;=[]";
    Assertions.assertEquals("%25%5C%2A%2F%3A%3C%3E%3F%5C%5C%7C%2B%2C%3B%3D%5B%5D", FilenameUtils.escapeStringAsFilename(filename));
  }

  @Test
  public void testMaxLength(){
    String randomString = RandomString.make(512);
    Assertions.assertEquals(255, FilenameUtils.escapeStringAsFilename(randomString).length());
  }

  @Test
  public void testFinalReplacement(){
    String t = ".(?=.*.)";
    Assertions.assertEquals("%2E(%3F%3D%2E%2A.)", FilenameUtils.escapeStringAsFilename(t));
  }

  @Test
  public void testNoReplacement(){
    String t = "myFile.txt";
    Assertions.assertEquals("myFile.txt", FilenameUtils.escapeStringAsFilename(t));
  }
}
