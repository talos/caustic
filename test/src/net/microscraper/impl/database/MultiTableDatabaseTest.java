package net.microscraper.impl.database;

import static org.junit.Assert.*;

import mockit.Cascading;
import mockit.Input;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.database.Connection;
import net.microscraper.interfaces.database.Table;

import org.junit.Before;
import org.junit.Test;

public class MultiTableDatabaseTest {

	@Mocked Table rootTable, resultTable;
	@Mocked Connection connection;
	@Tested MultiTableDatabase db;
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {
			{
				connection.getTable(withEqual(MultiTableDatabase.ROOT_TABLE_NAME), (String[]) any);
						result = rootTable;
				rootTable.insert((NameValuePair[]) any); result = 0;
				//connection.getTable(withNotEqual(MultiTableDatabase.ROOT_TABLE_NAME), (String[]) any);
				//		result = resultTable;
			}
		};
		db = new MultiTableDatabase(connection);
	}
	
	@Test
	public void testMultiTableDatabase() throws Exception {
		new Verifications() {{
			rootTable.insert((NameValuePair[]) any); times = 1;
		}};
	}
	
	@Test
	public void testStoreStringString() throws Exception {
		Result result = db.store("name", "value");
		assertEquals("name", result.getName());
		assertEquals("value", result.getValue());
				
		new Verifications() {{
			rootTable.insert((NameValuePair[]) any); times = 1;
			rootTable.addColumn("name"); times = 1;
			resultTable.insert((NameValuePair[]) any); times =1;
		}};
	}

	@Test
	public void testStoreResultStringString() throws Exception {
		Result parentResult = db.store("parentName", "parentValue");
		db.store(parentResult, "childName", "childValue");
		
		new Verifications() {{
			//table.insert((NameValuePair[]) any); times = 3;
			//table.addColumn(anyString); times = 1;
		}};
	}
}
