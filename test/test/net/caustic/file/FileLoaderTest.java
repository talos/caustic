package net.caustic.file;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import net.caustic.file.FileLoader;
import net.caustic.file.JavaIOFileLoader;
import net.caustic.util.StringUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
	
	
@RunWith(Parameterized.class)
public class FileLoaderTest {
	
	private final Class<FileLoader> klass;
	private FileLoader fileLoader;
	private String userDir;
	
	public FileLoaderTest(final Class<FileLoader> klass) {
		this.klass = klass;
	}

	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaIOFileLoader.class  }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		fileLoader = klass.newInstance();
		userDir = StringUtils.USER_DIR;
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
		String contents = fileLoader.load(userDir + "fixtures/file.txt");
		assertTrue(contents.contains("test file"));
	}
}
