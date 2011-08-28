package net.microscraper.database;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HashtableDatabaseTest extends DatabaseTest {

	@Override
	protected Database getDatabase() throws Exception {
		return new HashtableDatabase();
	}

}
