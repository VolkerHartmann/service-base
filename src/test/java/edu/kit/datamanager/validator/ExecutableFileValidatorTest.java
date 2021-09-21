/*
 * Copyright 2020 hartmann-v.
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
package edu.kit.datamanager.validator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hartmann-v
 */
public class ExecutableFileValidatorTest {
  
  private static URL EXECUTABLE_FILE;
  
  private static URL NOT_EXECUTABLE_FILE;
  
  private static File TMP_FILE;

  private static File TMP_FILE2;

  public ExecutableFileValidatorTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {
    TMP_FILE = Files.createTempFile("executableFileValidator", "test").toFile();
    TMP_FILE.setExecutable(true);
    EXECUTABLE_FILE = TMP_FILE.toURI().toURL();
    TMP_FILE2 = Files.createTempFile("executableFileValidator", "test").toFile();
    TMP_FILE2.setExecutable(false);
    NOT_EXECUTABLE_FILE = TMP_FILE2.toURI().toURL();
  }
  
  @AfterClass
  public static void tearDownClass() {
    FileUtils.deleteQuietly(TMP_FILE);
    FileUtils.deleteQuietly(TMP_FILE2);
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

  /**
   * Test of isValid method, of class ExecutableFileValidator.
   */
  @Test
  public void testIsValid() throws MalformedURLException {
    System.out.println("isValid");
    URL value = EXECUTABLE_FILE;
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = true;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsInvalidFile() throws MalformedURLException {
    System.out.println("testIsInvalidFile");
    URL value = new URL("file:invalid/file");
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNull() throws MalformedURLException {
    System.out.println("testIsNull");
    URL value = null;
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsPath() throws MalformedURLException {
    System.out.println("testIsPath");
    URL value = FileUtils.getTempDirectory().toURI().toURL();
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNotExecutable() throws MalformedURLException {
    System.out.println("testIsNotExecutable");
    URL value = NOT_EXECUTABLE_FILE;
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNotURL() throws MalformedURLException {
    System.out.println("testIsNotURL");
    URL value = new URL("file: src/test/resources/examples/gemma/simple.json");
    ConstraintValidatorContext context = null;
    ExecutableFileValidator instance = new ExecutableFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  
}
