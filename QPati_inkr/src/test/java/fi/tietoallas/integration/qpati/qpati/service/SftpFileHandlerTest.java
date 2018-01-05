/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.tietoallas.integration.qpati.qpati.service;

/*-
 * #%L
 * qpati
 * %%
 * Copyright (C) 2017 - 2018 Helsingin ja Uudenmaan sairaanhoitopiiri, Helsinki, Finland
 * %%
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
 * #L%
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;

import org.junit.*;

import static org.junit.Assert.assertFalse;
import org.junit.rules.ExpectedException;
import org.junit.runner.*;
import org.junit.runners.JUnit4;

import org.springframework.beans.factory.annotation.*;
import org.springframework.core.env.Environment;

import fi.tietoallas.integration.qpati.qpati.repository.MetadataRepository;

/**
 *
 * @author huangyee
 */
@RunWith(JUnit4.class)
public class SftpFileHandlerTest {
	@Autowired
	private Environment environment;

	@Autowired
	private MetadataRepository metadataRepository;

	// Directories for testing.
	Path root;
	Path dynamicDir;

	// Defines variables.
	SftpFileHandler sftpFileHandler;

	// Rule to indicate both exception and exception message we are expecting.
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * Initialize variables, directories and files before running each test case.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Initialise the root path.
		root = Paths.get("src/test/resources/package_service").toAbsolutePath();

		// Dynamically create directories and files for testing deleteFiles().
		Files.createDirectories(root.resolve("2017/11/02/10000/20000"));
		Files.createDirectories(root.resolve("2017/11/03/10000/20000"));

		// Assign the dynamic directory to the dynamicDir variable.
		dynamicDir = Paths.get(root + "/2017");

		// Initialise variables.
		sftpFileHandler = new SftpFileHandler(metadataRepository, environment);
	}

	/**
	 * Delete the dynamic root directory after each test.
	 * 
	 * @throws Exception
	 */
	@After
	public void removeTempDirectories() throws Exception {
		FileUtils.deleteDirectory(dynamicDir.toFile());
	}

	/**
	 * Test Case - 01
	 * 
	 * Method : sftpFileHanlder.deleteFromRoot(String dir) Input : dynamic directory
	 * Expectation : No file exists in the dynamic directory after recursively
	 * deleting.
	 * 
	 * @throws Exception
	 */
	@Test
	public void deleteFiles_DynamicDir_FilesDeletedTrue() throws Exception {
		System.out.println("Test 01 => deleteFromRoot(root) => No file exists in the dynamic directory.");

		sftpFileHandler.deleteFromRoot(dynamicDir.toString());

		assertFalse("Test 01 => No file exists in the dynamic directory => ", Files.exists(dynamicDir));
	}
}
