package net.caustic.database;

public class InMemoryDatabaseTest extends DatabaseTest {

	@Override
	public Database getDatabase() throws Exception {
		return new InMemoryDatabase();
	}

}
