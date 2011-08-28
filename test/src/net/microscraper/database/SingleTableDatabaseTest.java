package net.microscraper.database;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Before;
import org.junit.Test;

public class SingleTableDatabaseTest extends DatabaseTest  {

	@Mocked InsertableConnection connection;
	@Mocked Insertable table;
	
	@Override
	public Database getDatabase() throws Exception {
		new NonStrictExpectations() {{
			connection.getInsertable((String[]) any); result = table;
		}};
		return new SingleTableDatabase(connection);
	}
}
