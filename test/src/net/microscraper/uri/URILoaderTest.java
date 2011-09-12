package net.microscraper.uri;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.file.FileLoader;
import net.microscraper.http.HttpBrowser;
import net.microscraper.regexp.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class URILoaderTest {
	
	private final Constructor<URILoader> constructor;
	
	@Mocked private HttpBrowser browser;
	@Mocked private FileLoader fileLoader;
	
	private URILoader uriLoader;

	private String filePath = randomString();
	private String httpPath = randomString();

	private String fileURI = "file:" + filePath;
	private String httpURI = "http://" + httpPath;
	
	private String fileContents = randomString();
	private String httpContents = randomString();
	
	public URILoaderTest(Constructor<URILoader> constructor) {
		this.constructor = constructor;
	}
	
	@Parameters
	public static List<Constructor<?>[]> implementations() throws Exception {
		return Arrays.asList(new Constructor<?>[][] {
				{ JavaNetURILoader.class.getConstructor(HttpBrowser.class, FileLoader.class) }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		uriLoader = constructor.newInstance(browser, fileLoader);
		//uriLoader = getURILoader(browser, fileLoader);
		
		new NonStrictExpectations() {{
			fileLoader.load(filePath); result = fileContents;
			browser.get(httpURI, (Hashtable) any, (Pattern[]) any);
				result = httpContents;
		}};
	}

	@Test
	public void testLoadFilePath() throws Exception {
		assertEquals(fileContents, uriLoader.load(filePath));
	}

	@Test
	public void testLoadFileURI() throws Exception {
		assertEquals(fileContents, uriLoader.load(fileURI));
	}
	
	@Test
	public void testLoadHttp() throws Exception {
		assertEquals(httpContents, uriLoader.load(httpURI));
	}
}
