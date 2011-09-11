package net.microscraper.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class MapUtilsTest {
	private Decoder decoder;
	
	@Before
	public void setUp() throws Exception {
		decoder = new JavaNetDecoder(Decoder.UTF_8);
	}

	@Test
	public void testFromFormEncoded() throws Exception {
		String formEncodedData = "number=6&word=bond&string=quick+brown+fox&string%202=quick%20brown%20fox";
		
		Map<String ,String> hashtable = MapUtils.fromFormEncoded(decoder, formEncodedData);
		assertEquals("6", hashtable.get("number"));
		assertEquals("bond", hashtable.get("word"));
		assertEquals("quick brown fox", hashtable.get("string"));
		assertEquals("quick brown fox", hashtable.get("string 2"));
	}
}
