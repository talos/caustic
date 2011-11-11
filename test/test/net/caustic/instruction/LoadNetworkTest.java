package net.caustic.instruction;

import static org.junit.Assert.*;

import java.util.Hashtable;

import mockit.Mocked;
import net.caustic.database.DatabaseView;
import net.caustic.http.CookieManager;
import net.caustic.http.HttpBrowser;
import net.caustic.http.JavaNetCookieManager;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.JavaNetHttpUtils;
import net.caustic.http.RateLimitManager;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.regexp.StringTemplate;
import net.caustic.util.StaticStringTemplate;

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
		InstructionResult result = load.execute(null, input, liveBrowser);
		assertTrue(result.isSuccess());
		assertEquals(1, result.getResults().length);
		assertTrue(cookieManager.getCookiesFor(url.toString(), new Hashtable<String, String>()).length > 0);
	}

}
