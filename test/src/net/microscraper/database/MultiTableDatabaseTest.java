package net.microscraper.database;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;
import static net.microscraper.database.MultiTableDatabase.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.database.UpdateableConnection;
import net.microscraper.database.Updateable;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.util.NameValuePair;

import org.junit.Before;
import org.junit.Test;

public class MultiTableDatabaseTest {

	@Mocked Updateable rootTable;
	@Mocked UpdateableConnection connection;
	@Tested MultiTableDatabase db;
	
	int firstId;
	
	@Before
	public void setUp() throws Exception {
		new Expectations() {{
			connection.getIOTable(ROOT_TABLE_NAME, withSameInstance(ROOT_TABLE_COLUMNS));
				result = rootTable;
				$ = "Should create root table on instantiation.";
		}};
		db = new MultiTableDatabase(connection);
		firstId = db.getFirstId();
	}
	
	@Test
	public void testCreatesRootTableOnInstantiation() { }
	
	@Test
	public void testFirstIdIsZero() throws Exception {
		assertEquals(0, firstId);
	}
	
	@Test
	public void testStoreNameValueUpdatesRootTable() throws Exception {
		final String name = randomString();
		final String value = randomString();
		new Expectations() {
			{
				rootTable.hasColumn(name); result = false;
				rootTable.addColumn(name); $ = "Should add name column to root table.";
				rootTable.update(ID_COLUMN_NAME, firstId, (Hashtable) any);
					forEachInvocation = new Object() {
						void validate(String columnName, int id, Hashtable map) {
							assertEquals("Should update name and value in root table.", 1, map.size());
							assertTrue(map.containsKey(name));
							assertEquals(value, map.get(name));
						}
					};
			}
		};
		
		db.store(firstId, name, value);
	}
	
	@Test
	public void testStoreIntNameValueCreatesTableWithNameAndValueColumn() throws Exception {
		final String name = randomString();
		final String value = randomString();
		new Expectations() {
			@Injectable Updateable resultTable;
			{
				connection.getIOTable(name, withSameInstance(RESULT_TABLE_COLUMNS)); result = resultTable;
				resultTable.insert((Hashtable) any);
					forEachInvocation = new Object() {
						void validate(Hashtable map) {
							//assertEquals("Should create blank entry in new table", 0, nvps.length);
							assertEquals(4, map.size());
							
							assertTrue(map.containsKey(SOURCE_TABLE_COLUMN));
							assertEquals(ROOT_TABLE_NAME, map.get(SOURCE_TABLE_COLUMN));

							assertTrue(map.containsKey(SOURCE_ID_COLUMN_NAME));
							assertEquals(Integer.toString(firstId), map.get(SOURCE_ID_COLUMN_NAME));
							
							assertTrue(map.containsKey(ID_COLUMN_NAME));
							assertEquals(Integer.toString(1), map.get(ID_COLUMN_NAME));							
							
							assertTrue(map.containsKey(VALUE_COLUMN_NAME));
							assertEquals(value, map.get(VALUE_COLUMN_NAME));							
						}
					};
			}
		};
		
		db.store(firstId, 0, name, value);
	}
	

	@Test
	public void testStoreIntIsNoOp() throws Exception {
		
		assertEquals(0, db.store(firstId, 0));
		assertEquals(0, db.store(firstId, 1));
		assertEquals(0, db.store(firstId, 2));
	}
}
