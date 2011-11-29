package net.caustic.database;

public class MemoryDatabaseTest extends DatabaseTest {

	@Override
	public Database getDatabase() throws Exception {
		return new MemoryDatabase();
	}

}
