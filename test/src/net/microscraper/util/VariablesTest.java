package net.microscraper.util;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Enumeration;
import java.util.Hashtable;

import mockit.Mocked;
import net.microscraper.database.Database;

import org.junit.Test;

public class VariablesTest {
	
	private @Mocked Database database;
	private Variables variables;
	
	@Test
	public void testFromHashtable() throws Exception {
		int tableLength = 10;
		Hashtable hashtable = new Hashtable();
		
		for(int i = 0 ; i < tableLength ; i ++) {
			hashtable.put(randomString(), randomString());
		}
		
		variables = Variables.fromHashtable(database, hashtable);
		
		Enumeration e = hashtable.keys();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = (String) hashtable.get(key);
			assertTrue(variables.containsKey(key));
			assertEquals(value, variables.get(key));
		}
	}

	@Test
	public void testBranchCreatesAsManyVariablesAsResultValues() throws Exception {
		String[] resultValues1 = new String[randomInt()];
		String[] resultValues2 = new String[randomInt()];
		for(int i = 0 ; i < resultValues1.length ; i++) {
			resultValues1[i] = randomString();
		}
		for(int i = 0 ; i < resultValues2.length ; i++) {
			resultValues2[i] = randomString();
		}
		variables = Variables.empty(database);
		
		assertEquals(resultValues1.length, Variables.multiBranch(variables, randomString(), resultValues1, false, false).length);
		assertEquals(resultValues2.length, Variables.multiBranch(variables, randomString(), resultValues2, false, false).length);
	}
	
	@Test
	public void testSingleResultsPropagateThroughBranches() throws Exception {
		String childKey = randomString();
		String childValue = randomString();
		String grandchildKey = randomString();
		String grandchildValue = randomString();
		
		variables = Variables.empty(database);
		
		Variables child = Variables.singleBranch(variables, childKey, childValue, true, false);
		Variables grandchild = Variables.singleBranch(child, grandchildKey, grandchildValue, true, false);
		
		assertTrue(variables.containsKey(childKey));
		assertTrue(child.containsKey(childKey));
		assertTrue(grandchild.containsKey(childKey));

		assertTrue(variables.containsKey(grandchildKey));
		assertTrue(child.containsKey(grandchildKey));
		assertTrue(grandchild.containsKey(grandchildKey));
		
		assertEquals(childValue, variables.get(childKey));
		assertEquals(childValue, child.get(childKey));
		assertEquals(childValue, grandchild.get(childKey));
		
		assertEquals(grandchildValue, variables.get(grandchildKey));
		assertEquals(grandchildValue, child.get(grandchildKey));
		assertEquals(grandchildValue, grandchild.get(grandchildKey));
	}

	@Test
	public void testMultiResultsDontPropagate() throws Exception {
		String childKey = randomString();		
		variables = Variables.empty(database);
		
		Variables[] children = Variables.multiBranch(variables, childKey, new String[] { randomString(), randomString() }, true, false);
		
		assertFalse(variables.containsKey(childKey));
		assertTrue(children[0].containsKey(childKey));
		assertTrue(children[1].containsKey(childKey));
	}
	
	@Test
	public void testLatestValueOverwrites() throws Exception {
		String key = randomString();
		String childValue = randomString();
		String grandchildValue = randomString();
		
		variables = Variables.empty(database);
		
		Variables child = Variables.singleBranch(variables, key, childValue, true, false);
		Variables grandchild = Variables.singleBranch(variables, key, grandchildValue, true, false);
		
		assertNotSame(childValue, variables.get(key));
		assertNotSame(childValue, child.get(key));
		assertNotSame(childValue, grandchild.get(key));

		assertEquals(grandchildValue, variables.get(key));
		assertEquals(grandchildValue, child.get(key));
		assertEquals(grandchildValue, grandchild.get(key));
	}
	

	@Test
	public void testResultsPropagateUntilMultiChildren() throws Exception {
		String childKey = randomString();		
		String grandchildKey = randomString();
		variables = Variables.empty(database);
		
		Variables child = Variables.singleBranch(variables, childKey, randomString(), true, false);
		Variables[] grandchildren = Variables.multiBranch(child, grandchildKey, new String[] { randomString(), randomString() }, true, false);
		
		assertTrue(variables.containsKey(childKey));
		assertFalse(variables.containsKey(grandchildKey));
		
		assertTrue(child.containsKey(childKey));
		assertFalse(child.containsKey(grandchildKey));
		
		assertTrue("Grandchild doesn't contain its parent key.", grandchildren[0].containsKey(childKey));
		assertTrue("Grandchild doesn't contain its own key.", grandchildren[0].containsKey(grandchildKey));

		assertTrue("Grandchild doesn't contain its parent key.", grandchildren[1].containsKey(childKey));
		assertTrue("Grandchild doesn't contain its own key.", grandchildren[1].containsKey(grandchildKey));
	}

	@Test
	public void testCousinageThroughSingleResults() throws Exception {
		String siblingKey1 = randomString();
		String siblingKey2 = randomString();
		
		String cousinKey1 = randomString();
		String cousinKey2 = randomString();
		
		variables = Variables.empty(database);
		
		Variables sibling1 = Variables.singleBranch(variables, siblingKey1, randomString(), true, false);
		Variables sibling2 = Variables.singleBranch(variables, siblingKey2, randomString(), true, false);
		
		Variables cousinFromSibling1 = Variables.singleBranch(sibling1, cousinKey1, randomString(), true, false);
		Variables[] cousinsFromSibling2 = Variables.multiBranch(sibling2, cousinKey2, new String[] { randomString(), randomString() }, true, false);
		
		assertTrue("Variables doesn't contain key from its grandparent's grandchild descended through single results", cousinsFromSibling2[0].containsKey(siblingKey1));
		assertTrue("Variables doesn't contain key from its grandparent's grandchild descended through single results", cousinsFromSibling2[1].containsKey(siblingKey1));
		
		assertFalse("Variables contains key from its grandparent's grandchild descended through multiple results", cousinFromSibling1.containsKey(cousinKey2));
	}
	
}
