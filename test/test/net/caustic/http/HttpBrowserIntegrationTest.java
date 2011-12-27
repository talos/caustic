package net.caustic.http;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequestException;
import net.caustic.http.HttpRequester;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.RateLimitManager;
import net.caustic.util.Encoder;
import net.caustic.util.HashtableUtils;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Run network-dependent tests of {@link HttpBrowser}.  Non-mock dependencies
 * must be provided as parameters.
 * @author realest
 *
 */
@RunWith(Parameterized.class)
public class HttpBrowserIntegrationTest {
	
	@Mocked private Cookies cookies;
	
	private final Class<HttpRequester> requesterKlass;
	private final Class<HttpUtils> utilsKlass;
	
	private HttpBrowser browser;
	
	public HttpBrowserIntegrationTest(Class<HttpRequester> requesterKlass, Class<HttpUtils> utilsKlass) throws Exception {
		this.requesterKlass = requesterKlass;
		this.utilsKlass = utilsKlass;
	}
	
	@Parameters
	public static List<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetHttpRequester.class, JavaNetHttpUtils.class } // java.net implementation
		});
	}
	
	@Before
	public void setUp() throws Exception {
		browser = new HttpBrowser(requesterKlass.newInstance(),
				new RateLimitManager(utilsKlass.newInstance()));
	}

	@Test
	public void testGetGoogle() throws Exception {
		BrowserResponse resp = browser.request("http://www.google.com", "get", HashtableUtils.EMPTY,
				cookies, null);
		
		assertTrue("Google should have the word google in response body.", resp.content.contains("google"));
		assertTrue("Should have received at least one cookie.", resp.cookies.getHosts().length > 0);
	}
	

	@Test
	public void testGetGoogleRandomQuery() throws Exception {
		String url = "http://www.google.com/search?q=bleh";
		BrowserResponse resp = browser.request(url, "get", HashtableUtils.EMPTY, cookies, null);
		assertTrue("Google query for 'bleh' should have the query in response body.",
				resp.content.contains("bleh"));
	}

	@Test(expected=HttpRequestException.class)
	public void testGetFakeInvalidFormatURL() throws Exception {
		browser.request("jfsd//sdkj::dkfj", "get", HashtableUtils.EMPTY, cookies, null);
	}
	
	// If this doesn't throw HttpRequestException, it's possible that your ISP throws up
	// some kind of pseudopage when DNS fails.
	//@Test(expected=HttpRequestException.class)
	@Test
	public void testGetFakeValidFormatURL() throws Exception {
		try {
			browser.request("http://www.thisisnotarealdomainbutwillcauselag1928428.com/", "get", HashtableUtils.EMPTY,
				cookies, null);
			fail("This test is OK if your ISP serves up a page when DNS fails.  Otherwise, something is wrong.");
		} catch(HttpRequestException e) {
			// this is what should happen.
		}
	}
	
	@Test
	public void testAddCookiesForACRIS() throws Exception {
		new Expectations() {{
			cookies.get("a836-acris.nyc.gov"); result = new String[] { "JUMPPAGE", "YES" };
		}};
		BrowserResponse resp = browser.request("http://a836-acris.nyc.gov/Scripts/Coverpage.dll/index",
				"get", HashtableUtils.EMPTY, cookies, null);
		assertTrue("ACRIS should provide access to its property records page if the" +
				"JUMPPAGE cookie is set.", resp.content.contains("Search Property Records"));
	}
	
	@Test
	public void testPostViaACRIS() throws Exception {
		new Expectations() {{
			cookies.get("a836-acris.nyc.gov"); result = new String[] { "JUMPPAGE", "YES" };
		}};
		BrowserResponse resp = browser.request("http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult",
				"post", HashtableUtils.EMPTY, cookies, "hid_borough=3&hid_block=1772&hid_doctype=&hid_lot=74&hid_SearchType=BBL");
		assertTrue(resp.content.contains("PULASKI"));
	}
}
