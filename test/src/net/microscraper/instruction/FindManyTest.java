package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.interfaces.json.JSONInterfaceObject;

import org.junit.Test;

public class FindManyTest {
	@Mocked JSONInterfaceObject obj;
	@Tested FindMany findMany;
	
	private static final int NON_DEFAULT = 10;
	
	@Test
	public void testGetMinMatchDefault() throws Exception {
		findMany = new FindMany(obj);
		assertEquals(FindMany.DEFAULT_MIN_MATCH, findMany.getMinMatch());
	}
	
	@Test
	public void testGetMinMatchAssigned() throws Exception {
		new NonStrictExpectations() {{
			obj.getInt(FindMany.MIN_MATCH); result = NON_DEFAULT;
			obj.has(FindMany.MIN_MATCH); result = true;
		}};
		findMany = new FindMany(obj);
		assertEquals(NON_DEFAULT, findMany.getMinMatch());
	}

	@Test
	public void testGetMaxMatchDefault() throws Exception {
		findMany = new FindMany(obj);
		assertEquals(FindMany.DEFAULT_MAX_MATCH, findMany.getMaxMatch());
	}

	@Test
	public void testGetMaxMatchAssigned() throws Exception {
		new NonStrictExpectations() {{
			obj.getInt(FindMany.MAX_MATCH); result = NON_DEFAULT;
			obj.has(FindMany.MAX_MATCH); result = true;
		}};
		findMany = new FindMany(obj);
		assertEquals(NON_DEFAULT, findMany.getMaxMatch());
	}
}
