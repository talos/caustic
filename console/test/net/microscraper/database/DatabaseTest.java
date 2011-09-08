package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public abstract class DatabaseTest {

	protected Scope scope; 
	protected Database db;
	
	protected abstract Database getDatabase() throws Exception;
	
	@Before
	public void setUp() throws Exception {
		db = getDatabase();
		db.open();
		scope = db.getDefaultScope();
	}

	@Test
	public void testStoreOneToOneWithoutValueStoresNothing() throws Exception {
		db.storeOneToOne(scope, "bleh");
		assertNull(db.get(scope, "bleh"));
	}

	@Test
	public void testStoreOneToOneWithValueStoresValue() throws Exception {
		db.storeOneToOne(scope, "bleh", "meh");
		assertEquals("meh", db.get(scope, "bleh"));
	}

	@Test
	public void testStoreOneToManyWithoutValueStoresNothing() throws Exception {
		Scope branch = db.storeOneToMany(scope, "bleh");
		assertNull(db.get(scope, "bleh"));
		assertNull(db.get(branch, "bleh"));
	}
	
	@Test
	public void testStoreOneToManyWithValuePartiallyAccessible() throws Exception {
		Scope branch = db.storeOneToMany(scope, "bleh", "meh");
		assertNull("Main scope should not know that this key exists", db.get(scope, "bleh"));
		
		assertNotNull("Branch should know that this key exists", db.get(branch, "bleh"));
		assertEquals("Branch should retrieve this value.", "meh", db.get(branch, "bleh"));
	}

	@Test
	public void testSingleResultsPropagateThroughSaves() throws Exception {
		String childKey = randomString();
		String childValue = randomString();
		String grandchildKey = randomString();
		String grandchildValue = randomString();
				
		db.storeOneToOne(scope, childKey, childValue);
		db.storeOneToOne(scope, grandchildKey, grandchildValue);
		assertEquals(childValue, db.get(scope, childKey));
		assertEquals(grandchildValue, db.get(scope,grandchildKey));
	}

	@Test
	public void testMultiSavesDontPropagate() throws Exception {
		String childKey = randomString();		
		
		Scope child = db.storeOneToMany(scope, childKey, randomString());
		
		assertNull(db.get(scope, childKey));
		assertNotNull(db.get(child, childKey));
	}
	
	@Test
	public void testLatestValueOverwrites() throws Exception {
		String key = randomString();
		String childValue = randomString();
		String grandchildValue = randomString();
		
		db.storeOneToOne(scope, key, childValue);
		db.storeOneToOne(scope, key, grandchildValue);
		assertNotSame(childValue, db.get(scope,key));
		assertEquals(grandchildValue, db.get(scope,key));
	}
	

	@Test
	public void testResultsPropagateUntilMultiChildren() throws Exception {
		String childKey = randomString();		
		String grandchildKey = randomString();
		
		db.storeOneToOne(scope, childKey, randomString());
		Scope grandchild = db.storeOneToMany(scope, grandchildKey, randomString());
		
		assertNotNull(db.get(scope,childKey));
		assertNull(db.get(scope,grandchildKey));
		
		assertNotNull("Grandchild doesn't contain its parent key.", db.get(grandchild,childKey));
		assertNotNull("Grandchild doesn't contain its own key.", db.get(grandchild,grandchildKey));
	}

	@Test
	public void testCousinageThroughSingleResults() throws Exception {
		String siblingKey1 = randomString();
		String siblingKey2 = randomString();
		
		String cousinKey1 = randomString();
		String cousinKey2 = randomString();
				
		db.storeOneToOne(scope,siblingKey1, randomString());
		db.storeOneToOne(scope,siblingKey2, randomString());
		
		db.storeOneToOne(scope,cousinKey1, randomString());
		Scope cousinFromSibling2 = db.storeOneToMany(scope,cousinKey2, randomString());
		
		assertNotNull("Variables doesn't contain key from its grandparent's grandchild descended through single results",
				db.get(cousinFromSibling2, siblingKey1));
		assertNull("Variables contains key from its grandparent's grandchild descended through multiple results",
				db.get(scope, cousinKey2));
	}

}
