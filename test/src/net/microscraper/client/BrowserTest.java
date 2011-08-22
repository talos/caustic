package net.microscraper.client;

import static org.junit.Assert.*;

import net.microscraper.regexp.Pattern;
import net.microscraper.util.NameValuePair;

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
		browser.head(google, new NameValuePair[] {}, new NameValuePair[] {});
	}

	@Test
	public void testGetGoogle() throws Exception {
		String responseBody = browser.get(google, new NameValuePair[] {}, new NameValuePair[] {}, new Pattern[] {});
	}

	@Test
	public void testPostStringNameValuePairArrayNameValuePairArrayPatternArrayNameValuePairArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testPostStringNameValuePairArrayNameValuePairArrayPatternArrayString() {
		fail("Not yet implemented");
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
