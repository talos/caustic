package net.microscraper.http;

import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class JavaNetResponseHeadersTest extends ResponseHeadersTest {

	@Override
	protected ResponseHeaders getEmptyHeaders() throws Exception {
		return new JavaNetResponseHeaders(new HashMap<String, List<String>>());
	}

	@Test
	public void testHeaderNames() throws Exception {
		String headerName = randomString();
		String headerValue = randomString();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put(headerName, Arrays.asList(headerValue));
		
		ResponseHeaders headers = new JavaNetResponseHeaders(map);
		
		assertEquals(1, headers.getHeaderNames().length);
		assertEquals(headerName, headers.getHeaderNames()[0]);
	}
	
	@Test
	public void testHeaderGet() throws Exception {
		String headerName = randomString();
		String headerValue = randomString();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put(headerName, Arrays.asList(headerValue));
		
		ResponseHeaders headers = new JavaNetResponseHeaders(map);
		
		assertEquals(1, headers.getHeaderValues(headerName).length);
		assertEquals(headerValue, headers.getHeaderValues(headerName)[0]);
	}
	

	@Test
	public void testHeaderGetMultiple() throws Exception {
		String headerName = randomString();
		String headerValue = randomString();
		String headerValue2 = randomString();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put(headerName, Arrays.asList(headerValue, headerValue2));
		
		ResponseHeaders headers = new JavaNetResponseHeaders(map);
		
		assertEquals(2, headers.getHeaderValues(headerName).length);
		assertEquals(headerValue, headers.getHeaderValues(headerName)[0]);
		assertEquals(headerValue2, headers.getHeaderValues(headerName)[1]);
	}
}
