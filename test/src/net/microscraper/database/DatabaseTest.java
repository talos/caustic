package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public abstract class DatabaseTest {
	private Variables variables;
	
	protected abstract Database getDatabase() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		variables = getDatabase().open();
	}

	@Test
	public void testStoreOneToOneIntString() throws Exception {
		variables.storeOneToOne("bleh");
		assertNull(variables.get("bleh"));
	}

	@Test
	public void testStoreOneToOneIntStringString() throws Exception {
		variables.storeOneToOne("bleh", "meh");
		assertEquals("meh", variables.get("bleh"));
	}

	@Test
	public void testStoreOneToManyIntString() throws Exception {
		Variables branch = variables.storeOneToMany("bleh");
		assertNull(variables.get("bleh"));
		assertNull(branch.get("bleh"));
	}

	@Test
	public void testStoreOneToManyIntStringString() throws Exception {
		Variables branch = variables.storeOneToMany("bleh", "meh");
		assertNull(variables.get("bleh"));
		assertEquals("meh", branch.get("bleh"));
	}

	@Test
	public void testSingleResultsPropagateThroughSaves() throws Exception {
		String childKey = randomString();
		String childValue = randomString();
		String grandchildKey = randomString();
		String grandchildValue = randomString();
				
		Variables child = variables.storeOneToOne(childKey, childValue);
		Variables grandchild = variables.storeOneToOne(grandchildKey, grandchildValue);
		assertEquals(childValue, variables.get(childKey));
		assertEquals(childValue, child.get(childKey));
		assertEquals(childValue, grandchild.get(childKey));
		assertEquals(grandchildValue, variables.get(grandchildKey));
		assertEquals(grandchildValue, child.get(grandchildKey));
		assertEquals(grandchildValue, grandchild.get(grandchildKey));
	}

	@Test
	public void testMultiSavesDontPropagate() throws Exception {
		String childKey = randomString();		
		
		Variables child = variables.storeOneToMany(childKey, randomString());
		
		assertNull(variables.get(childKey));
		assertNotNull(child.get(childKey));
	}
	
	@Test
	public void testLatestValueOverwrites() throws Exception {
		String key = randomString();
		String childValue = randomString();
		String grandchildValue = randomString();
		
		Variables child = variables.storeOneToOne(key, childValue);
		Variables grandchild = variables.storeOneToOne(key, grandchildValue);
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
		
		Variables child = variables.storeOneToOne(childKey, randomString());
		Variables grandchild = variables.storeOneToMany(grandchildKey, randomString());
		
		assertNotNull(variables.get(childKey));
		assertNull(variables.get(grandchildKey));
		
		assertNotNull(child.get(childKey));
		assertNull(child.get(grandchildKey));
		
		assertNotNull("Grandchild doesn't contain its parent key.", grandchild.get(childKey));
		assertNotNull("Grandchild doesn't contain its own key.", grandchild.get(grandchildKey));
	}

	@Test
	public void testCousinageThroughSingleResults() throws Exception {
		String siblingKey1 = randomString();
		String siblingKey2 = randomString();
		
		String cousinKey1 = randomString();
		String cousinKey2 = randomString();
				
		Variables sibling1 = variables.storeOneToOne(siblingKey1, randomString());
		Variables sibling2 = variables.storeOneToOne(siblingKey2, randomString());
		
		Variables cousinFromSibling1 = sibling1.storeOneToOne(cousinKey1, randomString());
		Variables cousinFromSibling2 = sibling2.storeOneToMany(cousinKey2, randomString());
		
		assertNotNull("Variables doesn't contain key from its grandparent's grandchild descended through single results",
				cousinFromSibling2.get(siblingKey1));
		assertNull("Variables contains key from its grandparent's grandchild descended through multiple results",
				cousinFromSibling1.get(cousinKey2));
	}

}
