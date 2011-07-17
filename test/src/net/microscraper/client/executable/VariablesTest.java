package net.microscraper.client.executable;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Injectable;
import mockit.MockClass;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Client;
import net.microscraper.client.DefaultVariables;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
import net.microscraper.client.TestUtils;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.client.Variables;
import net.microscraper.client.impl.FileLoader;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaNetInterface;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.instruction.FindMany;
import net.microscraper.server.instruction.FindOne;
import net.microscraper.server.instruction.Page;
import net.microscraper.server.instruction.Regexp;
import net.microscraper.server.instruction.Scraper;

import org.junit.Before;
import org.junit.Test;

public class VariablesTest {
	private static final int rndLength = 5;
	
	// not generated randomly, cannot be in Variables
	private static final String erroneousName = "-";

	Log log = new Log();
	
	@Mocked Result sourceResult;
	
	@Mocked private Interfaces interfaces;
	@Mocked private URIInterface mockLocation;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		log.register(new SystemLogInterface());
		
		new NonStrictExpectations() {
			{
				mockLocation.isAbsolute(); result = true;
								
				setField(interfaces, "regexpCompiler", new JavaUtilRegexInterface());
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
	 * {@link FindOneExecutable} values are accessible through multiple levels.
	 * @throws Exception
	 */
	@Test
	public void testFindOneChildValue() throws Exception {
		int levels = 8;
		
		final FindOne[] findOnes = new FindOne[levels];
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
						false, child.getVariables().containsKey(erroneousName));
				assertEquals("FindOne " + child.getName() + " = " + child.getPattern() + " does not contain key for " + findOne.getPattern().toString(),
						true, child.getVariables().containsKey(findOne.getName().toString()));
				assertEquals("FindOne " + child.getName() + " = " + child.getPattern() + " does not contain value for " + findOne.getPattern().toString(),
						findOne.getPattern().toString(), child.getVariables().get(findOne.getName().toString()));
			}
		}
	}
	
	/**
	 * {@link PageExecutable} works as {@link Variables} for its child {@link FindOneExecutables}.
	 */
	@Test
	public void testPageChildren  () throws Exception {
		
		PageExecutable pageExecutable;
		
		final String name1 = "name1";
		final String value1 = "value1";
		final String name2 = "name2";
		final String value2 = "value2";
		final String name3 = "name3";
		final String value3 = "value3";
		
		new NonStrictExpectations() {
			@Mocked Result mockResult1, mockResult2, mockResult3;
			{
				mockResult1.getName(); result = name1;
				mockResult2.getName(); result = name2;
				mockResult3.getName(); result = name3;
				mockResult1.getValue(); result = value1;
				mockResult2.getValue(); result = value2;
				mockResult3.getValue(); result = value3;
				/*findOne1.isComplete(); result = true;
				findOne2.isComplete(); result = true;
				findOne3.isComplete(); result = true;
				findOne1.getResults(); result = new Result[] { mockResult1 };
				findOne2.getResults(); result = new Result[] { mockResult2 };
				findOne3.getResults(); result = new Result[] { mockResult3 };
				findOne1.getChildren(); result = new Executable [] { };
				findOne2.getChildren(); result = new Executable [] { findOne3 };
				findOne3.getChildren(); result = new Executable [] { };
				
				page.isComplete(); result  = true;
				setField(page, "findOneExecutables", new FindOneExecutable[] { findOne1, findOne2 });*/
			}
		};
		/*
		assertEquals("Page contains key for name 1", true, pageExecutable.containsKey(name1));
		assertEquals("Page contains key for name 2", true, pageExecutable.containsKey(name2));
		assertEquals("Page contains key for name 3", true, pageExecutable.containsKey(name3));
		assertEquals("Page contains value for name 1", value1, pageExecutable.get(name1));
		assertEquals("Page contains value for name 2", value2, pageExecutable.get(name2));
		assertEquals("Page contains value for name 3", value3, pageExecutable.get(name3));
		assertEquals("Page contains key for name 1", true, findOne1.containsKey(name1));
		assertEquals("Page contains key for name 2", true, findOne1.containsKey(name2));
		assertEquals("Page contains key for name 3", true, findOne1.containsKey(name3));*/
	}
}
