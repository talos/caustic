package net.microscraper.file;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public abstract class FileLoaderTest {
	
	private FileLoader fileLoader;
	private String userDir;
	
	protected abstract FileLoader getFileLoader() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		fileLoader = getFileLoader();
		userDir = System.getProperty("user.dir");
	}

	@Test(expected = IOException.class)
	public void testLoadNonexistentThrowsException() throws Exception {
		fileLoader.load("/" + randomString());
	}
	
	@Test(expected = IOException.class)
	public void testLoadURLThrowsException() throws Exception {
		fileLoader.load("http://www.google.com/");
	}
	
	@Test
	public void testLoadFixture() throws Exception {
		String contents = fileLoader.load(userDir + "/fixtures/file.txt");
		assertEquals("test file", contents);
	}
}
