package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.util.Hashtable;

import mockit.Mocked;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;

import org.junit.Before;
import org.junit.Test;

public class LoadNetworkTest {
	@Mocked private DatabaseView input;
	private HttpBrowser liveBrowser;
	private CookieManager cookieManager;
	private Encoder encoder;

	@Before
	public void setUp() throws Exception {
		cookieManager = new JavaNetCookieManager();	
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		liveBrowser = new HttpBrowser(new JavaNetHttpRequester(),
				new RateLimitManager(new JavaNetHttpUtils()),
				cookieManager);

	}
	
	@Test // This test requires a live internet connection.
	public void testLiveBrowserSetsCookies() throws Exception {
		final StringTemplate url = StringTemplate.staticTemplate("http://www.nytimes.com");
		
		Load load = new Load(liveBrowser, encoder, url);
		ScraperResult result = load.execute(null, input);
		assertTrue(result.isSuccess());
		assertTrue(cookieManager.getCookiesFor(url.toString(), new Hashtable<String, String>()).length > 0);
	}

}
