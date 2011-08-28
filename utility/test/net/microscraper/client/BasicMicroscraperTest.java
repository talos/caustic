package net.microscraper.client;

import static org.junit.Assert.*;

import net.microscraper.database.Database;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.HttpRequester;
import net.microscraper.http.RateLimitManager;

import org.junit.Before;
import org.junit.Test;

public class BasicMicroscraperTest extends MicroscraperImplementationTest {

	@Override
	protected Microscraper getScraperToTest(Database database) throws Exception {
		return BasicMicroscraper.get(database, RateLimitManager.DEFAULT_RATE_LIMIT, HttpRequester.DEFAULT_TIMEOUT_SECONDS);
	}

}
