package net.microscraper.database;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import net.microscraper.database.IOConnection;
import net.microscraper.database.IOTable;
import net.microscraper.database.MultiTableDatabase;
import net.microscraper.instruction.Result;
import net.microscraper.util.NameValuePair;

import org.junit.Before;
import org.junit.Test;

public class MultiTableDatabaseTest {

	@Mocked IOTable rootTable, resultTable;
	@Mocked IOConnection connection;
	@Tested MultiTableDatabase db;
	
	@Before
	public void setUp() throws Exception {
		new NonStrictExpectations() {
			{
				connection.getIOTable(withEqual(MultiTableDatabase.ROOT_TABLE_NAME), (String[]) any);
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
		db.store("name", "value", 0);
		
		new Verifications() {{
			rootTable.insert((NameValuePair[]) any); times = 1;
			rootTable.addColumn("name"); times = 1;
			resultTable.insert((NameValuePair[]) any); times =1;
		}};
	}

	@Test
	public void testStoreResultStringString() throws Exception {
		int parentId = db.store("parentName", "parentValue", 0);
		db.store("parentName", 0, "childName", "childValue", 0);
		
		new Verifications() {{
			//table.insert((NameValuePair[]) any); times = 3;
			//table.addColumn(anyString); times = 1;
		}};
	}
}
