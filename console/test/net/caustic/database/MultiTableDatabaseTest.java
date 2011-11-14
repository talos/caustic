package net.caustic.database;

import org.junit.After;

import net.caustic.database.sql.JDBCSqliteConnection;

public class MultiTableDatabaseTest extends DatabaseTest {
	private IOConnection connection;
	
	@Override
	public Database getDatabase() throws Exception {
		connection = JDBCSqliteConnection.inMemory("scope", true);
		connection.open();
		return new MultiTableDatabase(connection);
	}

	@After
	public void tearDown() throws Exception {
		connection.close();
	}
}
