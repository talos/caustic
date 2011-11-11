package net.caustic.http;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import net.caustic.http.CookieManager;
import net.caustic.http.HttpResponse;
import net.caustic.http.JavaNetCookieManager;
import net.caustic.http.JavaNetHttpRequester;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CookieManagerNetworkTest {
	private final Class<CookieManager> klass;
	private CookieManager cookieManager;

	public CookieManagerNetworkTest(Class<CookieManager> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetCookieManager.class  }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		cookieManager = klass.newInstance();
	}

	@Test
	public void testAddCookiesFromGoogleResponseHeaders() throws Exception {
		String google = "http://www.google.com/";
		HttpResponse resp = new JavaNetHttpRequester().get(google, new Hashtable<String, String>());
		cookieManager.addCookiesFromResponseHeaders(google, resp.getResponseHeaders());
		
		assertTrue("Should have received at least one set-cookie from Google.",
				cookieManager.getCookiesFor(google, new Hashtable<String, String>()).length > 0);
	}
}
