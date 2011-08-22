package net.microscraper.util;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Hashtable;

import net.microscraper.client.Browser;

import org.junit.Before;
import org.junit.Test;

public class HashtableUtilsTest {	
	private Decoder decoder = new Decoder() {
		@Override
		public String decode(String stringToDecode, String encoding)
				throws UnsupportedEncodingException {
			return URLDecoder.decode(stringToDecode, encoding);
		}
	};
	
	@Before
	public void setUp() {
		//hashtable = new Hashtable();
	}
	
	@Test
	public void testFromFormEncoded() throws Exception {
		String formEncodedData = "number=6&word=bond&string=quick+brown+fox&string%202=quick%20brown%20fox";
		
		Hashtable hashtable = HashtableUtils.fromFormEncoded(decoder, formEncodedData, Browser.UTF_8);
		assertEquals("6", hashtable.get("number"));
		assertEquals("bond", hashtable.get("word"));
		assertEquals("quick brown fox", hashtable.get("string"));
		assertEquals("quick brown fox", hashtable.get("string 2"));
	}

	@Test
	public void testCombineEmptiesAreEmpty() throws Exception {
		Hashtable source1 = new Hashtable();
		Hashtable source2 = new Hashtable();
		
		Hashtable combined = HashtableUtils.combine(new Hashtable[] { source1, source2 });
		
		assertEquals(0, combined.size());
	}
	

	@Test
	public void testCombineDifferentKeys() throws Exception {
		int tableSize = 10;
		
		Hashtable source1 = new Hashtable();
		Hashtable source2 = new Hashtable();
		Hashtable source3 = new Hashtable();
		
		for(int i = 0 ; i < tableSize ; i ++) {
			source1.put(randomString(3), randomString());
			source2.put(randomString(4), randomString());
			source3.put(randomString(5), randomString());
		}
		
		Hashtable combined = HashtableUtils.combine(new Hashtable[] { source1, source2, source3 });
		
		assertEquals(tableSize * 3, combined.size());
		assertTrue(combined.keySet().containsAll(source1.keySet()));
		assertTrue(combined.keySet().containsAll(source2.keySet()));
		assertTrue(combined.keySet().containsAll(source3.keySet()));
		assertTrue(combined.values().containsAll(source1.values()));
		assertTrue(combined.values().containsAll(source2.values()));
		assertTrue(combined.values().containsAll(source3.values()));
	}
	

	@Test
	public void testLaterKeyOverwritesEarlierKey() throws Exception {
		int tableSize = 10;
		
		Hashtable source1 = new Hashtable();
		Hashtable source2 = new Hashtable();
		Hashtable source3 = new Hashtable();
		
		for(int i = 0 ; i < tableSize ; i ++) {
			source1.put(randomString(3), randomString());
			source2.put(randomString(4), randomString());
			source3.put(randomString(5), randomString());
		}
		
		String sharedKey = randomString(6);
		String value1 = randomString(3);
		String value2 = randomString(4);
		
		source1.put(sharedKey, value1);
		source2.put(sharedKey, value2);
		
		Hashtable combined = HashtableUtils.combine(new Hashtable[] { source1, source2, source3 });
		
		assertTrue(combined.containsKey(sharedKey));
		assertEquals(value2, combined.get(sharedKey));
		assertNotSame(value1, combined.get(sharedKey));
	}
}
