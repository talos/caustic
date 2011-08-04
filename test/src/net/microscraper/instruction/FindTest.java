package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.interfaces.json.JSONInterfaceArray;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.test.TestUtils;

import org.junit.Test;

public class FindTest {

	@Mocked JSONInterfaceObject obj;
	@Tested Find find;
	
	@Test
	public void testGetReplacementDefault() throws Exception {
		find = new Find(obj);
		assertEquals(Find.DEFAULT_REPLACEMENT, find.getReplacement().toString());
	}
	
	@Test
	public void testGetReplacement() throws Exception {
		final String replacement = "replacment {{string}}";
		new NonStrictExpectations() {{
			obj.getString(Find.REPLACEMENT); result = replacement;
			obj.has(Find.REPLACEMENT); result = true;
		}};
		find = new Find(obj);
		assertEquals(replacement, find.getReplacement().toString());
	}
	
	@Test
	public void testGetTestsDefault() throws Exception {
		find = new Find(obj);
		assertArrayEquals(Find.DEFAULT_TESTS, find.getTests());
	}
	
	@Test
	public void testGetTests() throws Exception {
		new NonStrictExpectations() {
			JSONInterfaceArray tests;
			JSONInterfaceObject test1, test2;
			{
				tests.length(); result = 2;
				tests.getJSONObject(0); result = test1;
				tests.getJSONObject(1); result = test2;
				obj.getJSONArray(Find.TESTS); result = tests;
				obj.has(Find.TESTS); result = true;
			}
		};
		
		find = new Find(obj);
		assertEquals(2, find.getTests().length);
	}

}
