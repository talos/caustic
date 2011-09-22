package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.util.Hashtable;

import mockit.Mocked;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.util.JavaNetHttpUtils;
import net.microscraper.util.StaticStringTemplate;

import org.junit.Before;
import org.junit.Test;

public class LoadNetworkTest {
	@Mocked private DatabaseView input;
	private HttpBrowser liveBrowser;
	private CookieManager cookieManager;

	@Before
	public void setUp() throws Exception {
		cookieManager = new JavaNetCookieManager();	
		liveBrowser = new HttpBrowser(new JavaNetHttpRequester(),
				new RateLimitManager(new JavaNetHttpUtils()),
				cookieManager);

	}
	
	@Test // This test requires a live internet connection.
	public void testLiveBrowserSetsCookies() throws Exception {
		final StringTemplate url = new StaticStringTemplate("http://www.nytimes.com");
		
		Load load = new Load(url);
		LoadResult result = load.execute(liveBrowser, input);
		assertNotNull(result.getResponseBody());
		assertTrue(cookieManager.getCookiesFor(url.toString(), new Hashtable<String, String>()).length > 0);
	}

}
