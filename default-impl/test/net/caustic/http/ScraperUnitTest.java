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
import net.caustic.Response.DoneFind;
import net.caustic.Response.DoneLoad;
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
		scraper = new DefaultScraper(true);
		scraper.register(new SystemErrLogger());
	}
		
	@Test
	public void testDeserializeSimpleLoadFromJsonSucceeds() throws Exception {
		new Expectations() {{
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies);
		}};
		
		JSONObject load = new JSONObject().put("load", "http://www.foo.com/");
		
		DoneLoad response = (DoneLoad) scraper.scrape(new Request("id",
				load.toString(), uri, null, tags, cookies, true));
		
		assertEquals("bar", response.getChildren().keySet().toArray()[0]);
		assertEquals(respCookies, response.getCookies());
		
		assertArrayEquals(new Response[] {}, (Response[]) response.getChildren().get("bar"));
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
		
		assertEquals(Response.WAIT, response.getStatus());
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {		
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put("load", "http://www.foo.com/").toString();
			browser.request("http://www.foo.com/", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("bar", respCookies);
		}};
		
		DoneLoad response = (DoneLoad) scraper.scrape(new Request("id", "/uri", uri, null, tags, cookies, true));
		
		assertEquals("bar", response.getChildren().keySet().toArray()[0]);
		assertEquals(respCookies, response.getCookies());
		assertArrayEquals(new Response[] {}, (Response[]) response.getChildren().get("bar"));
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceedsAndMatchesAll() throws Exception {
		JSONObject find = new JSONObject().put("find", "foo");
		
		DoneFind response = (DoneFind) scraper.scrape(new Request("id", find.toString(), uri,
				"foo foo bar", tags, cookies, true));

		assertEquals("foo", response.getName());
		assertArrayEquals(new String[] { "foo" }, response.getChildren().keySet().toArray());
		assertArrayEquals(new Response[] {}, (Response[]) response.getChildren().get("foo"));
	}

	@Test
	public void testEmptyObjFails() throws Exception {		
		Response response = scraper.scrape(new Request("id", "{}", uri, null, tags, cookies, true));
		assertEquals(Response.FAILED, response.getStatus());
	}
	
	@Test
	public void testPointerJSON() throws Exception {
		
		new Expectations() {{
			loader.load("/pointer"); result = "/uri";
			loader.load("/uri"); result = new JSONObject().put("find", "needle").toString();
		}};
		
		DoneFind response = (DoneFind) scraper.scrape(new Request("id", "/pointer", uri,
				"haystack needle haystack", tags, cookies, false));
		
		assertEquals("needle", response.getName());
		assertArrayEquals(new String[] { "needle" }, response.getChildren().keySet().toArray());
	}
	
	@Test
	public void testArrayOfJSON(@Mocked HttpBrowser browser) throws Exception {
		JSONArray ary = new JSONArray();
		
		JSONObject loadGoogle = new JSONObject().put("load", "http://www.google.com/");
		
		ary.put(new JSONObject().put("find", "^foo"));
		ary.put(loadGoogle);
		
		Response.Reference response = (Response.Reference) scraper.scrape(
				new Request("id", ary.toString(), uri,
				"foo bar", tags, cookies, false));
		
		Response[] referenced = response.getReferenced();
		assertEquals(2, referenced.length);
		assertEquals(Response.DONE_FIND, referenced[0].getStatus());
		assertEquals(Response.WAIT, referenced[1].getStatus());
		
		DoneFind doneFind = (DoneFind) referenced[0];
		Response.Wait wait = (Response.Wait) referenced[1]; 

		assertEquals(loadGoogle.toString(), wait.getInstructionJSON().value.toString());
		assertEquals("http://www.google.com/", wait.getName());
		
		Hashtable<String, Response[]> findChildren = doneFind.getChildren();
		assertEquals("^foo", doneFind.getName());
		assertEquals("foo", findChildren.keySet().toArray()[0]);
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		JSONObject bad = new JSONObject().put("foo", "bar");
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				null, tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		JSONObject bad = new JSONObject();
		bad.put("find", "^foo$");
		bad.put("load", "http://www.foo.com/");
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", 10);
		bad.put("match", 5);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("min", 10);
		bad.put("match", 5);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}

	@Test
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", 6);
		bad.put("min", 4);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put("find", "^foo$");
		
		bad.put("max", -4);
		bad.put("min", -2);
		
		Response response = scraper.scrape(new Request("uri", bad.toString(), uri,
				"foo", tags, cookies, false));
		assertEquals(Response.FAILED, response.getStatus());
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject obj = new JSONObject()
			.put("extends", new JSONObject().put("find", "^foo$"));
		
		DoneFind response = (DoneFind) scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.getChildren().keySet().toArray());
	}
	
	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put("find", "^foo$").toString();
		}};
		
		JSONObject obj = new JSONObject().put("extends", "/uri");
		
		DoneFind response = (DoneFind) scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.getChildren().keySet().toArray());
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject()
			.put("extends", new JSONArray().put(new JSONObject().put("find", "^foo$")));
		
		DoneFind response = (DoneFind) scraper.scrape(new Request("uri", extendedFind.toString(), uri,
				"foo", tags, cookies, false));
		assertArrayEquals(new String[] { "foo" }, response.getChildren().keySet().toArray());
	}	
	
	@Test
	public void testExtendsThenViaURI() throws Exception {
		final JSONArray then = new JSONArray()
			.put("/another-uri")
			.put(new JSONObject().put("find", "b\\w+"));
		
		new Expectations() {{
			loader.load("/uri");
				result = new JSONObject().put("load", "http://foo.com")
										.put("then", then).toString();
			browser.request("http://foo.com", "get", (Hashtable) any, (Cookies) any, null);
				result = new BrowserResponse("alpha beta gaga", respCookies);
			loader.load("/another-uri");
				result = new JSONObject().put("load", "http://bar.com").toString();
			//browser.request("http://bar.com", "get", (Hashtable) any, (Cookies) any, null);
			//	result = new BrowserResponse("virgin suicides", respCookies);
				
		}};
		
		JSONObject obj = new JSONObject()
			.put("extends", "/uri");

		DoneLoad response = (DoneLoad) scraper.scrape(new Request("uri", obj.toString(), uri,
				"foo", tags, cookies, true));
		
		Hashtable<String, Response[]> children = response.getChildren();
		
		assertEquals(1, children.size());
		
		Response[] responses = children.get("alpha beta gaga");
		assertEquals(2, responses.length);
		
		System.out.println(responses[0].getInstructionJSON());
		System.out.println(responses[1].getInstructionJSON());
		
		assertEquals(Response.WAIT, responses[0].getStatus());
		assertEquals(Response.DONE_FIND, responses[1].getStatus());

		Response.Wait wait = (Response.Wait) responses[0];
		DoneFind thenFind = (DoneFind) responses[1];
		
		assertEquals(wait.getName(), "http://bar.com");
		//assertEquals(thenLoad.getName(), "http://bar.com");
		//assertArrayEquals(new String[] { "virgin suicides" }, thenLoad.getChildren().keySet().toArray());
		
		assertEquals(thenFind.getName(), "b\\w+");
		assertArrayEquals(new String[] { "beta" }, thenFind.getChildren().keySet().toArray());
		
		//assertTrue(Arrays.asList(
		//		response.getChildren()).containsAll(Arrays.asList(then.get(0), then.get(1).toString())));
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
				result = new BrowserResponse("", respCookies);
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
