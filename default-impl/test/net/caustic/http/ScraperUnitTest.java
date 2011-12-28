package net.caustic.http;

import java.util.Arrays;
import java.util.Hashtable;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.http.Cookies;
import net.caustic.http.HttpBrowser;
import net.caustic.log.SystemErrLogger;
import net.caustic.regexp.Pattern;
import net.caustic.uri.URILoader;
import net.caustic.util.StringMap;
import net.caustic.util.StringUtils;
import static org.junit.Assert.*;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class ScraperUnitTest {
	
	private static final String uri = StringUtils.USER_DIR;
	
	private @Capturing URILoader loader;
	private @Capturing HttpBrowser browser;
	private @NonStrict StringMap tags;
	private @NonStrict Cookies cookies, respCookies;
	
	private Scraper scraper;
	
	@Before
	public void setUp() {
		scraper = new DefaultScraper();
		scraper.register(new SystemErrLogger());
	}
	
	@Test
	public void testDeserializeSimpleLoadFromJsonSucceeds() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies);
		}};
		
		JSONObject load = new JSONObject().put("load", "http://www.foo.com/");
		
		Response response = scraper.scrape(new Request("id",
				load.toString(), uri, null, tags, cookies, true));
		
		assertNull(response.values);
		assertEquals("bar", response.content);
		assertEquals(respCookies, response.cookies);
		assertArrayEquals(new String[] {}, response.children);
	}

	@Test
	public void testNoForceWaits() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies); times = 0;
		}};
		
		JSONObject load = new JSONObject().put("load", "http://www.foo.com/");
		
		Response response = scraper.scrape(new Request("id",
				load.toString(), uri, null, tags, cookies, false));
		
		assertNull(response.values);
		assertNull(response.content);
		assertNull(response.cookies);
		assertTrue(response.wait);
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {		
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put("load", "http://www.foo.com/").toString();
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies);
		}};
		
		Response response = scraper.scrape(new Request("id", "/uri", uri, null, tags, cookies, true));
		
		assertNull(response.values);
		assertEquals("bar", response.content);
		assertEquals(respCookies, response.cookies);
		assertArrayEquals(new String[] {}, response.children);
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceedsAndMatchesAll() throws Exception {
		JSONObject find = new JSONObject().put("find", "foo");
		
		Response response = scraper.scrape(new Request("id", find.toString(), uri,
				"foo foo bar", tags, cookies, true));
		assertNull(response.content);
		assertNull(response.cookies);
		assertEquals("foo", response.name);
		assertArrayEquals(new String[] { "foo", "foo" }, response.values);
		assertArrayEquals(new String[] {}, response.children);
	}

	@Test
	public void testEmptyObjFails() throws Exception {		
		Response response = scraper.scrape(new Request("id", "{}", uri, null, tags, cookies, true));
		assertNotNull(response.failedBecause);
	}
	
	@Test
	public void testPointerJSON() throws Exception {
		
		new Expectations() {{
			loader.load("/pointer"); result = "/uri";
			loader.load("/uri"); result = new JSONObject().put("find", "needle").toString();
		}};
		
		Response response = scraper.scrape(new Request("id", "/pointer", uri,
				"haystack needle haystack", tags, cookies, false));
		
		assertNull(response.content);
		assertNull(response.cookies);
		assertEquals("needle", response.name);
		assertArrayEquals(new String[] { "needle" }, response.values);
	}
	
	@Test
	public void testArrayOfJSON(@Mocked HttpBrowser browser) throws Exception {
		JSONArray ary = new JSONArray();
		
		ary.put(new JSONObject().put("find", "^foo$"));
		ary.put(new JSONObject().put("load", "http://www.google.com/"));
		
		Response response = scraper.scrape(new Request("id", ary.toString(), uri,
				"foo bar", tags, cookies, false));
		
		assertNull(response.content);
		assertNull(response.values);
		
		assertEquals(2, response.children.length);
		assertTrue(Arrays.asList(response.children).contains(ary.getString(0)));
		assertTrue(Arrays.asList(response.children).contains(ary.getString(1)));		
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		JSONObject bad = new JSONObject().put("foo", "bar");
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				null, tags, cookies, false));
		assertNotNull(response.failedBecause);
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		JSONObject bad = new JSONObject();
		bad.put("find", "^foo$");
		bad.put("load", "http://www.foo.com/");
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertNotNull(response.failedBecause);
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", 10);
		bad.put("match", 5);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertNotNull(response.failedBecause);
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("min", 10);
		bad.put("match", 5);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertNotNull(response.failedBecause);
	}

	@Test
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", 6);
		bad.put("min", 4);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertNotNull(response.failedBecause);
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", -4);
		bad.put("min", -2);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertNotNull(response.failedBecause);
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject obj = new JSONObject()
			.put("extends", new JSONObject().put("find", "^foo$"));
		
		Response response = scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.values);
	}
	
	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put("find", "^foo$").toString();
		}};
		
		JSONObject obj = new JSONObject().put("extends", "/uri");
		
		Response response = scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.values);
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject()
			.put("extends", new JSONArray().put(new JSONObject().put("find", "^foo$")));
		
		Response response = scraper.scrape(new Request("uri", extendedFind.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.values);
	}	
	
	@Test
	public void testExtendsThenViaURI() throws Exception {
		final JSONArray then = new JSONArray()
			.put("/another-uri")
			.put(new JSONObject().put("find", "foo"));
		
		new Expectations() {{
			loader.load("/uri");
				result = new JSONObject().put("load", "http://foo.com")
										.put("then", then).toString();
			browser.request("http://foo.com", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies);
		}};
		
		JSONObject obj = new JSONObject()
			.put("extends", "/uri");

		Response response = scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, true));
				
		assertEquals(2, response.children.length);
		assertTrue(Arrays.asList(response.children).contains(then.get(0).toString()));
		assertTrue(Arrays.asList(response.children).contains(then.get(1).toString()));		
	}
	/*
	@Test
	public void textExtendsAccretes() throws Exception {
		JSONObject somePosts =
				new JSONObject()
					.put(POSTS, new JSONObject()
						.put(randomString(), randomString())
						.put(randomString(), randomString()));
		JSONObject morePosts = 
				new JSONObject()
					.put(POSTS,  new JSONObject()
						.put(randomString(), randomString())
						.put(randomString(), randomString()));
		load.put(EXTENDS, new JSONArray().put(somePosts).put(morePosts));
		new Expectations() {{
			browser.post(urlString, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any,
					}));
		}};
		
		Execution exc;
		exc = deserializer.deserializeString(load.toString(), variables, userDir);
		Instruction instruction = (Instruction) exc.getExecuted();
		
		instruction.execute(null, variables);
	}
	*/

	@Test
	public void testDeserializeSimpleHead() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "head", (Hashtable) any, (Cookies) any, null); times = 1;
				result = new BrowserResponse(null, respCookies);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put("load", "http://www.foo.com/").put("method", "head");
		scraper.scrape(new Request("id", obj.toString(), uri, null, tags, cookies, true));
	}


	@Test
	public void testDeserializeSimpleGet() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null); times = 1;
				result = new BrowserResponse("bar", respCookies);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put("load", "http://www.foo.com/").put("method", "get");
		scraper.scrape(new Request("id", obj.toString(), uri, null, tags, cookies, true));
	}

	@Test
	public void testDeserializeSimplePostWithoutData() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "post", (Hashtable) any, (Cookies) any, null); times = 1;
				result = new BrowserResponse("bar", respCookies);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put("load", "http://www.foo.com/").put("method", "post");
		scraper.scrape(new Request("id", obj.toString(), uri, null, tags, cookies, true));
	}
}
