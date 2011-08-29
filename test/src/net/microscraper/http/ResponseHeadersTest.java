package net.microscraper.http;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

public abstract class ResponseHeadersTest {
	
	@Before
	public void setUp() throws Exception {
	}

	protected abstract ResponseHeaders getEmptyHeaders() throws Exception;
	
	@Test
	public void testGetHeaderNamesIsZeroLengthOnEmpty() throws Exception {
		assertEquals(0, getEmptyHeaders().getHeaderNames().length);
	}

	@Test
	public void testGetHeaderValuesForNonexistentHeaderIsNull() throws Exception {
		assertNull(getEmptyHeaders().getHeaderValues(randomString()));
	}

}
