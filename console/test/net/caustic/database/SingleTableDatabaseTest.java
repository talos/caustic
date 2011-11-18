package net.caustic.database;

import org.junit.After;

import net.caustic.database.JDBCSqliteConnection;

public class SingleTableDatabaseTest extends DatabaseTest {
	private Connection connection;
	
	@Override
	public Database getDatabase() throws Exception {
		connection = JDBCSqliteConnection.inMemory("scope", true);
		connection.open();
		return new SingleTableDatabase(connection);
	}

	@After
	public void tearDown() throws Exception {
		connection.close();
	}
}
