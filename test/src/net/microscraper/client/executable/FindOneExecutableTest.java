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
import net.microscraper.client.impl.FileLoader;
import net.microscraper.client.impl.JSONME;
import net.microscraper.client.impl.JavaNetBrowser;
import net.microscraper.client.impl.JavaNetInterface;
import net.microscraper.client.impl.JavaUtilRegexInterface;
import net.microscraper.client.impl.SystemLogInterface;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.resource.FindOne;

import org.junit.Before;
import org.junit.Test;

public class FindOneExecutableTest {
	
	private static final String parentName = "parent";
	private static final String parentValue = "parent value";
	private static final String childName = "child";
	private static final String childValue = "child value";
	
	@Mocked({"isComplete", "getResults", "getChildren"}) FindOneExecutable findOneExecutableParent, findOneExecutableChild;
	
	/**
	 * Set up the {@link FindOneExecutable}s before each test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		//Log log = new Log();
		//log.register(new SystemLogInterface());
		
		new NonStrictExpectations() {
			Result mockResultParent, mockResultChild;
			{
				mockResultParent.getName(); result = parentName;
				mockResultChild.getName(); result = childName;
				
				mockResultParent.getValue(); result = parentValue;
				mockResultChild.getValue(); result = childValue;
				
				findOneExecutableParent.isComplete(); result = true;
				findOneExecutableChild.isComplete(); result = true;
				
				findOneExecutableParent.getResults(); result = new Result[] { mockResultParent };
				findOneExecutableChild.getResults();  result = new Result[] { mockResultChild };
				
				findOneExecutableParent.getChildren(); result = new Executable[] { findOneExecutableChild };
				findOneExecutableChild.getChildren();  result = new Executable[] {};
			}
		};
	}
	
	/**
	 * {@link FindOneExecutable} returns its one value for its name.
	 * @throws Exception
	 */
	@Test
	public void testGetLocalValue() throws Exception {
		assertEquals(parentValue, findOneExecutableParent.get(parentName));
		assertEquals(childValue, findOneExecutableChild.get(childName));		
	}
	
	/**
	 * {@link FindOneExecutable} returns its child's value for its child's name.
	 * @throws Exception
	 */
	@Test
	public void testGetExistentChildValue() throws Exception {
		assertEquals(childValue, findOneExecutableParent.get(childName));
	}
	
	/**
	 * {@link FindOneExecutable} throws {@link MissingVariableException} when {@link FindOneExecutable#get}
	 * is called for a name that is not its or a child's.
	 */
	@Test(expected = MissingVariableException.class)
	public void testGetNonexistentChildValue() throws Exception {
		findOneExecutableChild.get(parentName);
	}
	
	/**
	 * {@link FindOneExecutable} contains its name as a key.
	 */
	@Test
	public void testContainsLocalKey() {
		assertEquals(true, findOneExecutableParent.containsKey(parentName));
		assertEquals(true, findOneExecutableChild.containsKey(childName));
	}
	
	/**
	 * {@link FindOneExecutable} contains its child's name as a key.
	 */
	@Test
	public void testContainsChildKey() {
		assertEquals(true, findOneExecutableParent.containsKey(childName));
	}
	
	/**
	 * {@link FindOneExecutable} does not contain other names as a key.
	 */
	@Test
	public void testDoesNotContainParentKey() {
		assertEquals(false, findOneExecutableChild.containsKey(parentName));
		assertEquals(false, findOneExecutableChild.containsKey("random"));
	}
}
