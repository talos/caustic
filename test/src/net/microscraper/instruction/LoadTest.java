package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;


import org.junit.Before;
import org.junit.Test;

public class LoadTest {
	
	@Mocked private Database database;
	@Mocked private Scope scope;
	@Mocked(capture = 1) private HttpBrowser browser;
	private HttpBrowser liveBrowser;
	private Encoder encoder;
	private Load load;
	private StringTemplate url;
	
	@Before
	public void setUp() throws Exception {
		url = new StringTemplate(randomString(), StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		load = new Load(browser, encoder, url);
		liveBrowser = new HttpBrowser(new JavaNetHttpRequester(),
				new RateLimitManager(new JavaNetHttpUtils()),
				new JavaNetCookieManager());
		new NonStrictExpectations( ) {{
			database.getDefaultScope(); result = scope;
		}};
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			browser.head(url.toString(), (Hashtable) any);
		}};
		load.setMethod(HttpBrowser.HEAD);
		Execution exc = load.execute(null, scope);
		assertTrue(exc.isSuccessful());
		String[] results = (String[]) exc.getExecuted();
		assertEquals("Result should be one-length array.", 1, results.length);
		assertEquals("Result from head should be zero-length string.", 0, results[0].length());
	}

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		final String response = randomString();
		new Expectations() {{
			browser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
		}};
		Execution exc = load.execute(null, scope);
		assertTrue(exc.isSuccessful());
		String[] results = (String[]) exc.getExecuted();
		assertEquals("Result should be one-length array.", 1, results.length);
		assertEquals("Result from head should be response.", response, results[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final StringTemplate url = new StringTemplate("http://www.google.com/?q={{" + value + "}}", "{{", "}}", database);
		new Expectations() {{
			database.get(scope, name); result = value;
			browser.get("http://www.google.com/?q=" + value, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(browser, encoder, url);
		load.execute(null, scope);
	}
	
	@Test // This test requires a live internet connection.
	public void testLiveBrowserSetsCookies() throws Exception {
		final StringTemplate url = new StringTemplate("http://www.nytimes.com", "{{", "}}", database);
		new Expectations() {
			@Mocked CookieManager cookieManager;
			{
				browser.get(url.toString(), (Hashtable) any, (Pattern[]) any);
				cookieManager.addCookiesFromResponseHeaders(url.toString(), (ResponseHeaders) any);
			}
		};
		Load load = new Load(liveBrowser, encoder, url);
	}
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "");
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(HttpBrowser.POST);
		load.execute(null, scope);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final StringTemplate postData = new StringTemplate(randomString(), StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG,database);
		new Expectations() {{
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, scope);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final StringTemplate postData = new StringTemplate(StringTemplate.DEFAULT_OPEN_TAG + key + StringTemplate.DEFAULT_CLOSE_TAG,
				StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		new Expectations() {{
			database.get(scope, key); result = value;
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, scope);
	}
}
