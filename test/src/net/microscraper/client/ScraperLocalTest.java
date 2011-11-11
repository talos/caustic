package net.microscraper.client;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import net.microscraper.concurrent.SyncExecutor;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.DatabaseViewListener;
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

/**
 * Test {@link Scraper} without a network connection.
 * @author talos
 *
 */
public class ScraperLocalTest {
	@Mocked private HttpBrowser browser;
	@Mocked DatabaseViewListener listener;
	DatabaseView view;
	
	@Before
	public void setUp() {
		view = new InMemoryDatabaseView();
		view.addListener(listener);
	}
	
	@Test
	public void testExecutesTwice(@Mocked final Instruction instruction) throws Exception {

		final String missingTag = randomString();
		
		new Expectations() {{
			instruction.execute(null, (DatabaseView) any, (HttpBrowser) any);
				result = InstructionResult.missingTags(new String[] { missingTag } );
				times = 2;
				$ = "should execute the same instruction twice from single scrape";
		}};
		
		Scraper scraper = new Scraper(new InMemoryDatabaseView(), browser, new SyncExecutor());
		scraper.scrape(instruction);
		
		new Verifications() {{
			listener.put((String) any, (String) any); times = 0; $ = "Should not have any results.";
		}};
	}
}
