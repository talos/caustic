package net.microscraper.database;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;
import static net.microscraper.database.MultiTableDatabase.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.UpdateableConnection;
import net.microscraper.database.Updateable;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.util.IntUUIDFactory;

import org.junit.Test;

public class MultiTableDatabaseTest extends DatabaseTest  {

	@Mocked Updateable resultTable;
	@Mocked Insertable joinTable;
	@Mocked UpdateableConnection connection, otherConn;
		
	@Override
	protected Database getDatabase() throws Exception {
		new NonStrictExpectations() {{

			connection.newUpdateable(anyString, (String[]) any);
				result = resultTable;
			connection.newInsertable(anyString, (String[]) any);
				result = joinTable;
		}};
		return new MultiTableDatabase(new HashtableDatabase(new IntUUIDFactory()), connection);
	}
	
	@Test
	public void testCreatesDefaultResultTableOnInstantiation() throws Exception {
		new Expectations() {{
			otherConn.open();
			otherConn.newUpdateable(DEFAULT_TABLE_NAME, withSameInstance(RESULT_TABLE_COLUMNS));
				result = resultTable;
				$ = "Should create root table on getting default scope.";
		}};
		Database db = new MultiTableDatabase(new HashtableDatabase(new IntUUIDFactory()), otherConn);
		db.open();
	}
	
	@Test
	public void testStoreOneToOneWithValueUpdatesRootTable() throws Exception {
		final String name = randomString();
		final String value = randomString();
		new Expectations() {
			{
				resultTable.hasColumn(name); result = false;
				resultTable.addColumn(name); $ = "Should add name column to root table.";
				resultTable.update(SCOPE_COLUMN_NAME, scope.getID(), (Hashtable) any);
					forEachInvocation = new Object() {
						void validate(String columnName, net.microscraper.util.UUID id, Hashtable map) {
							assertEquals("Should update name and value in root table.", 1, map.size());
							assertTrue(map.containsKey(name));
							assertEquals(value, map.get(name));
						}
					};
			}
		};
		
		db.storeOneToOne(scope, name, value);
	}
	
	@Test
	public void testStoreOneToManyWithValueCreatesTableWithNameAndValueColumn() throws Exception {
		final String name = randomString();
		final String value = randomString();
		new Expectations() {
			//@Injectable Updateable resultTable;
			{
				//connection.getIOTable(name, withSameInstance(RESULT_TABLE_COLUMNS)); result = resultTable;
				joinTable.insert((Hashtable<String, String>) any);
					forEachInvocation = new Object() {
						void validate(Hashtable<String, String> map) {
							//assertEquals("Should create blank entry in new table", 0, nvps.length);
							assertEquals(3, map.size());
							
							assertTrue(map.containsKey(SOURCE_COLUMN_NAME));
							assertEquals(Integer.toString(scope.hashCode()), map.get(SOURCE_COLUMN_NAME));
							
							assertTrue(map.containsKey(SCOPE_COLUMN_NAME));
							assertEquals(Integer.toString(2), map.get(SCOPE_COLUMN_NAME));							
							
							assertTrue(map.containsKey(VALUE_COLUMN_NAME));
							assertEquals(value, map.get(VALUE_COLUMN_NAME));							
						}
					};
			}
		};
		
		db.storeOneToMany(scope, name, value);
	}
}
