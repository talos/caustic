package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public abstract class DatabaseTest {

	private int id; 
	private Database db;
	
	protected abstract Database getDatabase() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		db = getDatabase();
		id = db.getFreshSourceId();
	}

	@Test
	public void testStoreOneToOneWithoutValueStoresNothing() throws Exception {
		db.storeOneToOne(id, "bleh");
		assertNull(db.get(id, "bleh"));
	}

	@Test
	public void testStoreOneToOneWithValueStoresValue() throws Exception {
		db.storeOneToOne(id, "bleh", "meh");
		assertEquals("meh", db.get(id, "bleh"));
	}

	@Test
	public void testStoreOneToManyWithoutValueStoresNothing() throws Exception {
		int branch = db.storeOneToMany(id, "bleh");
		assertNull(db.get(id, "bleh"));
		assertNull(db.get(branch, "bleh"));
	}

	@Test
	public void testStoreOneToManyWithValuePartiallyAccessible() throws Exception {
		int branch = db.storeOneToMany(id, "bleh", "meh");
		assertNull(db.get(id, "bleh"));
		assertEquals("meh", db.get(branch, "bleh"));
	}

	@Test
	public void testSingleResultsPropagateThroughSaves() throws Exception {
		String childKey = randomString();
		String childValue = randomString();
		String grandchildKey = randomString();
		String grandchildValue = randomString();
				
		int child = db.storeOneToOne(id, childKey, childValue);
		int grandchild = db.storeOneToOne(id, grandchildKey, grandchildValue);
		assertEquals(childValue, db.get(id, childKey));
		assertEquals(childValue, db.get(child, childKey));
		assertEquals(childValue, db.get(grandchild,childKey));
		assertEquals(grandchildValue, db.get(id,grandchildKey));
		assertEquals(grandchildValue, db.get(child,grandchildKey));
		assertEquals(grandchildValue, db.get(grandchild,grandchildKey));
	}

	@Test
	public void testMultiSavesDontPropagate() throws Exception {
		String childKey = randomString();		
		
		int child = db.storeOneToMany(id, childKey, randomString());
		
		assertNull(db.get(id, childKey));
		assertNotNull(db.get(child, childKey));
	}
	
	@Test
	public void testLatestValueOverwrites() throws Exception {
		String key = randomString();
		String childValue = randomString();
		String grandchildValue = randomString();
		
		int child = db.storeOneToOne(id, key, childValue);
		int grandchild = db.storeOneToOne(child, key, grandchildValue);
		assertNotSame(childValue, db.get(id,key));
		assertNotSame(childValue, db.get(child,key));
		assertNotSame(childValue, db.get(grandchild,key));
		assertEquals(grandchildValue, db.get(id,key));
		assertEquals(grandchildValue, db.get(child,key));
		assertEquals(grandchildValue, db.get(grandchild,key));
	}
	

	@Test
	public void testResultsPropagateUntilMultiChildren() throws Exception {
		String childKey = randomString();		
		String grandchildKey = randomString();
		
		int child = db.storeOneToOne(id, childKey, randomString());
		int grandchild = db.storeOneToMany(child, grandchildKey, randomString());
		
		assertNotNull(db.get(id,childKey));
		assertNull(db.get(id,grandchildKey));
		
		assertNotNull(db.get(child,childKey));
		assertNull(db.get(child,grandchildKey));
		
		assertNotNull("Grandchild doesn't contain its parent key.", db.get(grandchild,childKey));
		assertNotNull("Grandchild doesn't contain its own key.", db.get(grandchild,grandchildKey));
	}

	@Test
	public void testCousinageThroughSingleResults() throws Exception {
		String siblingKey1 = randomString();
		String siblingKey2 = randomString();
		
		String cousinKey1 = randomString();
		String cousinKey2 = randomString();
				
		int sibling1 = db.storeOneToOne(id,siblingKey1, randomString());
		int sibling2 = db.storeOneToOne(id,siblingKey2, randomString());
		
		int cousinFromSibling1 = db.storeOneToOne(sibling1,cousinKey1, randomString());
		int cousinFromSibling2 = db.storeOneToMany(sibling2,cousinKey2, randomString());
		
		assertNotNull("Variables doesn't contain key from its grandparent's grandchild descended through single results",
				db.get(cousinFromSibling2, siblingKey1));
		assertNull("Variables contains key from its grandparent's grandchild descended through multiple results",
				db.get(cousinFromSibling1, cousinKey2));
	}

}
