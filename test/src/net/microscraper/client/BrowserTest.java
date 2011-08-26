package net.microscraper.client;

import static org.junit.Assert.*;

import java.util.Hashtable;

import net.microscraper.regexp.Pattern;

import org.junit.Before;
import org.junit.Test;

public abstract class BrowserTest {
	
	private static final String google = "http://www.google.com/";
	
	private Browser browser;
	
	protected abstract Browser getBrowser();
	
	@Before
	public void setUp() throws Exception {
		browser = getBrowser();
	}

	@Test
	public void testHeadGoogle() throws Exception {
		browser.head(google, new Hashtable<String, String>());
	}

	@Test
	public void testGetGoogle() throws Exception {
		String responseBody = browser.get(google, new Hashtable<String, String>(), new Pattern[] {});
		assertTrue(responseBody.contains("google"));
	}
	
	@Test
	public void testAddCookiesViaACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/Coverpage.dll/index";
		browser.addCookies(new Cookie[] { new BasicCookie(url, "JUMPPAGE", "YES") } );
		String responseBody = browser.get(url, new Hashtable<String, String>(), new Pattern[] {});
		assertTrue("ACRIS should provide access to its property records page if the" +
				"JUMPPAGE cookie is set.", responseBody.contains("Search Property Records"));
	}
	
	@Test
	public void testPostViaACRIS() throws Exception {
		String url = "http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult";
		String encodedPostData = "hid_borough=3&hid_block=1772&hid_doctype=&hid_lot=74&hid_SearchType=BBL";
		browser.addCookies(new Cookie[] { new BasicCookie(url, "JUMPPAGE", "YES") } );
		String responseBody = browser.post(url, new Hashtable<String, String>(), new Pattern[] {},
				encodedPostData);
		assertTrue(responseBody.contains("PULASKI"));
	}

	@Test
	public void testSetRateLimit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRateLimit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMaxResponseSize() {
		fail("Not yet implemented");
	}

}
