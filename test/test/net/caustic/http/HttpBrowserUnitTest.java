package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.io.InputStreamReader;
import java.util.Hashtable;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequester;
import net.caustic.http.HttpResponse;
import net.caustic.http.RateLimitManager;
import net.caustic.http.ResponseHeaders;
import net.caustic.util.HashtableUtils;

import org.junit.Before;
import org.junit.Test;

public class HttpBrowserUnitTest {
	
	@Mocked private Cookies cookies;
	@Mocked private HttpRequester requester;
	@Mocked private RateLimitManager rateLimitManager;
	@Mocked private HttpUtils utils;
	private HttpBrowser browser;
		
	@Before
	public void setUp() throws Exception {
		browser = new HttpBrowser(requester, rateLimitManager, utils);
	}

	@Test
	public void testHeadURL() throws Exception {
		new Expectations() {
			@Mocked HttpResponse response;
			@Mocked ResponseHeaders responseHeaders;
			{
				requester.head("url", (Hashtable) any); result = response;
				response.getResponseHeaders(); result = responseHeaders;
				response.isSuccess(); result = true;
			}
		};
		BrowserResponse response = browser.request("url", "head", HashtableUtils.EMPTY, cookies, null);
		assertNull(response.content);
		//assertArrayEquals(new String[] {}, response.cookies);
	}

	@Test
	public void testGetURL() throws Exception {
		final String content = randomString();
		new Expectations() {
			@Mocked InputStreamReader contentStream;
			@Mocked ResponseHeaders responseHeaders;
			@Mocked HttpResponse response;
			{
				requester.get("url", (Hashtable) any); result = response;
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
		assertEquals(content, browser.request("url", "get", HashtableUtils.EMPTY, cookies, null).content);
	}
	
}
