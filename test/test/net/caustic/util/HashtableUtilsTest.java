package net.caustic.util;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.caustic.http.HttpBrowser;
import net.caustic.util.HashtableUtils;

import org.junit.Before;
import org.junit.Test;

public class HashtableUtilsTest {	


	@Test
	public void testCombineEmptiesAreEmpty() throws Exception {
		Hashtable<String, String> source1 = new Hashtable<String, String>();
		Hashtable<String, String> source2 = new Hashtable<String, String>();
		
		Hashtable<String, String> combined = HashtableUtils.combine(new Hashtable[] { source1, source2 });
		
		assertEquals(0, combined.size());
	}
	

	@Test
	public void testCombineDifferentKeys() throws Exception {
		int tableSize = 10;
		
		Hashtable<String, String> source1 = new Hashtable<String, String>();
		Hashtable<String, String> source2 = new Hashtable<String, String>();
		Hashtable<String, String> source3 = new Hashtable<String, String>();
		
		for(int i = 0 ; i < tableSize ; i ++) {
			source1.put(randomString(3), randomString());
			source2.put(randomString(4), randomString());
			source3.put(randomString(5), randomString());
		}
		
		Hashtable<String, String> combined = HashtableUtils.combine(new Hashtable[] { source1, source2, source3 });
		
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
		
		Hashtable<String, String> source1 = new Hashtable<String, String>();
		Hashtable<String, String> source2 = new Hashtable<String, String>();
		Hashtable<String, String> source3 = new Hashtable<String, String>();
		
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
		
		Hashtable<String, String> combined = HashtableUtils.combine(new Hashtable[] { source1, source2, source3 });
		
		assertTrue(combined.containsKey(sharedKey));
		assertEquals(value2, combined.get(sharedKey));
		assertNotSame(value1, combined.get(sharedKey));
	}

	/**
	 * Turn an array of {@link Hashtable}s into a single {@link Hashtable}.  Keys from
	 * later elements of <code>hashtables</code> will overwrite keys from earlier elements:
	 * <p>
	 * <code>
	 * {"foo"   : "bar",<br>
	 *  "roses" : "red"}<br>
	 *  +<br>
	 * {"foo"     : "bazzz",<br>
	 *  "violets" : "blue" }</code>
	 * <p>turns into</p>
	 * <code>
	 * {"foo"     : "bazzz",<br>
	 *  "roses"   : "red",<br>
	 *  "violets" : "blue"}
	 * <p>
	 * @param hashtables An array of {@link Hashtable}s.
	 * @return A single {@link Hashtable}.
	 */
	@Test
	public void testLaterKeyOverwritesEarlierKeyExample() throws Exception {
		Hashtable<String, String> table1 = new Hashtable<String, String>();
		Hashtable<String, String> table2 = new Hashtable<String, String>();
		
		table1.put("foo", "bar");
		table1.put("roses", "red");
		
		table2.put("foo", "bazzz");
		table2.put("violets", "blue");
		
		Hashtable<String, String> combined = HashtableUtils.combine(new Hashtable[] { table1, table2 } );
		
		assertEquals("bazzz", combined.get("foo"));
		assertEquals("red", combined.get("roses"));
		assertEquals("blue", combined.get("violets"));
	}
	
	@Test
	public void testEmptyIsEmpty() {
		Hashtable<String, String> empty = HashtableUtils.EMPTY;
		assertTrue(empty.isEmpty());
		assertEquals(0, empty.size());
	}
	
	@Test
	public void testEmptyEqualsEmpty() {
		Hashtable<String, String> empty1 = HashtableUtils.EMPTY;
		Hashtable<String, String> empty2 = HashtableUtils.EMPTY;
		assertTrue(empty1.equals(empty2));
	}
	
	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyCantBeModifiedWithPut() {
		Hashtable<String, String> empty = HashtableUtils.EMPTY;
		empty.put(randomString(), randomString());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testEmptyCantBeModifiedWithPutAll() {
		Hashtable<String, String> empty = HashtableUtils.EMPTY;
		Map<String, String> map = new HashMap<String, String>();
		map.put(randomString(), randomString());
		map.put(randomString(), randomString());
		empty.putAll(map);
	}
	
	@Test
	public void testEmptyToFormEncoded() {
		assertEquals("", HashtableUtils.toFormEncoded(HashtableUtils.EMPTY));
	}
	
	@Test
	public void testToFormEncoded() {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("roses", "red");
		table.put("violets", "blue");
		String formEncoded = HashtableUtils.toFormEncoded(table);
		assertTrue(formEncoded.equals("roses=red&violets=blue") || formEncoded.equals("violets=blue&roses=red"));
	}
	
	@Test
	public void testToFormEncodedDoesNotEncode() {
		// encoding should be done before
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("roses are red", "violets are blue");
		assertEquals("roses are red=violets are blue", HashtableUtils.toFormEncoded(table));
	}
}
