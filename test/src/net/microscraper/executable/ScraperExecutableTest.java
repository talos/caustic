package net.microscraper.executable;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.BasicVariables;
import net.microscraper.Interfaces;
import net.microscraper.Log;
import net.microscraper.MustacheTemplate;
import net.microscraper.impl.log.SystemOutLogger;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Regexp;
import net.microscraper.instruction.Scraper;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.test.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class ScraperExecutableTest {
/*
	private static final int rndLength = 5;
	private static final int repetitions = 8;
	
	// "-" not generated randomly, is never in randomly generated Variables
	private static final String erroneousName = "-";

	private final Log log = new Log();
	
	@Mocked Result sourceResult;
	
	@Mocked private Interfaces interfaces;
	@Mocked private Database database;
	@Mocked private JSONLocation mockLocation;
	@Mocked private MustacheTemplate mockName;
	*/
	
	@Mocked({"generateChildren", "generateResults"}) ScraperExecutable scraperExecutable;
	
	/**
	 * @throws Exception
	 */
	/*@Before
	public void setUp() throws Exception {
		log.register(new SystemOutLogger());
		
		new NonStrictExpectations() {
			@Mocked Instruction instruction;
			@Mocked JSONLocation location;
			
			{
				instruction.getLocation(); result = location;
				location.toString(); result = "";
				
				interfaces.getRegexpCompiler(); result = new JakartaRegexpCompiler();
				interfaces.getDatabase(); result = database;
			}
		};
	}*/
	
	/**
	 * {@link FindOneExecutable} returns its one value for its name.
	 * @throws Exception
	 */
	@Test
	public void testFindOneLocalValue() throws Exception {
		/*final String name = TestUtils.makeRandomString(rndLength);
		final String value = TestUtils.makeRandomString(rndLength);
		
		FindOne findOne = new FindOne(mockLocation, new MustacheTemplate(name), new MustacheTemplate(value),
				false, false, false, new Regexp[] {},
				new MustacheTemplate("$0"), 0, new FindOne[] {}, new FindMany[] { });
		final Scraper scraper = new Scraper(mockLocation, mockName,
				new Page[] {},
				new Scraper[] {},
				new FindMany[] {},
				new FindOne[] { findOne } );
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = value;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(interfaces, scraper, new BasicVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		assertEquals(false, executable.containsKey(erroneousName));
		assertEquals(true, executable.containsKey(name));
		assertEquals(value, executable.get(name));*/
		
		new NonStrictExpectations() {
			FindOneExecutable findOneExecutable;
			{
				scraperExecutable.generateChildren(null); result = new Executable[] { findOneExecutable };
			}
		};
		
		scraperExecutable.run();
		scraperExecutable.containsKey("test");
	}
}
