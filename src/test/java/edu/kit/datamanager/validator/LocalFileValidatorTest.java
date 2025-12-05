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

import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author hartmann-v
 */
public class LocalFileValidatorTest {

  private static URL BASH_EXECUTABLE;

  private static File TMP_FILE;

  private static File TMP_DIR;

  public LocalFileValidatorTest() {
  }

  @BeforeAll
  public static void setUpClass() throws IOException {
    TMP_FILE = Files.createTempFile("localFileValidator", "test").toFile();
    BASH_EXECUTABLE = TMP_FILE.toURI().toURL();
  }

  @AfterAll
  public static void tearDownClass() {
    FileUtils.deleteQuietly(TMP_FILE);
  }

  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of isValid method, of class LocalFileValidator.
   */
  @Test
  public void testIsValid() throws MalformedURLException {
    System.out.println("isValid");
    URL value = BASH_EXECUTABLE;
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = true;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsInvalidFile() throws MalformedURLException {
    System.out.println("testIsInvalidFile");
    URL value = new URL("file:invalid/file");
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsNull() throws MalformedURLException {
    System.out.println("testIsNull");
    URL value = null;
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsPath() throws MalformedURLException {
    System.out.println("testIsPath");
    URL value = FileUtils.getTempDirectory().toURI().toURL();
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsNotURL() throws MalformedURLException {
    System.out.println("testIsNotURL");
    URL value = new URL("file: src/test/resources/examples/gemma/simple.json");
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

}
