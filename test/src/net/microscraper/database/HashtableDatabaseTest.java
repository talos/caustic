package net.microscraper.database;

import static org.junit.Assert.*;

import net.microscraper.util.IntUUIDFactory;

import org.junit.Before;
import org.junit.Test;

public class HashtableDatabaseTest extends DatabaseTest {

	@Override
	protected Database getDatabase() throws Exception {
		return new HashtableDatabase(new IntUUIDFactory());
	}

}
