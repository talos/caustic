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
	
	private final Constructor<URILoader>[] constructors;
	
	@Mocked private FileLoader fileLoader;
	
	private URILoader insecureURILoader, secureURILoader;
	
	private String fileContents = randomString();
	
	public URILoaderIntegrationTest(Constructor<URILoader>[] constructors) {
		this.constructors = constructors;
	}
	
	@Parameters
	public static List<Constructor<?>[][]> implementations() throws Exception {
		return Arrays.asList(new Constructor<?>[][][] {
				//{ JavaNetURILoader.class.getCongetConstructor(FileLoader.class) }
				{ JavaNetURILoader.class.getConstructors() }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		for(Constructor<URILoader> constructor : constructors) {
			switch(constructor.getParameterTypes().length) {
			case 0:
				secureURILoader = constructor.newInstance();
				break;
			case 1:
				insecureURILoader = constructor.newInstance(fileLoader);
				break;
			}
		}
	}

	@Test
	public void testInsecureLoadFilePath() throws Exception {
		new Expectations() {{
			fileLoader.load("/path/to/file.txt"); result = "foo bar";
		}};
		
		assertEquals("foo bar", insecureURILoader.load("/path/to/file.txt"));
	}

	@Test(expected=UnsupportedSchemeException.class)
	public void testSecureLoadFilePathThrows() throws Exception {
		secureURILoader.load("/path/to/file.txt");
	}

	@Test
	public void testLoadFileURI() throws Exception {
		new Expectations() {{
			fileLoader.load("/path/to/file.txt"); result = fileContents;
		}};
		
		assertEquals(fileContents, insecureURILoader.load("file:/path/to/file.txt"));
	}
	
	@Test(expected=UnsupportedSchemeException.class)
	public void testSecureLoadFileURIThrows() throws Exception {
		secureURILoader.load("file:/path/to/file.txt");
	}
	
	@Test(expected=URILoaderException.class)
	public void testSecureLoadBadScheme() throws Exception {
		secureURILoader.load("foo://www.google.com/");
	}

	@Test(expected=URILoaderException.class)
	public void testInsecureLoadBadScheme() throws Exception {
		insecureURILoader.load("foo://www.google.com/");
	}
	
	@Test
	public void testLoadHttp() throws Exception {
		assertTrue(insecureURILoader.load("http://www.google.com/").contains("google"));
		assertTrue(secureURILoader.load("http://www.google.com/").contains("google"));
	}
	
	@Test
	public void testLoadHttps() throws Exception {
		assertTrue(insecureURILoader.load("https://www.google.com/").contains("google"));
		assertTrue(secureURILoader.load("https://www.google.com/").contains("google"));
	}
}
