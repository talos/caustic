package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.caustic.http.CookieManager;
import net.caustic.http.JavaNetCookieManager;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CookieManagerLocalTest {
	private final Class<CookieManager> klass;
	private CookieManager cookieManager;

	public CookieManagerLocalTest(Class<CookieManager> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetCookieManager.class  },
				{ BasicCookieManager.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		cookieManager = klass.newInstance();
	}

	@Test
	public void testGetCookiesStartsEmpty() throws Exception {
		assertArrayEquals(new String[] { },
				cookieManager.getCookiesFor("http://www.empty.com/", new Hashtable<String, String>()));
	}

	@Test
	public void testAddCookies() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		String anotherSite = "http://www." + randomString(9) + ".com/";
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = randomString();
		String value = randomString();
		cookies.put(name, value);
		cookieManager.addCookies(exampleSite, cookies);
		
		assertArrayEquals("Should have cookie in store, one was added for " + exampleSite,
				new String[] { name + '=' + value },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));

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
		
		cookieManager.addCookies(exampleSite, cookies);
		cookieManager.addCookies(exampleSite, otherCookies);
		
		Set<String> expected = new HashSet<String>(Arrays.asList(new String[] { name + '=' + value, otherName + '=' + otherValue }));
		Set<String> actual = new HashSet<String>(Arrays.asList(cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>())));
		
		assertTrue("should have added two cookies separately", expected.equals(actual));
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
		
		cookieManager.addCookies(exampleSite, cookies);
		cookieManager.addCookies(exampleSite, otherCookies);
		
		assertArrayEquals("Should have only one cookie in store, with overwritten value.",
				new String[] { name + '=' + otherValue },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));
	}
	

	@Test
	public void testDoesNotEncodeCookies() throws Exception {
		String exampleSite = "http://www." + randomString(10) + ".com/";
		
		Hashtable<String, String> cookies = new Hashtable<String, String>();
		String name = "Several words in here";
		String value = "Several other words in here";
		cookies.put(name, value);
		
		cookieManager.addCookies(exampleSite, cookies);
		
		assertArrayEquals("Should not have modified cookie name or value.",
				new String[] { name + '=' + value },
				cookieManager.getCookiesFor(exampleSite, new Hashtable<String, String>()));
	}
}
