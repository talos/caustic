package net.caustic.uri;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import net.caustic.file.FileLoader;
import net.caustic.uri.JavaNetURILoader;
import net.caustic.uri.URILoader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class URILoaderIntegrationTest {
	
	private final Constructor<URILoader> constructor;
	
	@Mocked private FileLoader fileLoader;
	
	private URILoader uriLoader;
	
	private String fileContents = randomString();
	
	public URILoaderIntegrationTest(Constructor<URILoader> constructor) {
		this.constructor = constructor;
	}
	
	@Parameters
	public static List<Constructor<?>[]> implementations() throws Exception {
		return Arrays.asList(new Constructor<?>[][] {
				{ JavaNetURILoader.class.getConstructor(FileLoader.class) }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		uriLoader = constructor.newInstance(fileLoader);
	}

	@Test
	public void testLoadFilePath() throws Exception {
		new Expectations() {{
			fileLoader.load("/path/to/file.txt"); result = "foo bar";
		}};
		
		assertEquals("foo bar", uriLoader.load("/path/to/file.txt"));
	}

	@Test
	public void testLoadFileURI() throws Exception {
		new Expectations() {{
			fileLoader.load("/path/to/file.txt"); result = fileContents;
		}};
		
		assertEquals(fileContents, uriLoader.load("file:/path/to/file.txt"));
	}
	
	@Test
	public void testLoadHttp() throws Exception {
		assertTrue(uriLoader.load("http://www.google.com/").contains("google"));
	}
}
