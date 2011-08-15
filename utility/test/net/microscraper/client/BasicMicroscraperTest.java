package net.microscraper.client;

import net.microscraper.database.Database;

public class BasicMicroscraperTest extends MicroscraperImplementationTest {

	@Override
	protected Microscraper getScraperToTest(Database database) {
		return new BasicMicroscraper(database);
	}

}
