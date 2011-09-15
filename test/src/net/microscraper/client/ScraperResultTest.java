package net.microscraper.client;

import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import mockit.Mocked;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;

import org.junit.Test;

public class ScraperResultTest {
	@Test
	public void testSuccessWithValues() {
		String name = randomString();
		DatabaseView[] values = new DatabaseView[] {
				new InMemoryDatabaseView(),
				new InMemoryDatabaseView(),
				new InMemoryDatabaseView()
		};
		ScraperResult result = ScraperResult.success(name, values, new Scraper[] {});
		assertTrue(result.isSuccess());
		assertArrayEquals(values, result.getResultViews());
	}

	@Test
	public void testMissingTags(@Mocked final Scraper scraper) {
		String[] missingTags = new String[] {
				randomString(),
				randomString(),
				randomString()
		};
		ScraperResult result = ScraperResult.missingTags(missingTags, scraper);
		assertTrue(result.isMissingTags());
		assertArrayEquals(missingTags, result.getMissingTags());
		assertEquals(scraper, result.getScraperToRetry());
	}

}
