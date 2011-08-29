package net.microscraper.client;

import net.microscraper.database.Database;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.RateLimitManager;

public class BasicMicroscraperTest extends MicroscraperImplementationTest {

	@Override
	protected Microscraper getScraperToTest(Database database) throws Exception {
		return BasicMicroscraper.get(database, RateLimitManager.DEFAULT_RATE_LIMIT, HttpRequester.DEFAULT_TIMEOUT_MILLISECONDS);
	}

}
