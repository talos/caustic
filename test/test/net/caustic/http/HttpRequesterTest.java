package net.caustic.http;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import net.caustic.http.HttpBrowser;
import net.caustic.http.HttpRequestException;
import net.caustic.http.HttpRequester;
import net.caustic.http.HttpResponse;
import net.caustic.http.JavaNetHttpRequester;
import net.caustic.http.ResponseHeaders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class HttpRequesterTest {
	
	private final Class<HttpRequester> klass;
	private HttpRequester requester;
	
	public HttpRequesterTest(Class<HttpRequester> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetHttpRequester.class  }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		requester = klass.newInstance();
	}
	
	private String getString(InputStreamReader inputStream) throws IOException {
		StringBuffer strBuf = new StringBuffer();
		char[] buf = new char[512];
		int readBytes;
		while((readBytes = inputStream.read(buf)) != -1) {
			strBuf.append(buf, 0, readBytes);
		}
		return strBuf.toString();
	}
	
	@Test(expected = HttpRequestException.class)
	public void testBadURLThrowsHttpRequestException() throws Exception {
		HttpResponse response = requester.head(randomString(), new Hashtable());
	}
	
	@Test(expected = HttpRequestException.class)
	public void testTimeoutThrowsHttpRequestException() throws Exception {
		requester.setTimeout(10);
		requester.get("http://www.nytimes.com", new Hashtable());
	}
	
	@Test
	public void testHeadGoogleHeaders() throws Exception {
		 requester.head("http://www.google.com", new Hashtable() {});
		
	}
	
	@Test
	public void testGetGoogleContent() throws Exception {
		HttpResponse response = requester.get("http://www.google.com", new Hashtable());
		assertTrue(response.isSuccess());
		InputStreamReader contentStream = response.getContentStream();
		assertTrue(getString(contentStream).contains("google"));
	}
	
	@Test
	public void testGetGoogleContentQueryWithoutHeadersFails() throws Exception {
		HttpResponse response = requester.get("http://www.google.com/search?q=bleh", new Hashtable());
		assertFalse(response.isSuccess());
	}

	@Test
	public void testGetGoogleContentQueryWithHeaders() throws Exception {
		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put(HttpBrowser.ACCEPT_HEADER_NAME, HttpBrowser.ACCEPT_HEADER_DEFAULT_VALUE);
		headers.put(HttpBrowser.ACCEPT_LANGUAGE_HEADER_NAME, HttpBrowser.ACCEPT_LANGUAGE_HEADER_DEFAULT_VALUE);
		headers.put(HttpBrowser.REFERER_HEADER_NAME, "http://www.google.com/search?q=bleh");
		headers.put(HttpBrowser.USER_AGENT_HEADER_NAME, HttpBrowser.USER_AGENT_HEADER_DEFAULT_VALUE);
		HttpResponse response = requester.get("http://www.google.com/search?q=bleh", headers);
		
		assertTrue(response.isSuccess());
		InputStreamReader contentStream = response.getContentStream();
		assertTrue(getString(contentStream).contains("bleh"));
	}
	
	@Test
	public void testPopulatesHeaders() throws Exception {
		HttpResponse response = requester.get("http://www.google.com", new Hashtable() {});
		assertTrue(response.isSuccess());
		ResponseHeaders responseHeaders = response.getResponseHeaders();
		assertTrue(responseHeaders.getHeaderNames().length > 1);
	}
	
	@Test
	public void testPost() throws Exception {
		
		String url = "http://a836-acris.nyc.gov/Scripts/DocSearch.dll/BBLResult";
		String encodedPostData = "hid_borough=3&hid_block=1772&hid_doctype=&hid_lot=74&hid_SearchType=BBL";
		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put("Cookie", "JUMPPAGE=YES");

		HttpResponse response = requester.post(url, headers, encodedPostData);
		assertTrue(response.isSuccess());
		//ResponseHeaders responseHeaders = response.getResponseHeaders();
		assertTrue(getString(response.getContentStream()).contains("PULASKI"));

	}
}
