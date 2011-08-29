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

	@Mocked Updateable table;
	@Mocked UpdateableConnection connection, otherConn;
		
	@Override
	protected Database getDatabase() throws Exception {
		new NonStrictExpectations() {{
			//connection.getIOTable(anyString, withSameInstance(RESULT_TABLE_COLUMNS));
			//	result = rootTable;
			//	result = resultTable;
			//connection.getIOTable(ROOT_TABLE_NAME, withSameInstance(ROOT_TABLE_COLUMNS));
			//	result = rootTable;
			connection.getIOTable(anyString, (String[]) any);
				result = table;
		}};
		return new MultiTableDatabase(new HashtableDatabase(new IntUUIDFactory()), connection);
	}
	
	@Test
	public void testCreatesRootTableOnInstantiation() throws Exception {
		new Expectations() {{
			otherConn.getIOTable(ROOT_TABLE_NAME, withSameInstance(ROOT_TABLE_COLUMNS));
				result = table;
				$ = "Should create root table on instantiation.";
		}};
		new MultiTableDatabase(new HashtableDatabase(new IntUUIDFactory()), otherConn);
	}
	
	@Test
	public void testStoreOneToOneWithValueUpdatesRootTable() throws Exception {
		final String name = randomString();
		final String value = randomString();
		new Expectations() {
			{
				table.hasColumn(name); result = false;
				table.addColumn(name); $ = "Should add name column to root table.";
				table.update(SCOPE_COLUMN_NAME, scope.getID(), (Hashtable) any);
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
				table.insert((Hashtable<String, String>) any);
					forEachInvocation = new Object() {
						void validate(Hashtable<String, String> map) {
							//assertEquals("Should create blank entry in new table", 0, nvps.length);
							assertEquals(4, map.size());
							
							assertTrue(map.containsKey(SOURCE_TABLE_COLUMN));
							assertEquals(ROOT_TABLE_NAME, map.get(SOURCE_TABLE_COLUMN));

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
