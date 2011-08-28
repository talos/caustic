package net.microscraper.http;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Hashtable;

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
	
	@Test
	public void testTimeout() throws Exception {
		requester.setTimeout(1);
		HttpResponse response = requester.head("http://www.lkjsdosfisjdfijosidjfsd.com", new Hashtable());
	}
	
	@Test
	public void testHeadGoogleHeaders() throws Exception {
		HttpResponse response = requester.head("http://www.google.com", new Hashtable() {});
		
	}
	
	@Test
	public void testGetGoogleContent() throws Exception {
		HttpResponse response = requester.get("http://www.google.com", new Hashtable() {});
		assertTrue(response.isSuccess());
		InputStreamReader contentStream = response.getContentStream();
		assertTrue(getString(contentStream).contains("google"));
	}

}
