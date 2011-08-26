package net.microscraper.database;

import static org.junit.Assert.*;

import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

public class SingleTableDatabaseTest {

	@Mocked InsertableConnection connection;
	SingleTableDatabase database;
	Variables variables;
	
	@Before
	public void setUp() throws Exception {
		database = new SingleTableDatabase(connection);
		variables = database.open();
	}

	@Test
	public void testClose() {
		fail("Not yet implemented");
	}

	@Test
	public void testStoreOneToOneIntString() {
		fail("Not yet implemented");
		//database.storeOneToOne(sourceId, name);
	}

	@Test
	public void testStoreOneToOneIntStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testStoreOneToManyIntString() {
		fail("Not yet implemented");
	}

	@Test
	public void testStoreOneToManyIntStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testOpen() {
		fail("Not yet implemented");
	}

	@Test
	public void testToStringInt() {
		fail("Not yet implemented");
	}

}
