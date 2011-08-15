package net.microscraper.util;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import net.microscraper.client.Browser;

import org.junit.Test;

public class BasicVariablesTest {
	
//	@Mocked Browser browser;
	
/*	@Before
	public void setup() {
		 
	}*/

	
	@Test
	public void testExtend() {
		fail("Not yet implemented");
	}

	@Test
	public void testEmpty() {
		fail("Not yet implemented");
	}

	@MockClass(realClass = Browser.class)
	public final class MockBrowser {
		 @Mock
		 String encode(String string, String encoding) throws UnsupportedEncodingException {
			 return URLEncoder.encode(string, encoding);
		 }
		 
		 @Mock
		 String decode(String string, String encoding) throws UnsupportedEncodingException {
			 return URLDecoder.decode(string, encoding);
		 }	      
	}

	@Test
	public void testFromFormEncoded() throws Exception {
	      
		final Browser browser = Mockit.setUpMock(MockBrowser.class);
		
		String formEncoded = "number=6&word=bond&string=quick+brown+fox&string%202=quick%20brown%20fox";
		Variables variables = BasicVariables.fromFormEncoded(browser, formEncoded, Browser.UTF_8);
		assertEquals("6", variables.get("number"));
		
		assertEquals("bond", variables.get("word"));

		assertEquals("quick brown fox", variables.get("string"));

		assertEquals("quick brown fox", variables.get("string 2"));
	}

	@Test
	public void testFromHashtable() {
		fail("Not yet implemented");
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testContainsKey() {
		fail("Not yet implemented");
	}

}
