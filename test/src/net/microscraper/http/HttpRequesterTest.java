package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Hashtable;

import net.microscraper.util.StringUtils;

import org.junit.Before;
import org.junit.Test;

public abstract class HttpRequesterTest {
	
	private HttpRequester requester;
	
	protected abstract HttpRequester getHttpRequester() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		requester = getHttpRequester();
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
	
	@Test(expected = IOException.class)
	public void testBadURLThrowsIOException() throws Exception {
		HttpResponse response = requester.head(randomString(), new Hashtable());
	}
	
	@Test(expected = IOException.class)
	public void testTimeoutThrowsIOException() throws Exception {
		requester.setTimeout(10);
		requester.get("http://www.nytimes.com", new Hashtable());
	}
	
	@Test
	public void testHeadGoogleHeaders() throws Exception {
		 requester.head("http://www.google.com", new Hashtable() {});
		
	}
	
	@Test
	public void testGetGoogleContent() throws Exception {
		HttpResponse response = requester.get("http://www.google.com", new Hashtable() {});
		assertTrue(response.isSuccess());
		InputStreamReader contentStream = response.getContentStream();
		assertTrue(getString(contentStream).contains("google"));
	}
	
	@Test
	public void testPopulatesHeaders() throws Exception {
		HttpResponse response = requester.get("http://www.google.com", new Hashtable() {});
		assertTrue(response.isSuccess());
		ResponseHeaders responseHeaders = response.getResponseHeaders();
		assertTrue(responseHeaders.getHeaderNames().length > 1);
	}
}
