package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CookieManagerLocalTest {
	private final Class<CookieManager> klass;
	private CookieManager cookieManager;
	private Encoder encoder;

	public CookieManagerLocalTest(Class<CookieManager> klass) {
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
		encoder = new JavaNetEncoder(Encoder.UTF_8);
	}

	@Test
	public void testGetCookiesStartsEmpty() throws Exception {
		assertArrayEquals(new String[] { },
				cookieManager.getCookiesFor("http://www.empty.com/", new Hashtable<String, String>()));
	}

	@Test
	public void testGetCookie2sStartsEmpty() throws Exception {
		assertArrayEquals(new String[] { },
				cookieManager.getCookie2sFor("http://www.empty.com/", new Hashtable<String, String>()));
	}
	@Test
	public void testAddCookies() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		String anotherSite = "http://www." + randomString(9) + ".com/";
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = randomString();
		String value = randomString();
		cookies.put(name, value);
		cookieManager.addCookies(exampleSite, cookies, encoder);
		
		assertArrayEquals("Should have cookie in store, one was added for " + exampleSite,
				new String[] { name + '=' + value },
				cookieManager.getCookie2sFor(exampleSite, new Hashtable<String, String>()));
		
		assertArrayEquals("Should not have cookie in store, none were added for " + anotherSite,
						new String[] { },
						cookieManager.getCookiesFor(anotherSite, new Hashtable<String, String>()));
		
	}
	


	@Test
	public void testAddCookiesSeparately() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = randomString();
		String value = randomString();
		cookies.put(name, value);
		
		Hashtable<String, String> otherCookies = new Hashtable<String, String>();
		String otherName = randomString();
		String otherValue = randomString();
		cookies.put(otherName, otherValue);
		
		cookieManager.addCookies(exampleSite, cookies, encoder);
		cookieManager.addCookies(exampleSite, otherCookies, encoder);
		
		assertArrayEquals("Should have two separate cookies for " + exampleSite,
				new String[] { name + '=' + value, otherName + '=' + otherValue },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));
	}
	

	@Test
	public void testOverwriteCookiesWithSameName() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = randomString();
		String value = randomString();
		cookies.put(name, value);
		
		Hashtable<String, String> otherCookies = new Hashtable<String, String>();
		String otherValue = randomString();
		cookies.put(name, otherValue);
		
		cookieManager.addCookies(exampleSite, cookies, encoder);
		cookieManager.addCookies(exampleSite, otherCookies, encoder);
		
		assertArrayEquals("Should have only one cookie in store, with overwritten value.",
				new String[] { name + '=' + otherValue },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));
	}
	

	@Test
	public void testEncodesCookies() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = "Several words in here";
		String value = "Several other words in here";
		cookies.put(name, value);
		
		cookieManager.addCookies(exampleSite, cookies, encoder);
		
		assertArrayEquals("Should have only one cookie in store, with overwritten value.",
				new String[] { encoder.encode(name) + '=' + encoder.encode(value) },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));
	}
}
