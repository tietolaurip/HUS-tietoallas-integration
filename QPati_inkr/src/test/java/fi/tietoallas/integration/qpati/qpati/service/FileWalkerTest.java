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
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import fi.tietoallas.integration.qpati.qpati.repository.MetadataRepository;

@RunWith(JUnit4.class)
public class FileWalkerTest {
	@Autowired
	private Environment environment;

	@Autowired
	private MetadataRepository metadataRepository;

	// Test resources root directory.
	Path root;
	Path tmp_dynamic_one;
	Path tmp_dynamic_two;
	Path previous_refs_dynamic;

	// Directories for testing getLines() and normalizeFilename().
	String data_static;
	String data_new_static;
	String check_permission_static;

	// CSV Test Files.
	File csvOne;
	File csvTwo;
	File csvRefOne;
	File csvRefTwo;
	File csvRefNewOne;
	File csvRefNewTwo;

	// Defines variables.
	FileWalker fileWalker;
	Helpers helpers;
	List<String[]> data;
	
	// Helper methods.
	private class Helpers {
		/**
         * Convert a collection of paths to a collection of filenames.
         * 
         * @param  List<Path>   paths - a collection of paths.
         * @return List<String> names - a collection of names.
         * @throws Exception
         */
		public List<String> listNames(List<Path> paths) throws Exception {
			List<String> names = new ArrayList<>();
			for (Path path : paths) {
				System.out.println("File => " + path.toFile().getName());
				names.add(path.toFile().getName());
			}
			return names;
		}
		
		/**
         * Flatten a two-dimensional list to a string list.
         * 
         * @param  List<String[]> origList - a two-dimensional list.
         * @return List<String>   flatList - a string list.
         * @throws Exception
         */
		public List<String> flattenList(List<String[]> origList) throws Exception {
			List<String> flatList = new ArrayList<>();
			origList.forEach(value -> {
				System.out.println("Value => " + String.join(", ", value));
				flatList.add(String.join(", ", value));
			});
			return flatList;
		}
	}

	/**
	 * Rule to indicate both exception and exception message we are expecting.
	 */
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * Initialize variables, directories and files before running each test case.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Initialize Path parameters.
		root = Paths.get("src/test/resources/package_service").toAbsolutePath();
		tmp_dynamic_one = Files.createDirectories(root.resolve("tmp_dynamic_one"));
		tmp_dynamic_two = Files.createDirectories(root.resolve("tmp_dynamic_two"));
		previous_refs_dynamic = Files.createDirectories(root.resolve("previous_refs_dynamic"));

		// Initialize String parameters.
		data_static = root + "/data/";
		data_new_static = root + "/data_new/";
		check_permission_static = root + "/check_permission/";
		
		// Copy static folders to dynamic folders.
		FileUtils.copyDirectory(Paths.get(data_static).toFile(), tmp_dynamic_one.toFile());
		FileUtils.copyDirectory(Paths.get(data_new_static).toFile(), tmp_dynamic_two.toFile());

		// Get CSV test files.
		csvOne = Paths.get(data_static + "TEST_one.csv").toAbsolutePath().toFile();
		csvTwo = Paths.get(data_static + "TEST_two.csv").toAbsolutePath().toFile();
		csvRefOne = Paths.get(data_static + "Ref_TEST_one.csv").toAbsolutePath().toFile();
		csvRefTwo = Paths.get(data_static + "Ref_TEST_two.csv").toAbsolutePath().toFile();
		csvRefNewOne = Paths.get(data_new_static + "Ref_TEST_one.csv").toAbsolutePath().toFile();
		csvRefNewTwo = Paths.get(data_new_static + "Ref_TEST_two.csv").toAbsolutePath().toFile();

