package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.microscraper.concurrent.SyncExecutor;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.DatabaseViewHook;
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
	@Mocked DatabaseViewHook hook;
	
	@Test
	public void testExecutesTwice(@Mocked final Instruction instruction) throws Exception {

		final String missingTag = randomString();
		
		// 
		new Expectations() {{
			instruction.execute(null, (DatabaseView) any, (HttpBrowser) any);
				result = InstructionResult.missingTags(new String[] { missingTag } );
				times = 2;
				$ = "should execute the same instruction twice from single scrape";
		}};
		
		Scraper scraper = new Scraper(instruction, HashtableUtils.EMPTY, null, browser, hook);
		scraper.scrapeSync();
		
		new Verifications() {{
			hook.put((String) any, (String) any); times = 0; $ = "Should not have any results.";
		}};
	}
	
	@Test
	public void testBindsStringFromHashtable(@Mocked final RegexpCompiler compiler) throws Exception {
		final String search = randomString();
		final String input = randomString();
		final String name = randomString();
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
		find.setName(new StaticStringTemplate(name));
		Instruction instruction = new Instruction(find);
		
		Scraper scraper = new Scraper(instruction, HashtableUtils.EMPTY, input, browser, hook);
		scraper.scrapeSync();
		
		new Verifications() {{
			for(int i = 0 ; i < mockResultValues.length ; i ++) {
				hook.put(name, mockResultValues[i]);				
			}
		}};
	}
	

	@Test
	public void testBindsStringFromDatabaseView(@Mocked final RegexpCompiler compiler) throws Exception {
		final String search = randomString();
		final String input = randomString();
		final String name = randomString();
		
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
		view.addHook(hook);
		Scraper scraper = new Scraper(instruction, view, input, browser);
		scraper.scrapeSync();
		
		new Verifications() {{
			for(int i = 0 ; i < mockResultValues.length ; i ++) {
				hook.put(name, mockResultValues[i]);				
			}
		}};
	}
}
