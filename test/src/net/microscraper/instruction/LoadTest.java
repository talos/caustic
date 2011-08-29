package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import net.microscraper.database.Database;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.http.ResponseHeaders;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.Template;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;


import org.junit.Before;
import org.junit.Test;

public class LoadTest {
	
	@Mocked private Database database;
	@Mocked(capture = 1) private HttpBrowser browser;
	int id = 0;
	private HttpBrowser liveBrowser;
	private Encoder encoder;
	private Load load;
	private Template url;
	
	@Before
	public void setUp() throws Exception {
		url = new Template(randomString(), Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		load = new Load(browser, encoder, url);
		liveBrowser = new HttpBrowser(new JavaNetHttpRequester(),
				new RateLimitManager(new JavaNetHttpUtils(), RateLimitManager.DEFAULT_RATE_LIMIT),
				new JavaNetCookieManager());
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			browser.head(url.toString(), (Hashtable) any);
		}};
		load.setMethod(HttpBrowser.HEAD);
		Execution exc = load.execute(null, id);
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
		Execution exc = load.execute(null, id);
		assertTrue(exc.isSuccessful());
		String[] results = (String[]) exc.getExecuted();
		assertEquals("Result should be one-length array.", 1, results.length);
		assertEquals("Result from head should be response.", response, results[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final Template url = new Template("http://www.google.com/?q={{" + value + "}}", "{{", "}}", database);
		new Expectations() {{
			database.get(id, name); result = value;
			browser.get("http://www.google.com/?q=" + value, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(browser, encoder, url);
		load.execute(null, id);
	}
	
	@Test // This test requires a live internet connection.
	public void testLiveBrowserSetsCookies() throws Exception {
		final Template url = new Template("http://www.nytimes.com", "{{", "}}", database);
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
		load.execute(null, id);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final Template postData = new Template(randomString(), Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG,database);
		new Expectations() {{
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, id);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final Template postData = new Template(Template.DEFAULT_OPEN_TAG + key + Template.DEFAULT_CLOSE_TAG,
				Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		new Expectations() {{
			database.get(id, key); result = value;
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, id);
	}
}
