package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.template.StringTemplate;
import net.microscraper.util.HashtableUtils;

import org.junit.Test;

public class ScraperLocalTest {
	@Mocked private HttpBrowser browser;

	@Test
	public void testIsStuck(@Mocked final Instruction instruction, @Mocked final DatabaseView input) throws Exception {

		final String missingTag = randomString();
		new Expectations() {{
			instruction.execute(null, input, browser); result = InstructionResult.missingTags(new String[] { missingTag} );
			instruction.execute(null, input, browser); result = InstructionResult.missingTags(new String[] { missingTag} );
		}};
		
		Scraper scraper = new Scraper(instruction, input, null, browser);
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
		find.setName(StringTemplate.staticTemplate(randomString()));
		Instruction instruction = new Instruction(find);
		
		Scraper scraper = new Scraper(instruction, HashtableUtils.EMPTY, input, browser);
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
		find.setName(StringTemplate.staticTemplate(randomString()));
		Instruction instruction = new Instruction(find);
		DatabaseView view = new InMemoryDatabaseView();
		Scraper scraper = new Scraper(instruction, view, input, browser);
		ScraperResult result = scraper.scrape();
		assertTrue(result.isSuccess());
		DatabaseView[] resultViews = result.getResultViews();
		assertEquals(mockResultValues.length, resultViews.length);
		for(int i = 0 ; i < resultViews.length ; i ++) {
			assertEquals(mockResultValues[i], resultViews[i].get(result.getName()));
		}
	}
}
