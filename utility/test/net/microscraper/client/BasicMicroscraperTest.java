package net.microscraper.client;

import static org.junit.Assert.*;

import net.microscraper.database.Database;

import org.junit.Before;
import org.junit.Test;

public class BasicMicroscraperTest extends MicroscraperImplementationTest {
/*
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}
*/
	@Override
	protected Microscraper getScraperToTest(Database database) throws Exception {
		return BasicMicroscraper.get(database, Browser.DEFAULT_RATE_LIMIT, Browser.DEFAULT_TIMEOUT);
	}

}
