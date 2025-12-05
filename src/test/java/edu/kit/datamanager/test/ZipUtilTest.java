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

import edu.kit.datamanager.exceptions.InvalidAuthenticationException;
import edu.kit.datamanager.exceptions.MessageValidationException;
import edu.kit.datamanager.util.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author jejkal
 */
public class ZipUtilTest{

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtilTest.class);

  private final static String TEST_FILE_NAME = "testFile.txt";
  private final static String UNICODE_FILE_NAME = "Ūnîcødę.bin";
  private final static String EMPTY_FILE_NAME = "emptyFile";
  private final static String EMPTY_FOLDER_NAME = "emptyFolder";
  private final static String SUB_FOLDER_NAME = "subFolder";

  private final static File TMP_DIR = new File(FileUtils.getTempDirectory(), "ZipUtilsTest1/");
  private final static File TMP_DIR_WITH_BASE_PATH = new File(FileUtils.getTempDirectory(), "ZipUtilsTest2/base");
  private final static File TEST_FILE = new File(TMP_DIR, TEST_FILE_NAME);
  private final static File UNICODE_FILE = new File(TMP_DIR, UNICODE_FILE_NAME);
  private final static File EMPTY_FILE = new File(TMP_DIR, EMPTY_FILE_NAME);
  private final static File EMPTY_FOLDER = new File(TMP_DIR, EMPTY_FOLDER_NAME + "/");
  private final static File SUB_FOLDER = new File(TMP_DIR, SUB_FOLDER_NAME + "/");
  private final static File TEST_FILE_IN_SUB_FOLDER = new File(SUB_FOLDER, TEST_FILE_NAME);

  final @BeforeAll
  public static void prepareTest(){
    FileUtils.deleteQuietly(TMP_DIR);
    if(!TMP_DIR.mkdirs()){
      Assertions.fail("Failed to create test folder at " + TMP_DIR);
    }

    FileUtils.deleteQuietly(TMP_DIR_WITH_BASE_PATH);
    if(!TMP_DIR_WITH_BASE_PATH.mkdirs()){
      Assertions.fail("Failed to create test folder at " + TMP_DIR_WITH_BASE_PATH);
    }

    try{
      LOGGER.debug("Creating content for {}.", TEST_FILE);
      FileUtils.write(TEST_FILE, "Simple content in testFile.txt", "UTF-8");
      LOGGER.debug("Creating content for {}.", TEST_FILE_IN_SUB_FOLDER);
      FileUtils.write(TEST_FILE_IN_SUB_FOLDER, "Simple content in subFolder/testFile.txt", "UTF-8");
      LOGGER.debug("Creating content for {}.", UNICODE_FILE);
      FileUtils.write(UNICODE_FILE, "Simple content in Ūnîcødę.txt", "UTF-8");
      LOGGER.debug("Creating empty file at {}.", EMPTY_FILE);
      EMPTY_FILE.createNewFile();
      LOGGER.debug("Creating empty folder at {}.", EMPTY_FOLDER);
      if(!EMPTY_FOLDER.mkdirs()){
        throw new IOException("Failed to create empty folder.");
      }
      LOGGER.debug("Copying content from {} to {}.", TMP_DIR, TMP_DIR_WITH_BASE_PATH);
      FileUtils.copyDirectory(TMP_DIR, TMP_DIR_WITH_BASE_PATH);
    } catch(IOException ex){
      ex.printStackTrace();
      Assertions.fail("Unable to create testFile");
    }
  }

  @AfterAll
  public static void cleanupTest(){
    FileUtils.deleteQuietly(TMP_DIR);
    FileUtils.deleteQuietly(TMP_DIR_WITH_BASE_PATH);
  }

  @Test
  public void testZipToCurrentFolder() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipToCurrentFolder.zip");
    ZipUtils.zip(TMP_DIR, out);
    Assertions.assertTrue(out.exists());
    //unzip to current folder
    ZipUtils.unzip(out);

    checkDirectoryStructure(new File("."));

    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(new File(TEST_FILE_NAME));
    FileUtils.deleteQuietly(new File(UNICODE_FILE_NAME));
    FileUtils.deleteQuietly(new File(EMPTY_FILE_NAME));
    FileUtils.deleteQuietly(new File(EMPTY_FOLDER_NAME));
    FileUtils.deleteQuietly(new File(SUB_FOLDER_NAME));
  }

  @Test
  public void testZipToOtherFolder() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipToOtherFolder.zip");
    ZipUtils.zip(TMP_DIR, out);
    Assertions.assertTrue(out.exists());
    File destination = new File("out");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination);

    checkDirectoryStructure(destination);

    //out should exists as it is not deleted by default (related test: testDeleteAfterUnzip())
    Assertions.assertTrue(out.exists());
    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipToOtherNonExistingFolder() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipToOtherFolder.zip");
    ZipUtils.zip(TMP_DIR, out);
    Assertions.assertTrue(out.exists());
    File destination = new File("out_not_exist");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination);

    checkDirectoryStructure(destination);

    //out should exists as it is not deleted by default (related test: testDeleteAfterUnzip())
    Assertions.assertTrue(out.exists());
    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipWithBasePath() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipWithBasePath.zip");
    ZipUtils.zip(TMP_DIR_WITH_BASE_PATH, TMP_DIR_WITH_BASE_PATH.getParentFile().getAbsolutePath(), out);
    Assertions.assertTrue(out.exists());
    File destination = new File("out_with_base");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination);

    checkDirectoryStructure(new File(destination, "/base"));

    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipFilesWithBasePath() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipFilesWithBasePath.zip");
    ZipUtils.zip(new File[]{TEST_FILE,
      UNICODE_FILE,
      EMPTY_FILE,
      EMPTY_FOLDER,
      SUB_FOLDER}, TMP_DIR.getAbsolutePath(), out);
    Assertions.assertTrue(out.exists());
    File destination = new File("out_with_single_files");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination);

    checkDirectoryStructure(destination);

    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipFilesWithoutFilesArgument() throws IOException{
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      File out = new File(FileUtils.getTempDirectory(), "testZipFilesWithBasePath.zip");
      ZipUtils.zip((File[]) null, TMP_DIR.getAbsolutePath(), out);
    });
  }

  @Test
  public void testZipFilesWithoutBasePathArgument() throws IOException{
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      File out = new File(FileUtils.getTempDirectory(), "testZipFilesWithBasePath.zip");
      ZipUtils.zip(new File[]{TEST_FILE,
              UNICODE_FILE,
              EMPTY_FILE,
              EMPTY_FOLDER,
              SUB_FOLDER}, null, out);
    });
  }

  @Test
  public void testZipFilesWithoutDestinationArgument() throws IOException{
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      File out = new File(FileUtils.getTempDirectory(), "testZipFilesWithBasePath.zip");
      ZipUtils.zip(new File[]{TEST_FILE,
              UNICODE_FILE,
              EMPTY_FILE,
              EMPTY_FOLDER,
              SUB_FOLDER}, TMP_DIR.getAbsolutePath(), (File) null);
    });
  }

  @Test
  public void testDeleteAfterUnzip() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipToOtherFolder.zip");
    ZipUtils.zip(TMP_DIR, out);
    Assertions.assertTrue(out.exists());
    File destination = new File("out");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination, true);

    checkDirectoryStructure(destination);

    Assertions.assertFalse(out.exists());
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testDeleteAfterUnzipWithoutDestination() throws IOException{
    File out = new File(FileUtils.getTempDirectory(), "testZipToOtherFolder.zip");
    ZipUtils.zip(TMP_DIR, out);
    Assertions.assertTrue(out.exists());
    //unzip to current folder

    ZipUtils.unzip(out, true);

    checkDirectoryStructure(out.getParentFile());

    Assertions.assertFalse(out.exists());
    FileUtils.deleteQuietly(new File(out.getParentFile(), TEST_FILE_NAME));
    FileUtils.deleteQuietly(new File(out.getParentFile(), UNICODE_FILE_NAME));
    FileUtils.deleteQuietly(new File(out.getParentFile(), EMPTY_FILE_NAME));
    FileUtils.deleteQuietly(new File(out.getParentFile(), EMPTY_FOLDER_NAME));
    FileUtils.deleteQuietly(new File(out.getParentFile(), SUB_FOLDER_NAME));
  }

  @Test
  public void testZipDirectoryWithTxtExtension(){
    File out = new File(FileUtils.getTempDirectory(), "testZipDirectory.zip");
    //only zip text files (test dataset should contain one of them)
    ZipUtils.zipDirectory(TMP_DIR, out, "txt");
    Assertions.assertTrue(out.exists());
    File destination = new File("out");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination, true);

    //check text file
    Assertions.assertTrue(new File(destination, TEST_FILE_NAME).exists());
    //check if non-text files does not exist
    Assertions.assertFalse(new File(destination, UNICODE_FILE_NAME).exists());
    Assertions.assertFalse(new File(destination, EMPTY_FILE_NAME).exists());

    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipDirectoryWithAllExtension(){
    File out = new File(FileUtils.getTempDirectory(), "testZipDirectory.zip");
    //only zip text files (test dataset should contain one of them)
    ZipUtils.zipDirectory(TMP_DIR, out, (String[])null);
    Assertions.assertTrue(out.exists());
    File destination = new File("out");
    destination.mkdirs();
    //unzip to current folder

    ZipUtils.unzip(out, destination, true);

    //check text file
    checkDirectoryStructure(destination);

    FileUtils.deleteQuietly(out);
    FileUtils.deleteQuietly(destination);
  }

  @Test
  public void testZipFile() throws IOException{
    File out = FileUtils.getTempDirectory();
    //only zip text files (test dataset should contain two of them)
    ZipUtils.zipSingleFile(TEST_FILE, out);
    File outFile = new File(out, TEST_FILE_NAME + ".zip");
    Assertions.assertTrue(outFile.exists());
    Assertions.assertTrue(outFile.length() > 1);

    FileUtils.deleteQuietly(outFile);
  }

  @Test
  @Disabled("Not working if run as privileged user")
  public void testZipWithIOException() throws IOException {
    Assertions.assertThrows(IOException.class, () -> {
      File outDir = new File(FileUtils.getTempDirectory(), "myFolder");
      if (!outDir.exists()) {
        outDir.mkdirs();
      }
      outDir.setWritable(true);
      File out = new File(outDir, "testZipToCurrentFolder.zip");

      out.setWritable(false);
      outDir.setExecutable(false);
      try {
        ZipUtils.zip(TMP_DIR, out);
      } finally {
        out.setWritable(true);
        outDir.setExecutable(true);
        FileUtils.deleteQuietly(outDir);
      }
    });
  }

  @Test
  public void testZipWithRuntimeException() throws IOException {
    Assertions.assertThrows(IOException.class, () -> {
      ZipUtils.zip(new File[]{TEST_FILE,
              UNICODE_FILE,
              EMPTY_FILE,
              EMPTY_FOLDER,
              SUB_FOLDER}, TMP_DIR.getAbsolutePath(), (ZipOutputStream) null);
    });
  }

  private static void checkDirectoryStructure(File location){
    Assertions.assertTrue(new File(location, TEST_FILE_NAME).exists());
    Assertions.assertTrue(new File(location, UNICODE_FILE_NAME).exists());
    Assertions.assertTrue(new File(location, EMPTY_FILE_NAME).exists());
    Assertions.assertTrue(new File(location, EMPTY_FOLDER_NAME).exists());
    Assertions.assertTrue(new File(location, SUB_FOLDER_NAME).exists());
    Assertions.assertTrue(new File(location, SUB_FOLDER_NAME + "/" + TEST_FILE_NAME).exists());
  }

}
