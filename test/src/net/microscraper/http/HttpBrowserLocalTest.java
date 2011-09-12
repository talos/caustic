package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.HttpResponse;
import net.microscraper.http.RateLimitManager;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.regexp.Pattern;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public class HttpBrowserLocalTest {
	
	private static final String google = "http://www.google.com/";
	
	@Mocked private HttpRequester requester;
	@Mocked private RateLimitManager rateLimitManager;
	@Mocked private CookieManager cookieManager;
	private HttpBrowser browser;
		
	@Before
	public void setUp() throws Exception {
		browser = new HttpBrowser(requester, rateLimitManager, cookieManager);
	}

	@Test
	public void testHeadGoogle() throws Exception {
		new Expectations() {
			@Mocked HttpResponse response;
			@Mocked ResponseHeaders responseHeaders;
			{
				requester.head(google, (Hashtable) any); result = response;
				response.getResponseHeaders(); result = responseHeaders;
				response.isSuccess(); result = true;
			}
		};
		browser.head(google, new Hashtable<String, String>());
	}

	@Test
	public void testGetGoogle() throws Exception {
		final String content = randomString();
		new Expectations() {
			@Mocked InputStreamReader contentStream;
			@Mocked ResponseHeaders responseHeaders;
			@Mocked HttpResponse response;
			{
				requester.get(google, (Hashtable) any); result = response;
				response.getResponseHeaders(); result = responseHeaders;
				response.isSuccess(); result = true;
				response.getContentStream(); result = contentStream;
				contentStream.read((char[]) any);
					result = new Delegate() {
						int read(char[] buffer) {
							for(int i = 0 ; i < content.length() ; i ++) {
								buffer[i] = content.charAt(i);
							}
							return content.length();
						}
					};
					result = -1;
				
				contentStream.close();
			}
		};
		assertEquals(content, browser.get(google, new Hashtable<String, String>(), new Pattern[] {}));
	}
	
	@Test
	public void testSetMaxResponseSize() {
		fail("Not yet implemented");
	}

}
