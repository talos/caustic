package net.microscraper.client;

import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

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
	public void testMissingTags() {
		String[] missingTags = new String[] {
				randomString(),
				randomString(),
				randomString()
		};
		ScraperResult result = ScraperResult.missingTags(missingTags);
		assertTrue(result.isMissingTags());
		assertArrayEquals(missingTags, result.getMissingTags());
	}

	@Test
	public void testFailure() {
		String failedBecause = randomString();
		ScraperResult result = ScraperResult.fromSubstitutionOverwrite(failedBecause);
		assertEquals(failedBecause, result.getFailedBecause());
	}

}
