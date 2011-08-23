package net.microscraper.uri;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.file.FileLoader;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.NameValuePair;

import org.junit.Before;
import org.junit.Test;

public abstract class URILoaderTest {
	
	@Mocked private Browser browser;
	@Mocked private FileLoader fileLoader;
	
	private URILoader uriLoader;
	
	private String filePath = randomString();
	private String httpPath = randomString();
	
	private String fileContents = randomString();
	private String httpContents = randomString();
	
	protected abstract URILoader getURILoader(Browser browser,
			FileLoader fileLoader) throws Exception;
	
	@Before
	public void setUp() throws Exception {
		uriLoader = getURILoader(browser, fileLoader);
		
		new NonStrictExpectations() {{
			fileLoader.load(filePath); result = fileContents;
			browser.get(httpPath, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
				result = httpContents;
		}};
	}

	@Test
	public void testLoadFile() throws Exception {
		assertEquals(fileContents, uriLoader.load(filePath));
	}
	
	@Test
	public void testLoadHttp() throws Exception {
		assertEquals(httpContents, uriLoader.load(httpPath));
	}
}
