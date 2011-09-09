package net.microscraper.client;

import static net.microscraper.util.TestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class ScraperResultTest {
	@Test
	public void testSuccessWithValues() {
		String name = randomString();
		String[] values = new String[] {
				randomString(),
				randomString(),
				randomString()
		};
		ScraperResult result = ScraperResult.success(name, values, new Scraper[] {});
		assertTrue(result.isSuccess());
		assertArrayEquals(values, result.getValues());
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
		ScraperResult result = ScraperResult.failure(failedBecause);
		assertEquals(failedBecause, result.getFailedBecause());
	}

}
