package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import net.microscraper.browser.JavaNetBrowser;
import net.microscraper.browser.JavaNetEncoder;
import net.microscraper.client.Browser;
import net.microscraper.client.Cookie;
import net.microscraper.database.Variables;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.Template;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;


import org.junit.Before;
import org.junit.Test;

public class LoadTest {
	
	@Mocked(capture = 1) private Browser browser;
	@Mocked(capture = 1, methods = "addCookies") private Browser liveBrowser;
	@Mocked private Variables variables;
	private Encoder encoder;
	private Load load;
	private Template url;
	
	@Before
	public void setUp() throws Exception {
		url = Template.compile(randomString(), Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		encoder = new JavaNetEncoder();
		load = new Load(browser, encoder, url);
		liveBrowser = new JavaNetBrowser();
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			browser.head(url.toString(), (Hashtable) any);
		}};
		load.setMethod(Browser.HEAD);
		Execution exc = load.execute(null, variables);
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
		Execution exc = load.execute(null, variables);
		assertTrue(exc.isSuccessful());
		String[] results = (String[]) exc.getExecuted();
		assertEquals("Result should be one-length array.", 1, results.length);
		assertEquals("Result from head should be response.", response, results[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final Template url = Template.compile("http://www.google.com/?q={{" + value + "}}", "{{", "}}");
		new Expectations() {{
			variables.get(name); result = value;
			browser.get("http://www.google.com/?q=" + value, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(browser, encoder, url);
		load.execute(null, variables);
	}
	
	@Test // This test requires a live internet connection.
	public void testLiveBrowserSetsCookies() throws Exception {
		final Template url = Template.compile("http://www.nytimes.com", "{{", "}}");
		new Expectations() {{
			browser.get(url.toString(), (Hashtable) any, (Pattern[]) any);
			browser.addCookies((Cookie[]) any);
		}};
		Load load = new Load(liveBrowser, encoder, url);
	}
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "");
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(Browser.POST);
		load.execute(null, variables);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final Template postData = Template.compile(randomString(), Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		new Expectations() {{
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, variables);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final Template postData = Template.compile(Template.DEFAULT_OPEN_TAG + key + Template.DEFAULT_CLOSE_TAG,
				Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG);
		new Expectations() {{
			variables.get(key); result = value;
			browser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, variables);
	}
}
