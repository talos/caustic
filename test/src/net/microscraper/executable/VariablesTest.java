package net.microscraper.executable;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.DefaultVariables;
import net.microscraper.Interfaces;
import net.microscraper.Log;
import net.microscraper.MustacheTemplate;
import net.microscraper.executable.Executable;
import net.microscraper.executable.FindOneExecutable;
import net.microscraper.executable.Result;
import net.microscraper.executable.ScraperExecutable;
import net.microscraper.executable.SpawnedScraperExecutable;
import net.microscraper.impl.log.SystemLogInterface;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.instruction.FindMany;
import net.microscraper.instruction.FindOne;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Regexp;
import net.microscraper.instruction.Scraper;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.test.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class VariablesTest {
	private static final int rndLength = 5;
	private static final int repetitions = 8;
	
	// "-" not generated randomly, is never in randomly generated Variables
	private static final String erroneousName = "-";

	private final Log log = new Log();
	
	@Mocked Result sourceResult;
	
	@Mocked private Interfaces interfaces;
	@Mocked private JSONLocation mockLocation;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		log.register(new SystemLogInterface());
		
		new NonStrictExpectations() {
			{
				sourceResult.getName(); result = "";
				setField(interfaces, "regexpCompiler", new JakartaRegexpCompiler());
			}
		};
	}
	
	/**
	 * {@link FindOneExecutable} returns its one value for its name.
	 * @throws Exception
	 */
	@Test
	public void testFindOneLocalValue() throws Exception {
		final String name = TestUtils.makeRandomString(rndLength);
		final String value = TestUtils.makeRandomString(rndLength);
		
		FindOne findOne = new FindOne(mockLocation, new MustacheTemplate(value),
				false, false, false, new MustacheTemplate(name), new Regexp[] {},
				new MustacheTemplate("$0"), 0, new FindOne[] {}, new FindMany[] { });
		final Scraper scraper = new Scraper(mockLocation,
				new Page[] {},
				new Scraper[] {},
				new FindMany[] {},
				new FindOne[] { findOne } );
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = value;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(interfaces, scraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		assertEquals(false, executable.containsKey(erroneousName));
		assertEquals(true, executable.containsKey(name));
		assertEquals(value, executable.get(name));
	}
	
	
	/**
	 * {@link FindOne}s that spawn other {@link FindOne}s should share {@link Variable}s throughout the chain.
	 * @throws Exception
	 */
	@Test
	public void findOneFindOnesChildrenShareVariables() throws Exception {		
		final FindOne[] findOnes = new FindOne[repetitions];
		for(int i = 0 ; i < findOnes.length ; i ++) {
			String pattern;
			FindOne[] children;
			if(i > 0) {
				pattern  = findOnes[i - 1].getPattern() + " " + TestUtils.makeRandomString(rndLength);
				children = new FindOne[] { findOnes[i - 1] };
			} else { // the first generated
				pattern  = TestUtils.makeRandomString(rndLength);
				children = new FindOne[]  { };
			}
			findOnes[i] = new FindOne(mockLocation, new MustacheTemplate(pattern), 
					false, false, false, new MustacheTemplate(TestUtils.makeRandomString(rndLength)),
					new Regexp[] {}, new MustacheTemplate("$0"), 0, children, 
					new FindMany[] {});
		}
		final FindOne parentFindOne = findOnes[findOnes.length -1];
		final Scraper scraper = new Scraper(mockLocation,
				new Page[] {},
				new Scraper[] {},
				new FindMany[] {},
				new FindOne[] { parentFindOne } );
		
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = parentFindOne.getPattern().toString();
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(interfaces, scraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		Executable[] allChildren = TestUtils.getAllChildren(executable);
		
		for(int i = 0 ; i < allChildren.length ; i ++) {
			FindOneExecutable child = (FindOneExecutable) allChildren[i];
			// Ensure presence of all variables.
			for(int j = 0 ; j < findOnes.length ; j ++ ) {
				FindOne findOne = findOnes[j];
				assertEquals("FindOne " + child.getName() + " contains erroneous key.",
						false, child.containsKey(erroneousName));
				assertEquals("FindOne " + child.getName() + " = " + child.getPattern() + " does not contain key for " + findOne.getPattern().toString(),
						true, child.containsKey(findOne.getName().toString()));
				assertEquals("FindOne " + child.getName() + " = " + child.getPattern() + " does not contain value for " + findOne.getPattern().toString(),
						findOne.getPattern().toString(), child.get(findOne.getName().toString()));
			}
		}
	}
	
	/**
	 * All {@link FindOne}s spawned from a {@link Scraper} should share {@link Variable}s.
	 */
	@Test
	public void scraperFindOnesShareVariables  () throws Exception {
		final FindOne[] findOnes = new FindOne[repetitions];
		String buildMockResultValue = "";
		for(int i = 0 ; i < findOnes.length ; i ++ ) {
			String pattern = TestUtils.makeRandomString(rndLength);
			buildMockResultValue += pattern;
			findOnes[i] = new FindOne(mockLocation, new MustacheTemplate(pattern), 
					false, false, false, new MustacheTemplate(TestUtils.makeRandomString(rndLength)),
					new Regexp[] {}, new MustacheTemplate("$0"), 0, new FindOne[] {}, 
					new FindMany[] {});
		}
		final String mockResultValue = buildMockResultValue;
		
		final Scraper scraper = new Scraper(mockLocation,
				new Page[] {},
				new Scraper[] {},
				new FindMany[] {},
				findOnes );
		
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = mockResultValue;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(interfaces, scraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		assertEquals("ScraperExecutable has erroneous key.", false, executable.containsKey(erroneousName));
		for(int i = 0 ; i < findOnes.length ; i ++) {
			String name = findOnes[i].getName().toString();
			String value = findOnes[i].getPattern().toString();
			
			assertEquals("ScraperExecutable does not have key for child", true, executable.containsKey(name));
			assertEquals("ScraperExecutable has wrong value for child", value, executable.get(name));
			
			Executable[] allChildren = TestUtils.getAllChildren(executable);
			for(int j = 0 ; j < allChildren.length ; j ++) {
				assertEquals("FindOne does not have key for sibling.", true, allChildren[j].containsKey(name));
				assertEquals("FindOne has wrong value for sibling.", value, allChildren[j].get(name));
				assertEquals("FindOne has erroneous key", false, allChildren[j].containsKey(erroneousName));
			}
		}
	}
	
	/**
	 * {@link Scraper} should not have access to the results of {@link FindMany}.
	 */
	@Test
	public void findManyParentsCannotAccessVariables () throws Exception {
		final String sourceString = TestUtils.makeRandomString(rndLength);
		final MustacheTemplate pattern = new MustacheTemplate(sourceString);
		final String name = TestUtils.makeRandomString(rndLength);
		final MustacheTemplate replacement = new MustacheTemplate("$0");
		final FindMany findMany = new FindMany(mockLocation, pattern, false, false, false,
				new MustacheTemplate(name), new Regexp[] {}, replacement, 0, -1,
				new Scraper [] {}, new Page [] {});
		final Scraper scraper = new Scraper(mockLocation, new Page[] {}, 
				new Scraper[] {}, new FindMany[] { findMany }, new FindOne[] { } );
		
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = sourceString;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(interfaces, scraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		assertEquals("Scraper has access to FindMany value.", false, executable.containsKey(name));
	}
	
	/**
	 * {@link Scraper} children of {@link FindMany} should have access to the {@link Result} that spawned them.
	 */
	@Test
	public void scraperChildrenOfFindManyCanAccessVariables () throws Exception {
		final String sourceString = "zip zap zop";
		
		MustacheTemplate pattern = new MustacheTemplate("z.p");
		String name = TestUtils.makeRandomString(rndLength);
		MustacheTemplate replacement = new MustacheTemplate("$0");
		Scraper childScraper = new Scraper(mockLocation, new Page[] {},
				new Scraper[] {}, new FindMany[] {}, new FindOne[] {} );
		FindMany findMany = new FindMany(mockLocation, pattern, false, false, false,
				new MustacheTemplate(name), new Regexp[] {}, replacement, 0, -1,
				new Scraper [] { childScraper }, new Page [] {});
		Scraper parentScraper = new Scraper(mockLocation, new Page[] {}, 
				new Scraper[] {}, new FindMany[] { findMany }, new FindOne[] { } );
		
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = sourceString;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(
				interfaces, parentScraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		Executable findManyExecutable = executable.getChildren()[0];
		Executable[] children = findManyExecutable.getChildren();
		
		for(int i = 0 ; i < children.length ; i++) {
			assertEquals("Scraper doesn't have access to the result of FindMany that spawned it.",
					true, children[i].containsKey(name));
		}
	}
	

	/**
	 * {@link Scraper} children of {@link FindMany} should have access to all the {@link FindOne} results of the
	 * original {@link Scraper}.
	 */
	@Test
	public void scraperChildrenOfFindManyCanAccessVariablesOfOriginalScraper () throws Exception {
		String sourceString = "zip zap zop";
		
		MustacheTemplate pattern = new MustacheTemplate("z.p");
		String name = TestUtils.makeRandomString(rndLength);
		MustacheTemplate replacement = new MustacheTemplate("$0");
		Scraper childScraper = new Scraper(mockLocation, new Page[] {},
				new Scraper[] {}, new FindMany[] {}, new FindOne[] {} );
		FindMany findMany = new FindMany(mockLocation, pattern, false, false, false,
				new MustacheTemplate(name), new Regexp[] {}, replacement, 0, -1,
				new Scraper [] { childScraper }, new Page [] {});
		
		FindOne[] findOnes = new FindOne[repetitions];
		for(int i = 0 ; i < findOnes.length ; i ++) {
			String findOnePattern = TestUtils.makeRandomString(rndLength);
			sourceString += findOnePattern;
			findOnes[i] = new FindOne(mockLocation, new MustacheTemplate(findOnePattern),
					false, false, false, new MustacheTemplate(TestUtils.makeRandomString(rndLength)),
					new Regexp[] {}, replacement, 0, new FindOne[] {}, new FindMany[] {} );
		}
		Scraper parentScraper = new Scraper(mockLocation, new Page[] {}, 
				new Scraper[] {}, new FindMany[] { findMany }, findOnes );
		
		final String finalSourceString = sourceString;
		new NonStrictExpectations() {
			{
				sourceResult.getValue(); result = finalSourceString;
			}
		};
		
		ScraperExecutable executable = new SpawnedScraperExecutable(
				interfaces, parentScraper, new DefaultVariables(), sourceResult);
		TestUtils.recursiveRun(executable);
		
		Executable findManyExecutable = executable.getChildren()[0];
		Executable[] children = findManyExecutable.getChildren();
		
		for(int i = 0 ; i < children.length ; i++) {
			for(int j = 0 ; j < findOnes.length ; j ++) {
				assertEquals("Scraper from FindMany does not have keys for FindOnes from original Scraper.",
						true, children[i].containsKey(findOnes[j].getName().toString()));
			}
			
		}
	}
}