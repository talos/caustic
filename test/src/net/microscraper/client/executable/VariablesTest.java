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
	
	private Variables variables;
	
	private static final String findOneName = "find one name";
	private static final String findOneInput = "The quick brown fox jumped over the lazy dog.";
	private static final String findOneSearch = "dog";
	private static final String findOneReplace = "$0";
	
	@Mocked private Interfaces interfaces;
	@Mocked({"getName", "getFindOnes", "getFindManys"}) private FindOne findOne;
	@Mocked private Page page;
	@Mocked private Scraper scraper;
	
	@Mocked private Result sourceResult;
	@Mocked private URIInterface location;
	
	private FindOneExecutable findOneExecutable;
	private ScraperExecutable pageExecutable;
	private ScraperExecutable spawnedScraperExecutable;
		
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
		Log log = new Log();
		log.register(new SystemLogInterface());
		
		new NonStrictExpectations() {
			{
				
				sourceResult.getValue(); result = findOneInput;
				sourceResult.getUri();   result = location;
				
				findOne.getName(); result = new MustacheTemplate(findOneName);
				findOne.getFindOnes(); result = new FindOne[] {};
				findOne.getFindManys(); result = new FindMany[] {};
				setField(findOne, "location", location);
				//setField(findOne, "name", new MustacheTemplate(findOneName));
				setField(findOne, "pattern", new MustacheTemplate(findOneSearch));
				setField(findOne, "replacement", new MustacheTemplate(findOneReplace));
				
				setField(interfaces, "regexpCompiler", new JavaUtilRegexInterface());
			}
		};

		findOneExecutable = new FindOneExecutable(interfaces, findOne, variables, sourceResult);
		pageExecutable = new PageExecutable(interfaces, page, variables, sourceResult);
		spawnedScraperExecutable = new SpawnedScraperExecutable(interfaces, scraper, variables, sourceResult);

		findOneExecutable.run();
	}
	
	/**
	 * {@link FindOneExecutable} returns its one value for its name.
	 * @throws Exception
	 */
	@Test
	public void testGetLocalValue() throws Exception {	
		assertEquals(findOneSearch, findOneExecutable.get(findOneName));		
	}
	
	/**
	 * {@link FindOneExecutable} contains its name as a key.
	 */
	
	@Test
	public void testContainsLocalKey() {		
		assertEquals(true, findOneExecutable.containsKey(findOneName));
	}
	
	/**
	 * {@link FindOneExecutable} returns its child's value for its child's name.
	 * @throws Exception
	 */
	/*
	@Test
	public void testGetExistentChildValue() throws Exception {		
		assertEquals(result2.getValue(), findOne1.get(result2.getName()));
		assertEquals(result2.getValue(), scraper1.get(result2.getName()));
		assertEquals(result2.getValue(), scraper3.get(result2.getName()));
	}
	*/
	/**
	 * {@link FindOneExecutable} contains its child's name as a key.
	 */
	/*
	@Test
	public void testContainsChildKey() {		
		assertEquals(true, findOne1.containsKey(result2.getName()));
		assertEquals(true, scraper1.containsKey(result2.getName()));
		assertEquals(true, scraper3.containsKey(result2.getName()));
	}*/
	
	/**
	 * {@link FindOneExecutable} throws {@link MissingVariableException} when {@link FindOneExecutable#get}
	 * is called for a name that is not its or a child's.
	 */
	/*
	@Test(expected = MissingVariableException.class)
	public void testGetNonexistentChildValue() throws Exception {
		findOne1.get(result1.getName());
	}*/
	
	
	/**
	 * {@link FindOneExecutable} does not contain other names as a key.
	 */
	/*
	@Test
	public void testDoesNotContainParentKey() {
		assertEquals(false, findOne1.containsKey("random"));
		assertEquals(false, scraper1.containsKey("random"));
	}*/
}
