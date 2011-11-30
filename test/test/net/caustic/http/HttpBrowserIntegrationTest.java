package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import net.caustic.database.Database;
import net.caustic.database.MemoryDatabase;
import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequestException;
import net.caustic.http.HttpRequester;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.RateLimitManager;
import net.caustic.regexp.Pattern;
import net.caustic.scope.Scope;
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
	private final Class<HttpRequester> requesterKlass;
	private final Class<HttpUtils> utilsKlass;
	private final Constructor<Encoder> encoderConstructor;
	
	private Database db;
	private Scope scope;
	private HttpBrowser browser;
	private Encoder encoder;
	
	public HttpBrowserIntegrationTest(Class<HttpRequester> requesterKlass, Class<HttpUtils> utilsKlass,
			Class<Encoder> encoderKlass) throws Exception {
		this.requesterKlass = requesterKlass;
		this.utilsKlass = utilsKlass;
		encoderConstructor = encoderKlass.getConstructor(String.class);
	}
	
	@Parameters
	public static List<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetHttpRequester.class, JavaNetHttpUtils.class, JavaNetEncoder.class } // java.net implementation
		});
	}
	
	@Before
	public void setUp() throws Exception {
		encoder = encoderConstructor.newInstance(Encoder.UTF_8);
		browser = new HttpBrowser(requesterKlass.newInstance(),
				new RateLimitManager(utilsKlass.newInstance()),
				utilsKlass.newInstance(),
				encoder);
		db = new MemoryDatabase();
		scope = db.newDefaultScope();
	}

	@Test
	public void testGetGoogle() throws Exception {
		String url = "http://www.google.com/";
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {}, db, scope);
		assertTrue("Google should have the word google in response body.", responseBody.contains("google"));
	}
	

	@Test
	public void testGetGoogleRandomQuery() throws Exception {
		String query = randomString();
		String url = "http://www.google.com/search?q=" + query;
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {}, db, scope);
		assertTrue("Google query for " + query +" should have the query in response body.",
				responseBody.contains(query));
	}

	@Test(expected=HttpRequestException.class)
	public void testGetFakeInvalidFormatURL() throws Exception {
		String url = randomString(30);
		browser.get(url, new Hashtable<String, String>(), new Pattern[] {}, db, scope);
	}
	
	// If this doesn't throw HttpRequestException, it's possible that your ISP throws up
	// some kind of pseudopage when DNS fails.
	@Test(expected=HttpRequestException.class)
	public void testGetFakeValidFormatURL() throws Exception {
		String url = "http://www." + randomString(30) + ".com/";
		browser.get(url, new Hashtable<String, String>(), new Pattern[] {}, db, scope);
	}
	
	@Test
	public void testAddCookiesForACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/Coverpage.dll/index";
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		cookies.put("JUMPPAGE", "YES");
		db.addCookie(scope, "a386-acris.nyc.gov", "JUMPPAGE", "YES");
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {}, db, scope);
		assertTrue("ACRIS should provide access to its property records page if the" +
				"JUMPPAGE cookie is set.", responseBody.contains("Search Property Records"));
	}
	
	@Test
	public void testPostViaACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult";
		String encodedPostData = "hid_borough=3&hid_block=1772&hid_doctype=&hid_lot=74&hid_SearchType=BBL";
		db.addCookie(scope, "a386-acris.nyc.gov", "JUMPPAGE", "YES");
		String responseBody = browser.post(url, HashtableUtils.EMPTY, new Pattern[] {},
				encodedPostData, db, scope);
		assertTrue(responseBody.contains("PULASKI"));
	}

	@Test
	public void testAddCookiesFromGoogleResponseHeaders() throws Exception {
		browser.get("http://www.google.com/", HashtableUtils.EMPTY, new Pattern[] {}, db, scope);
		
		assertTrue("Should have received at least one Set-Cookie from Google.", 
				db.getCookies(scope, "www.google.com", encoder).length > 0);
	}
}
