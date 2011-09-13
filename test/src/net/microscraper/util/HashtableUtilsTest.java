package net.microscraper.util;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.microscraper.http.HttpBrowser;

import org.junit.Before;
import org.junit.Test;

public class HashtableUtilsTest {	


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
	
	@Test
	public void testEmptyIsEmpty() {
		Hashtable empty = HashtableUtils.EMPTY;
		assertTrue(empty.isEmpty());
		assertEquals(0, empty.size());
	}
	
	@Test
	public void testEmptyEqualsEmpty() {
		Hashtable empty1 = HashtableUtils.EMPTY;
		Hashtable empty2 = HashtableUtils.EMPTY;
		assertTrue(empty1.equals(empty2));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyCantBeModifiedWithPut() {
		Hashtable empty = HashtableUtils.EMPTY;
		empty.put(randomString(), randomString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyCantBeModifiedWithPutAll() {
		Hashtable empty = HashtableUtils.EMPTY;
		Map<String, String> map = new HashMap<String, String>();
		map.put(randomString(), randomString());
		map.put(randomString(), randomString());
		empty.putAll(map);
	}
}