		// Initialize variables.
		fileWalker = new FileWalker(metadataRepository, environment);
		helpers = new Helpers();
		data = new ArrayList<>();
	}

	/**
	 * Delete the dynamic directory after each test.
	 * 
	 * @throws Exception
	 */
	@After
	public void removeTempDirectories() throws Exception {
		FileUtils.deleteDirectory(previous_refs_dynamic.toFile());
		FileUtils.deleteDirectory(tmp_dynamic_one.toFile());
		FileUtils.deleteDirectory(tmp_dynamic_two.toFile());
	}

	/**
	 * Test Case - 01
	 * 
	 * Method      : FileWalker.listFoldersWithReadyFlag(String root) 
	 * Input       : root path => src/test/resources/package_service/ 
	 * Expectation : Each value of the List should be equal to the name of the corresponding folder.
	 * 
	 * @throws Exception
	 */
	@Test
	public void listFoldersWithReadyFlag_RootFolder_ListValueIsEqualToFolderNameTrue() throws Exception {
		System.out.println("Test 01 => listFoldersWithReadyFlag(root) => Check List values.");

		List<Path> folders = fileWalker.listFoldersWithReadyFlag(root.toString());
		List<String> folderNames = helpers.listNames(folders);
		
		assertThat("Test 01 => List should contain 2 values (data, data_new)", folderNames,
				containsInAnyOrder("data", "data_new", "tmp", "tmp_dynamic_one", "tmp_dynamic_two"));
	}

	/**
	 * Test Case - 02
	 * 
	 * Method      : FileWalker.listCSVFilesOfFolderWithReadyFlag(Path data_static) 
	 * Input       : data_static path => src/test/resources/package_service/data/ 
	 * Expectation : Each value of the List should be equal to the name of the corresponding CSV filename.
	 * 
	 * @throws Exception
	 */
	@Test
	public void listCSVFilesOfFolderWithReadyFlag_DataFolder_ListSizeIs4True() throws Exception {
		System.out.println("Test 02 => listCSVFilesOfFolderWithReadyFlag(data_static) => Check List values.");

		List<Path> csvFiles = fileWalker.listCSVFilesOfFolderWithReadyFlag(Paths.get(data_static));
		List<String> fileNames = helpers.listNames(csvFiles);

		assertThat("Test 02 => List should contain 4 values (Ref_TEST_one.csv, Ref_TEST_two.csv, TEST_one.csv, TEST_two.csv)",
				fileNames, containsInAnyOrder("Ref_TEST_one.csv", "Ref_TEST_two.csv", "TEST_one.csv", "TEST_two.csv"));
	}
	
	/**
	 * Test Case - 03
	 * 
	 * Method      : FileWalker.processFile(File file, String prevRefsPath) 
	 * Input       : src/test/resources/package_service/tmp/TEST_one.csv 
	 * Expectation : Each value of the List should be equal to the corresponding CSV row.
	 * 
	 * @throws Exception
	 */
	@Test
	public void processFile_NormalCSVFile_ListValueIsEqualToCSVRowTrue() throws Exception {
		System.out.println("Test 03 => processFile(csvOne, previous_refs_dynamic) => Check List values.");
		
		File destFile = new File(tmp_dynamic_one + "/TEST_one.csv");
		FileUtils.copyFile(csvOne, destFile);
		data = fileWalker.processFile(destFile, previous_refs_dynamic.toString());
		List<String> actual = helpers.flattenList(data);
		
		assertThat("Test 03 = > Each value of the List should be equal to the corresponding CSV row => ", 
				actual, containsInAnyOrder(
						"SampleNumber, RecordNumber, OrderNumber, AnswerNumber, VersionNumber, RowNumber, Caption, Value",
						"YP2017000008, 5039, 1, 1, 6, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 6, 2, Avausluvanantaja(t), ",
						"YP2017000008, 5039, 1, 1, 7, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 7, 2, Avausluvanantaja(t), ",
						"YP2017000008, 5039, 1, 1, 8, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 8, 2, Avausluvanantaja(t), ",
						"VALUE1, , VALUE3, VALUE4, VALUE5, VALUE6, VALUE7, VALUE8",
						"VALUE1, VALUE2, VALUE3, VALUE4, , VALUE6, VALUE7, VALUE8",
						"VALUE1, VALUE2, VALUE3, null, null, null, null, null"));
	}
	
	/**
	 * Test Case - 04
	 * 
	 * Method      : FileWalker.processFile(File file, String prevRefsPath) 
	 * Input       : csvRefOne => src/test/resources/package_service/data/Ref_TEST_one.csv 
	 * Expectation : Each value of the List should be equal to the corresponding CSV row.
	 * 
	 * @throws Exception
	 */
	@Test
	public void processFile_RefCSVFile_ListValueIsEqualToCSVRowTrue() throws Exception {
		System.out.println("Test 04 => processFile(csvRefOne, previous_refs_dynamic) => Check List values.");
		
		File destFile = new File(tmp_dynamic_one + "/Ref_TEST_one.csv");
		FileUtils.copyFile(csvRefOne, destFile);
		data = fileWalker.processFile(destFile, previous_refs_dynamic.toString());
		List<String> actual = helpers.flattenList(data);
		
		assertThat("Test 04 = > Each value of the List should be equal to the corresponding CSV row => ", 
				actual, containsInAnyOrder(
						"TIETUENRO, NIMI, LYHENNE, KÄYTÖSSÄ, TALLETTAJA, TALLETETTU",
						"1, YML Patologia, YML/PAT, KYLLÄ, KAATRASALO,MAURI, 27.04.2015 @ 23:40:15"));
	}
	
	/**
	 * Test Case - 05
	 * 
	 * Method      : FileWalker.processFile(File file, String prevRefsPath) 
	 * Input       : csvRefNewOne                      => src/test/resources/package_service/data_new/Ref_TEST_one.csv
	 *             : csvRefOne (previous_refs_dynamic) => src/test/resources/package_service/previous_refs/Ref_TEST_one.csv 
	 * Expectation : Each value of the List should be equal to the corresponding different row between old and new Ref CSV files.
	 * 
	 * @throws Exception
	 */
	@Test
	public void processFile_TwoRefCSVFiles_ListValueIsEqualToDiffCSVRowTrue() throws Exception {
		System.out.println("Test 05 => processFile(csvRefNewOne, previous_refs_dynamic) => Check differences between old and new Ref CSV files.");
		
		File destOldRefFile = new File(previous_refs_dynamic + "/Ref_TEST_one.csv");
		File destNewRefFile = new File(tmp_dynamic_one + "/Ref_TEST_one.csv");
		FileUtils.copyFile(csvRefOne, destOldRefFile);
		FileUtils.copyFile(csvRefNewOne, destNewRefFile);
		
		data = fileWalker.processFile(destNewRefFile, previous_refs_dynamic.toString() + "/");
		List<String> actual = helpers.flattenList(data);
		
		assertThat("Test 05 = > Each value of the List should be equal to the corresponding difference between old and new Ref CSV files => ", 
				actual, containsInAnyOrder(
						"TIETUENRO, NIMI, LYHENNE, KÄYTÖSSÄ, TALLETTAJA, TALLETETTU",
						"2, YML Patologia, YML/PAT, KYLLÄ, KAATRASALO,MAURI, 27.04.2015 @ 23:40:15"));
	}

	/**
	 * Test Case - 06
	 * 
	 * Method      : FileWalker.getDiffLines(oldStream, newStream) 
	 * Input       : old stream => src/test/resources/package_service/data/Ref_Test_Two.csv 
	 *             : new stream => src/test/resources/package_service/data_new/Ref_Test_Two.csv 
	 * Expectation : The size of returned List should be 4.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getDiffLines_TwoRefCSVFiles_ListSizeIs4True() throws Exception {
		System.out.println("Test 06 => getDiffLines(oldInputStream, newInputStream) => Check List size (4).");

		FileInputStream newStream = new FileInputStream(csvRefNewTwo);
		FileInputStream oldStream = new FileInputStream(csvRefTwo);

		List<String[]> diff = fileWalker.getDiffLines(oldStream, newStream);
		helpers.flattenList(diff);

		assertThat("Test 06 => List size should be 4 (header + 3 different rows) => ", diff, hasSize(4));
	}

	/**
	 * Test Case - 07
	 * 
	 * Method      : FileWalker.getAllLines(File file) 
	 * Input       : csvOne => src/test/resources/package_service/data/TEST_one.csv 
	 * Expectation : The size of returned List should be 10.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllLines_NormalCSVFile_ListSizeIs10True() throws Exception {
		System.out.println("Test 07 => getAllLines(csvOne) => Check List size (10).");

		data = fileWalker.getAllLines(csvOne);

		assertThat("Test 07 => List size should be 10 (rows) => ", data, hasSize(10));
	}

	/**
	 * Test Case - 08
	 * 
	 * Method      : FileWalker.getAllLines(File file) 
	 * Input       : csvOne => src/test/resources/package_service/data/TEST_one.csv 
	 * Expectation : The length of each value (Array) of the returned List should be 8.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllLines_NormalCSVFile_EveryListValueLengthIs8True() throws Exception {
		System.out.println("Test 08 => getAllLines(csvOne) => Check the length of each List value (8).");

		data = fileWalker.getAllLines(csvOne);

		data.stream().map((value) -> {
			System.out.println(Arrays.toString(value) + " => columns size: " + value.length);
			return value;
		}).forEachOrdered((value) -> {
			assertThat("Test 08 => The length of every List value should be 8 (columns) =>", value.length, is(8));
		});
	}

	/**
	 * Test Case - 09
	 * 
	 * Method      : FileWalker.getAllLines(File file) 
	 * Input       : csvOne => src/test/resources/package_service/data/TEST_one.csv 
	 * Expectation : Every List value should be equal to the corresponding CSV row.
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAllLines_NormalCSVFile_ListValueIsEqualToCSVRowTrue() throws Exception {
		System.out.println("Test 09 => getAllLines(csvOne) => Check every element in the List.");

		data = fileWalker.getAllLines(csvOne);
		List<String> actual = helpers.flattenList(data);

		assertThat("Test 09 => Every List value should be equal to corresponding CSV row => ",
				actual, containsInAnyOrder(
						"SampleNumber, RecordNumber, OrderNumber, AnswerNumber, VersionNumber, RowNumber, Caption, Value",
						"YP2017000008, 5039, 1, 1, 6, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 6, 2, Avausluvanantaja(t), ",
						"YP2017000008, 5039, 1, 1, 7, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 7, 2, Avausluvanantaja(t), ",
						"YP2017000008, 5039, 1, 1, 8, 1, otsikko, jotain tietoa",
						"YP2017000008, 5039, 1, 1, 8, 2, Avausluvanantaja(t), ",
						"VALUE1, , VALUE3, VALUE4, VALUE5, VALUE6, VALUE7, VALUE8",
						"VALUE1, VALUE2, VALUE3, VALUE4, , VALUE6, VALUE7, VALUE8",
						"VALUE1, VALUE2, VALUE3, null, null, null, null, null"));
	}
	
	/**
	 * Test Case - 10
	 * 
	 * Method      : FileWalker.trimList(List<String> origList, Character delimiter) 
	 * Input       : A test List with 7 values and the length of each value is different.
	 * Expectation : Returned List size should be 7 and the length of each value should be same as the length of first value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void trimList_List_ListSizeIs4True() throws Exception {
		System.out.println("Test 10 => trimList(testList) => Check List size and values.");
		
		List<String> testList = Arrays.asList(
				"a1,a2,a3,a4,a5,",
				"b1,b2,b3,b4,b5",
				"d1,d2,,d4,",
				"e1,",
				"f1,f2,f3,f4,f5,f6,f7,f8",
				",g2,g3,g4,g5",
				",h2,h3,h4,");
		data = fileWalker.trimList(testList, '\u002C');
		List<String> actual = helpers.flattenList(data);
		
		assertThat("Test 10 => List size should be 7 => ", data, hasSize(7));
		for (String[] value : data) {
			assertThat("Test 10 => The length of each value should be 5 => ", value.length, is(5));
		}
		assertThat("Test 10 => List value should be equal to corresponding item => ",
				actual, containsInAnyOrder(
						"a1, a2, a3, a4, a5",
						"b1, b2, b3, b4, b5",
						"d1, d2, , d4, ",
						"e1, , null, null, null",
						"f1, f2, f3, f4, f5",
						", g2, g3, g4, g5",
						", h2, h3, h4, "));
	}

	/**
	 * Test Case - 11
	 * 
	 * Method      : FileWalker.normalizeFileName(File file) 
	 * Input       : csvOne    => src/test/resources/package_service/data/TEST_one.csv 
	 *             : csvRefOne => src/test/resources/package_service/data/Ref_TEST_one.csv 
	 * Expectation : For the normal CSV, the returned string should be same as the CSV name (lowercase). 
	 *             : For the reference CSV, the returned string should be same as the CSV name (lowercase) without "Ref_".
	 * 
	 * @throws Exception
	 */
	@Test
	public void normalizeFileName_TwoNormalCSVFiles_ValueIsEqualToCSVNameTrue() throws Exception {
		System.out.println("Test 11 => normalizeFileName(csvOne) => Returned string is same as the CSV name.");

		String csvFilename = FileWalker.normalizeFileName(csvOne);
		String csvRefFilename = FileWalker.normalizeFileName(csvRefOne);

		assertThat("Test 11 => Normal filename should be test_one => ", csvFilename, is("test_one"));
		assertThat("Test 11 => Reference filename should be test_one => ", csvRefFilename, is("test_one"));
	}

	/**
	 * Test Case - 12
	 * 
	 * Method      : FileWalker.cleanCSVFile(Path csvFile, String prevRefFolderPath) 
	 * Input       : tmp_dynamic_one => TEST_one.csv & TEST_two.csv & Ref_TEST_one.csv & Ref_TEST_two.csv 
	 *             : previous_refs_dynamic
	 * Expectation : All CSV files shouldn't exist in the current directory.
	 *             : Ref_TEST_one.csv and Ref_TEST_two.csv should exist in the previous_refs directory.
	 * 
	 * @throws Exception
	 */
	@Test
	public void cleanCSVFile_TwoNormalAndTwoRefCSVFiles_CheckFilesExistTrue() throws Exception {
		System.out.println("Test 12 => cleanCSVFile(csvFile, prevRefFolderPath) => Check whether files exist in the specific folder.");

		List<Path> files = fileWalker.listCSVFilesOfFolderWithReadyFlag(tmp_dynamic_one);
		for (Path file : files) {
			fileWalker.cleanCSVFile(file, previous_refs_dynamic.toString());
			assertFalse("Test 12 => " + file.toFile().getName() + " shouldn't exist in the current dir => ", file.toFile().exists());
		}
		assertTrue("Test 12 => Ref_TEST_one.csv should exist in previous_refs_dynamic dir => ", 
				Paths.get(previous_refs_dynamic + "/Ref_TEST_one.csv").toFile().exists());
		assertTrue("Test 12 => Ref_TEST_two.csv should exist in previous_refs_dynamic dir => ", 
				Paths.get(previous_refs_dynamic + "/Ref_TEST_two.csv").toFile().exists());
		
	}

	/**
	 * Test Case - 13
	 * 
	 * Method      : FileWalker.cleanCSVFile(Path csvFile, String prevRefFolderPath) 
	 * Input       : tmp_dynamic_one (old reference files) => Ref_TEST_one.csv & Ref_TEST_two.csv 
	 *             : tmp_dynamic_two (new reference files) => Ref_TEST_one.csv & Ref_TEST_two.csv
	 *             : previous_refs_dynamic
	 * Expectation : Old Ref_TEST_one.csv and Ref_TEST_two.csv should be updated.
	 * 
	 * @throws Exception
	 */
	@Test
	public void cleanCSVFile_FourRefCSVFiles_RefFilesShouldBeUpdatedTrue() throws Exception {
		System.out.println("Test 13 => cleanCSVFile(csvFile, prevRefFolderPath) => Old reference files should be updated.");

		// Old reference files
		fileWalker.cleanCSVFile(Paths.get(tmp_dynamic_one + "/Ref_TEST_one.csv"), previous_refs_dynamic.toString());
		fileWalker.cleanCSVFile(Paths.get(tmp_dynamic_one + "/Ref_TEST_two.csv"), previous_refs_dynamic.toString());

		List<String[]> oldRefOne = fileWalker.getAllLines(Paths.get(previous_refs_dynamic + "/Ref_TEST_one.csv").toFile());
		List<String[]> oldRefTwo = fileWalker.getAllLines(Paths.get(previous_refs_dynamic + "/Ref_TEST_two.csv").toFile());

		assertThat("Test 13 => The size of oldRefOne should be 2 => ", oldRefOne, hasSize(2));
		assertThat("Test 13 => The size of oldRefTwo should be 3 => ", oldRefTwo, hasSize(3));

		// New reference files
		fileWalker.cleanCSVFile(Paths.get(tmp_dynamic_two + "/Ref_TEST_one.csv"), previous_refs_dynamic.toString());
		fileWalker.cleanCSVFile(Paths.get(tmp_dynamic_two + "/Ref_TEST_two.csv"), previous_refs_dynamic.toString());

		List<String[]> newRefOne = fileWalker.getAllLines(Paths.get(previous_refs_dynamic + "/Ref_TEST_one.csv").toFile());
		List<String[]> newRefTwo = fileWalker.getAllLines(Paths.get(previous_refs_dynamic + "/Ref_TEST_two.csv").toFile());

		assertThat("Test 13 => The size of newRefOne should be 3 => ", newRefOne, hasSize(3));
		assertThat("Test 13 => The size of newRefTwo should be 5 => ", newRefTwo, hasSize(5));
	}
	
	/**
	 * Test Case - 14
	 * 
	 * Method      : FileWalker.cleanFolder(Path folder) 
	 * Input       : tmp_dyamic_one
	 * Expectation : The tmp_dynamic_one folder shouldn't exist after deleting.
	 * 
	 * @throws Exception
	 */
	@Test
	public void cleanFolder_TmpDynamicOneFolder_FolderShouldNotExistTrue() throws Exception {
		System.out.println("Test 14 => cleanFolder(tmp_dynamic_one) => Folder shouldn't exist.");

		List<Path> files = fileWalker.listCSVFilesOfFolderWithReadyFlag(tmp_dynamic_one);
		for (Path file : files) {
			fileWalker.cleanCSVFile(file, previous_refs_dynamic.toString());
		}
		fileWalker.cleanFolder(tmp_dynamic_one);
		
		assertFalse("Test 14 => tmp_dynamic_one folder shouldn't exist after cleaning => ", tmp_dynamic_one.toFile().exists());
	}
	
	/**
	 * Test Case - 15
	 * 
	 * Method      : FileWalker.checkFilePermission(File file) 
	 * Input       : check_permission_static
	 * Expectation : Folder with 770 and file with 660 should return true. Otherwise, return false.
	 * 
	 * @throws Exception
	 */
	@Test
	public void checkFilePermission_CheckPermissionFolder() throws Exception {
		System.out.println("Test 15 => checkFilePermission()");
	
		assertFalse("Test 15 => folder_770/file_550.csv false => ", fileWalker.checkFilePermission(Paths.get(check_permission_static + "folder_770/file_550.csv").toFile()));
		assertTrue("Test 15 => folder_770/file_660.csv true => ", fileWalker.checkFilePermission(Paths.get(check_permission_static + "folder_770/file_660.csv").toFile()));
	}
}
