package net.microscraper.util;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Enumeration;
import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Database;

import org.junit.Before;
import org.junit.Test;

public class VariablesTest {
	
	private @Mocked Database database;
	private Variables variables;
	
	private int firstId;
	
	@Before
	public void setUp() throws Exception {
		firstId = randomInt();
		new NonStrictExpectations() {{
			database.getFirstId(); result = firstId;
		}};
	}
	
	@Test
	public void testFromHashtable() throws Exception {
		int tableLength = 10;
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		
		for(int i = 0 ; i < tableLength ; i ++) {
			hashtable.put(randomString(), randomString());
		}
		
		variables = Variables.fromHashtable(database, hashtable);
		
		Enumeration<String> e = hashtable.keys();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = (String) hashtable.get(key);
			assertTrue(variables.containsKey(key));
			assertEquals(value, variables.get(key));
		}
	}
	
	@Test
	public void testSingleResultsPropagateThroughSaves() throws Exception {
		String childKey = randomString();
		String childValue = randomString();
		String grandchildKey = randomString();
		String grandchildValue = randomString();
		
		variables = Variables.empty(database);
		
		variables.save(childKey, childValue);
		variables.save(grandchildKey, grandchildValue);
		assertTrue(variables.containsKey(childKey));
		assertTrue(variables.containsKey(grandchildKey));
		assertEquals(childValue, variables.get(childKey));
		assertEquals(grandchildValue, variables.get(grandchildKey));
	}

	@Test
	public void testMultiSavesDontPropagate() throws Exception {
		String childKey = randomString();		
		variables = Variables.empty(database);
		
		Variables child = variables.saveAndBranch(0, childKey, randomString());
		
		assertFalse(variables.containsKey(childKey));
		assertTrue(child.containsKey(childKey));
	}
	
	@Test
	public void testLatestValueOverwrites() throws Exception {
		String key = randomString();
		String childValue = randomString();
		String grandchildValue = randomString();
		
		variables = Variables.empty(database);
		
		variables.save(key, childValue);
		variables.save(key, grandchildValue);
		assertNotSame(childValue, variables.get(key));
		assertEquals(grandchildValue, variables.get(key));
	}
	

	@Test
	public void testResultsPropagateUntilMultiChildren() throws Exception {
		String childKey = randomString();		
		String grandchildKey = randomString();
		variables = Variables.empty(database);
		
		variables.save(childKey, randomString());
		Variables grandchild = variables.saveAndBranch(0, grandchildKey, randomString());
		
		assertTrue(variables.containsKey(childKey));
		assertFalse(variables.containsKey(grandchildKey));
		
		assertTrue(variables.containsKey(childKey));
		assertFalse(variables.containsKey(grandchildKey));
		
		assertTrue("Grandchild doesn't contain its parent key.", grandchild.containsKey(childKey));
		assertTrue("Grandchild doesn't contain its own key.", grandchild.containsKey(grandchildKey));
	}

	@Test
	public void testCousinageThroughSingleResults() throws Exception {
		String siblingKey1 = randomString();
		String siblingKey2 = randomString();
		
		String cousinKey1 = randomString();
		String cousinKey2 = randomString();
		
		variables = Variables.empty(database);
		
		variables.save(siblingKey1, randomString());
		variables.save(siblingKey2, randomString());
		
		variables.save(cousinKey1, randomString());
		Variables cousinFromSibling2 = variables.saveAndBranch(0, cousinKey2, randomString());
		
		assertTrue("Variables doesn't contain key from its grandparent's grandchild descended through single results", cousinFromSibling2.containsKey(siblingKey1));
		
		assertFalse("Variables contains key from its grandparent's grandchild descended through multiple results", variables.containsKey(cousinKey2));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNonStringKeyInHashtableIsIllegal() throws Exception {
		Hashtable<Object, String> hashtable = new Hashtable<Object, String>();
		hashtable.put(new Object(), randomString());
		Variables.fromHashtable(database, hashtable);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonStringValueInHashtableIsIllegal() throws Exception {
		Hashtable<String, Object> hashtable = new Hashtable<String, Object>();
		hashtable.put(randomString(), new Object());
		Variables.fromHashtable(database, hashtable);
	}

	@Test()
	public void testInitialCommitsToDatabase() throws Exception {
		final String key = randomString();
		final String value = randomString();
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		hashtable.put(key, value);
		new Expectations() {{
			database.store(firstId, key, value);
		}};
		Variables.fromHashtable(database, hashtable);
	}
	
	@Test()
	public void testSaveCommitsToDatabase() throws Exception {
		final String key = randomString();
		final String value = randomString();
		new Expectations() {{
			database.store(firstId, key, value);
		}};
		Variables.empty(database).save(key, value);
	}
}
