package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import net.microscraper.regexp.Pattern;
import net.microscraper.util.HttpUtils;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;

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
public class HttpBrowserNetworkTest {
	private final Class<HttpRequester> requesterKlass;
	private final Class<HttpUtils> utilsKlass;
	private final Class<CookieManager> cookieManagerKlass;
	
	private HttpBrowser browser;
	
	public HttpBrowserNetworkTest(Class<HttpRequester> requesterKlass, Class<HttpUtils> utilsKlass,
			Class<CookieManager> cookieManagerKlass) {
		this.requesterKlass = requesterKlass;
		this.utilsKlass = utilsKlass;
		this.cookieManagerKlass = cookieManagerKlass;
	}
	
	@Parameters
	public static List<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetHttpRequester.class, JavaNetHttpUtils.class, JavaNetCookieManager.class } // java.net implementation
		});
	}
	
	@Before
	public void setUp() throws Exception {
		browser = new HttpBrowser(requesterKlass.newInstance(),
				new RateLimitManager(utilsKlass.newInstance()),
				cookieManagerKlass.newInstance());
	}

	@Test
	public void testGetGoogle() throws Exception {
		String url = "http://www.google.com/";
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
		assertTrue("Google should have the word google in response body.", responseBody.contains("google"));
	}
	

	@Test
	public void testGetGoogleRandomQuery() throws Exception {
		String query = randomString();
		String url = "http://www.google.com/search?q=" + query;
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
		assertTrue("Google query for " + query +" should have the query in response body.",
				responseBody.contains(query));
	}

	@Test(expected=IOException.class)
	public void testGetFakeInvalidFormatURL() throws Exception {
		String url = randomString(30);
		browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
	}
	
	// If this doesn't throw IOException, it's possible that your ISP throws up
	// some kind of pseudopage when DNS fails.
	@Test(expected=IOException.class)
	public void testGetFakeValidFormatURL() throws Exception {
		String url = "http://www." + randomString(30) + ".com/";
		browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
	}
	
	@Test
	public void testAddCookiesViaACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/Coverpage.dll/index";
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		cookies.put("JUMPPAGE", "YES");
		browser.addCookies(url, cookies, new JavaNetEncoder("UTF-8"));
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
		assertTrue("ACRIS should provide access to its property records page if the" +
				"JUMPPAGE cookie is set.", responseBody.contains("Search Property Records"));
	}
	
	@Test
	public void testPostViaACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult";
		String encodedPostData = "hid_borough=3&hid_block=1772&hid_doctype=&hid_lot=74&hid_SearchType=BBL";
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		cookies.put("JUMPPAGE", "YES");
		browser.addCookies(url, cookies, new JavaNetEncoder("UTF-8"));
		String responseBody = browser.post(url, new Hashtable<String, String>(), new Pattern[] {},
				encodedPostData);
		assertTrue(responseBody.contains("PULASKI"));
	}
}
