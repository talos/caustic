package net.microscraper.client.executable;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Injectable;
import mockit.MockClass;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Client;
import net.microscraper.client.Log;
import net.microscraper.client.MissingVariableException;
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
import net.microscraper.server.instruction.Scraper;

import org.junit.Before;
import org.junit.Test;

public class VariablesTest {
	
	@Mocked private Interfaces interfaces;
	/*private Variables variables;
	
	private static final String findOneName = "find one name";
	private static final String findOneInput = "The quick brown fox jumped over the lazy dog.";
	private static final String findOneSearch = "dog";
	private static final String findOneReplace = "$0";
	
	@Mocked private Page page;
	@Mocked private Scraper scraper;
	*/
	//@Mocked private Result sourceResult;
	//@Mocked private URIInterface location;
	
	// private FindOneExecutable findOneExecutable1, findOneExecutable2, findOneExecutable3, findOneExecutable4;
	// private ScraperExecutable pageExecutable;
	//private ScraperExecutable spawnedScraperExecutable;
	
	/*
	@Mocked Result result1, result2, result3;
	
	@Mocked({"isComplete", "getResults", "getChildren"}) FindOneExecutable findOne1, findOne2, findOne3;
	@Mocked({"isComplete", "getResults", "getChildren"}) ScraperExecutable scraper1, scraper2, scraper3;
	*/
	
	/**
	 * Set up the {@link Variable} implementing {@link Executable}s before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		//Log log = new Log();
		//log.register(new SystemLogInterface());
		
		new NonStrictExpectations() {
			@Mocked({"getName", "getFindOnes", "getFindManys"}) private FindOne findOne1, findOne2, findOne3, findOne4;

			{
				
				//sourceResult.getValue(); result = findOneInput;
				//sourceResult.getUri();   result = location;
								
				setField(interfaces, "regexpCompiler", new JavaUtilRegexInterface());
			}
		};

		//findOneExecutable = new FindOneExecutable(interfaces, findOne, variables, sourceResult);
		//pageExecutable = new PageExecutable(interfaces, page, variables, sourceResult);
		//spawnedScraperExecutable = new SpawnedScraperExecutable(interfaces, scraper, variables, sourceResult);

	}
	
	/**
	 * {@link FindOneExecutable} returns its one value for its name.
	 * @throws Exception
	 */
	@Test
	public void testFindOneLocalValue(
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable simpleExecutable
			) throws Exception {
		final String name = "name";
		final String value = "value";
		
		new NonStrictExpectations() {
			@Mocked Result mockResult;
			{
				/*
				sourceResult.getValue(); result = findOneInput;
				sourceResult.getUri();   result = location;
				
				findOne.getName(); result = new MustacheTemplate(findOneName);
				findOne.getFindOnes(); result = new FindOne[] {};
				findOne.getFindManys(); result = new FindMany[] {};
				setField(findOne, "location", location);
				//setField(findOne, "name", new MustacheTemplate(findOneName));
				setField(findOne, "pattern", new MustacheTemplate(findOneSearch));
				setField(findOne, "replacement", new MustacheTemplate(findOneReplace));
				
				setField(interfaces, "regexpCompiler", new JavaUtilRegexInterface());*/
				
				mockResult.getName(); result = name;
				mockResult.getValue(); result = value;
				simpleExecutable.isComplete(); result = true;
				simpleExecutable.getResults(); result = new Result[] { mockResult };
				simpleExecutable.getChildren(); result = new Executable [] {};
			}
		};
		
		//simpleExecutable.run();
		
		assertEquals("Contains key for name..", true, simpleExecutable.containsKey(name));
		assertEquals("Retrives value for name.", value, simpleExecutable.get(name));
	}
	
	
	/**
	 * {@link FindOneExecutable} returns its child's value for its child's name.
	 * @throws Exception
	 */
	@Test(expected = MissingVariableException.class)
	public void testFindOneChildValue(
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable parentFindOne,
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable childFindOne
			) throws Exception {
		
		final String parentName = "parentName";
		final String parentValue = "parentValue";
		final String childName = "childName";
		final String childValue = "childValue";
		
		new NonStrictExpectations() {
			@Mocked Result parentMockResult, childMockResult;
			{
				parentMockResult.getName(); result = parentName;
				parentMockResult.getValue(); result = parentValue;

				childMockResult.getName(); result = childName;
				childMockResult.getValue(); result = childValue;
				
				parentFindOne.isComplete(); result = true;
				childFindOne.isComplete(); result = true;
				
				parentFindOne.getResults(); result = new Result[] { parentMockResult };
				childFindOne.getResults(); result = new Result[] { childMockResult };
				
				parentFindOne.getChildren(); result = new Executable [] { childFindOne };
				childFindOne.getChildren(); result = new Executable [] {};
			}
		};
		
		//simpleExecutable.run();
		
		assertEquals("Parent contains key for child name.", true, parentFindOne.containsKey(parentName));
		assertEquals("Child does not contain key for parent name.", false, childFindOne.containsKey(parentName));
		assertEquals("Parent retrieves value for child name.", childValue, parentFindOne.get(childName));
		childFindOne.get(parentName); // should throw MissingVariableException
		
	}
	
	/**
	 * {@link PageExecutable} works as {@link Variables} for its child {@link FindOneExecutables}.
	 */
	@Test
	public void testPageChildren  (
			//@Mocked ({"isComplete", "getChildren"}) final PageExecutable pageExecutable,
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable findOne1Executable,
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable findOne2Executable,
			@Mocked ({"isComplete", "getResults", "getChildren"}) final FindOneExecutable findOne3Executable
			) throws Exception {
		
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
		
		assertEquals("Page contains key for name 1", true, pageExecutable.containsKey(name1));
		assertEquals("Page contains key for name 2", true, pageExecutable.containsKey(name2));
		assertEquals("Page contains key for name 3", true, pageExecutable.containsKey(name3));
		assertEquals("Page contains value for name 1", value1, pageExecutable.get(name1));
		assertEquals("Page contains value for name 2", value2, pageExecutable.get(name2));
		assertEquals("Page contains value for name 3", value3, pageExecutable.get(name3));
		assertEquals("Page contains key for name 1", true, findOne1.containsKey(name1));
		assertEquals("Page contains key for name 2", true, findOne1.containsKey(name2));
		assertEquals("Page contains key for name 3", true, findOne1.containsKey(name3));
	}
}
