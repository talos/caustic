package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.regexp.Pattern;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;


import org.junit.Before;
import org.junit.Test;

public class LoadLocalTest {
	
	@Mocked private DatabaseView input;
	@Injectable private HttpBrowser mockBrowser;
	private Encoder encoder;
	private Load load;
	private StringTemplate url;
	
	@Before
	public void setUp() throws Exception {
		url = StringTemplate.staticTemplate(randomString());
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		load = new Load(mockBrowser, encoder, url);
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			mockBrowser.head(url.toString(), (Hashtable) any);
		}};
		load.setMethod(HttpBrowser.HEAD);
		ScraperResult result = load.execute(null, input);
		assertTrue(result.isSuccess());
		DatabaseView[] views = result.getResultViews();
		assertEquals("Result should be one-length array.", 1, views.length);
		assertEquals("Result view should be input view.", input, views[0]);
	}

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		final String response = randomString();
		new Expectations() {{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
		}};
		ScraperResult result = load.execute(null, input);
		assertTrue(result.isSuccess());
		DatabaseView[] views = result.getResultViews();
		assertEquals("Result should be one-length array.", 1, views.length);
		assertEquals("Result view should be input view.", input, views[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final StringTemplate url = new StringTemplate("http://www.google.com/?q={{" + name + "}}", "{{", "}}");
		new Expectations() {{
			input.get(name); result = value;
			mockBrowser.get("http://www.google.com/?q=" + value, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(mockBrowser, encoder, url);
		load.execute(null, input);
	}
	
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "");
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(HttpBrowser.POST);
		load.execute(null, input);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final StringTemplate postData = new StringTemplate(randomString(), StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, input);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final StringTemplate postData = new StringTemplate(StringTemplate.DEFAULT_OPEN_TAG + key + StringTemplate.DEFAULT_CLOSE_TAG,
				StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		new Expectations() {{
			input.get(key); result = value;
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, input);
	}
}
