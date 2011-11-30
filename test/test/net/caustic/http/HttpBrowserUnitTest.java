package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import net.caustic.database.Database;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequester;
import net.caustic.http.HttpResponse;
import net.caustic.http.RateLimitManager;
import net.caustic.http.ResponseHeaders;
import net.caustic.regexp.Pattern;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.HashtableUtils;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public class HttpBrowserUnitTest {
		
	@Mocked private HttpRequester requester;
	@Mocked private RateLimitManager rateLimitManager;
	@Mocked private HttpUtils utils;
	@Mocked private Encoder encoder;
	@Mocked private Database db;
	@Mocked private Scope scope;
	private HttpBrowser browser;
		
	@Before
	public void setUp() throws Exception {
		browser = new HttpBrowser(requester, rateLimitManager, utils, encoder);
	}

	@Test
	public void testHeadGoogle() throws Exception {
		new Expectations() {
			@Mocked HttpResponse response;
			@Mocked ResponseHeaders responseHeaders;
			{
				requester.head("http://www.google.com/", (Hashtable) any); result = response;
				response.getResponseHeaders(); result = responseHeaders;
				response.isSuccess(); result = true;
			}
		};
		browser.head("http://www.google.com/", HashtableUtils.EMPTY, db, scope);
	}

	@Test
	public void testGetGoogle() throws Exception {
		final String content = randomString();
		new Expectations() {
			@Mocked InputStreamReader contentStream;
			@Mocked ResponseHeaders responseHeaders;
			@Mocked HttpResponse response;
			{
				requester.get("http://www.google.com/", (Hashtable) any); result = response;
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
		assertEquals(content, browser.get("http://www.google.com/", HashtableUtils.EMPTY,
				new Pattern[] {}, db, scope));
	}
	
}
