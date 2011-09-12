package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.database.DatabaseView;
import net.microscraper.instruction.Instruction;

import org.junit.Before;
import org.junit.Test;

public class ScraperLocalTest {
	@Mocked private Instruction instruction;
	@Mocked private DatabaseView input;
	private Scraper scraper;
	
	@Before
	public void setUp() throws Exception {
		scraper = new Scraper(instruction, input, null);
	}

	@Test
	public void testIsStuck() throws Exception {
		final String missingTag = randomString();
		new Expectations() {{
			instruction.execute(null, input); result = ScraperResult.missingTags(new String[] { missingTag} );
			instruction.execute(null, input); result = ScraperResult.missingTags(new String[] { missingTag} );
		}};
		
		scraper.scrape();
		scraper.scrape();
		
		assertTrue("After missing the same tag twice, should be stuck.", scraper.isStuck());
	}

}
