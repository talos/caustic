package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.HashtableUtils;

import org.junit.Test;

public class ScraperLocalTest {
	@Mocked private Instruction instruction;
	@Mocked private DatabaseView input;

	@Test
	public void testIsStuck() throws Exception {

		final String missingTag = randomString();
		new Expectations() {{
			instruction.execute(null, input); result = ScraperResult.missingTags(new String[] { missingTag} );
			instruction.execute(null, input); result = ScraperResult.missingTags(new String[] { missingTag} );
		}};
		
		Scraper scraper = new Scraper(instruction, input, null);
		scraper.scrape();
		scraper.scrape();
		
		assertTrue("After missing the same tag twice, should be stuck.", scraper.isStuck());
	}
	
	@Test
	public void testBindsStringFromHashtable(@Mocked final RegexpCompiler compiler) throws Exception {
		final String search = randomString();
		final String input = randomString();
		final String[] mockResultValues = new String[] {
				randomString(),
				randomString()
		};
		new Expectations() {
			Pattern pattern;
			{
				compiler.compile(search, anyBoolean, anyBoolean, anyBoolean); result = pattern;
				pattern.match(input, anyString, anyInt, anyInt); result = mockResultValues;
			}
		};
		Find find = new Find(compiler, StringTemplate.staticTemplate(search));
		Scraper scraper = new Scraper(find, HashtableUtils.EMPTY, input);
		ScraperResult result = scraper.scrape();
		assertTrue(result.isSuccess());
		DatabaseView[] resultViews = result.getResultViews();
		assertEquals(mockResultValues.length, resultViews.length);
		for(int i = 0 ; i < resultViews.length ; i ++) {
			assertEquals(mockResultValues[i], resultViews[i].get(result.getName()));
		}
	}
	

	@Test
	public void testBindsStringFromDatabaseView(@Mocked final RegexpCompiler compiler) throws Exception {
		final String search = randomString();
		final String input = randomString();
		final String[] mockResultValues = new String[] {
				randomString(),
				randomString()
		};
		new Expectations() {
			Pattern pattern;
			{
				compiler.compile(search, anyBoolean, anyBoolean, anyBoolean); result = pattern;
				pattern.match(input, anyString, anyInt, anyInt); result = mockResultValues;
			}
		};
		Find find = new Find(compiler, StringTemplate.staticTemplate(search));
		DatabaseView view = new InMemoryDatabaseView();
		Scraper scraper = new Scraper(find, view, input);
		ScraperResult result = scraper.scrape();
		assertTrue(result.isSuccess());
		DatabaseView[] resultViews = result.getResultViews();
		assertEquals(mockResultValues.length, resultViews.length);
		for(int i = 0 ; i < resultViews.length ; i ++) {
			assertEquals(mockResultValues[i], resultViews[i].get(result.getName()));
		}
	}
}
