package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResponseHeadersTest {
	
	private final Constructor<ResponseHeaders> constructor;
	
	public ResponseHeadersTest(Class<ResponseHeaders> klass)
			throws Exception {
		this.constructor = klass.getConstructor(Map.class);
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{ JavaNetResponseHeaders.class  }
		});
	}
	
	@Test
	public void testGetHeaderNamesIsZeroLengthOnEmpty() throws Exception {
		ResponseHeaders empty = constructor.newInstance(Collections.emptyMap());
		assertEquals(0, empty.getHeaderNames().length);
	}

	@Test
	public void testGetHeaderValuesForNonexistentHeaderIsNull() throws Exception {
		ResponseHeaders empty = constructor.newInstance(Collections.emptyMap());
		assertNull(empty.getHeaderValues(randomString()));
	}

	@Test
	public void testHeaderNames() throws Exception {
		String headerName = randomString();
		String headerValue = randomString();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put(headerName, Arrays.asList(headerValue));
		
		ResponseHeaders headers = constructor.newInstance(map);
		
		assertEquals(1, headers.getHeaderNames().length);
		assertEquals(headerName, headers.getHeaderNames()[0]);
	}
	
	@Test
	public void testHeaderGet() throws Exception {
		String headerName = randomString();
		String headerValue = randomString();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		map.put(headerName, Arrays.asList(headerValue));
		
		ResponseHeaders headers = constructor.newInstance(map);
		
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
		
		ResponseHeaders headers = constructor.newInstance(map);
		
		assertEquals(2, headers.getHeaderValues(headerName).length);
		assertEquals(headerValue, headers.getHeaderValues(headerName)[0]);
		assertEquals(headerValue2, headers.getHeaderValues(headerName)[1]);
	}
}
