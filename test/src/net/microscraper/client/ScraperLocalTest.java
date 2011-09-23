package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import mockit.Expectations;
import mockit.Mocked;
import net.microscraper.concurrent.Executor;
import net.microscraper.concurrent.SyncExecutor;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.InstructionResult;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.util.HashtableUtils;
import net.microscraper.util.StaticStringTemplate;

import org.junit.Before;
import org.junit.Test;

public class ScraperLocalTest {
	@Mocked private HttpBrowser browser;
	private Executor executor;
	
	@Before
	public void setUp() {
		executor = new SyncExecutor();
	}

	@Test
	public void testIsStuck(@Mocked final Instruction instruction, @Mocked final DatabaseView input) throws Exception {

		final String missingTag = randomString();
		
		// should execute the same instruction twice from single scrape
		new Expectations() {{
			instruction.execute(null, input, (HttpBrowser) any); result = InstructionResult.missingTags(new String[] { missingTag} );
				times = 2;
		}};
		
		Scraper scraper = new Scraper(instruction, input, null, browser, executor);
		InstructionResult[] results = scraper.scrape();
		assertEquals("Should only be one result.", 1, results.length);
		assertTrue("Should be missing tags.", results[0].isMissingTags());
		assertEquals("Should be missing one tag", 1, results[0].getMissingTags().length);
		assertEquals("Is missing wrong tag", missingTag, results[0].getMissingTags()[0]);
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
				compiler.newPattern(search, anyBoolean, anyBoolean, anyBoolean); result = pattern;
				pattern.match(input, anyString, anyInt, anyInt); result = mockResultValues;
			}
		};
		
		Find find = new Find(compiler, new StaticStringTemplate(search));
		find.setName(new StaticStringTemplate(randomString()));
		Instruction instruction = new Instruction(find);
		
		Scraper scraper = new Scraper(instruction, HashtableUtils.EMPTY, input, browser, executor);
		InstructionResult[] results = scraper.scrape();
		assertEquals(1, results.length);
		for(int i = 0 ; i < results.length ; i ++) {
			InstructionResult result = results[i];
			assertTrue(result.isSuccess());
			
			/*DatabaseView[] resultViews = result.getResults()
			assertEquals(mockResultValues.length, resultViews.length);
			for(int i = 0 ; i < resultViews.length ; i ++) {
				assertEquals(mockResultValues[i], resultViews[i].get(result.getName()));
			}*/
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
				compiler.newPattern(search, anyBoolean, anyBoolean, anyBoolean); result = pattern;
				pattern.match(input, anyString, anyInt, anyInt); result = mockResultValues;
			}
		};
		Find find = new Find(compiler, new StaticStringTemplate(search));
		find.setName(new StaticStringTemplate(randomString()));
		Instruction instruction = new Instruction(find);
		DatabaseView view = new InMemoryDatabaseView();
		Scraper scraper = new Scraper(instruction, view, input, browser, executor);
		InstructionResult[] results = scraper.scrape();
		assertEquals(1, results.length);
		for(InstructionResult result : results) {
			assertTrue(result.isSuccess());
		}
		/*assertTrue(result.isSuccess());
		DatabaseView[] resultViews = result.getResultViews();
		assertEquals(mockResultValues.length, resultViews.length);
		for(int i = 0 ; i < resultViews.length ; i ++) {
			assertEquals(mockResultValues[i], resultViews[i].get(result.getName()));
		}*/
	}
}
