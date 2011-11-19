package net.caustic.json;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import net.caustic.json.JsonMEParser;
import net.caustic.json.JsonParser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JsonObjectTest {
	private final Class<JsonParser> klass;
	private JsonParser parser;
	
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";
	
	public JsonObjectTest(Class<JsonParser> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static List<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{	JsonMEParser.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		parser = klass.newInstance();
	}
		
	@Test
	public void testGetJSONArray() throws Exception {
		assertEquals(4, parser.newObject(jsonStringComplexObject).getJsonArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertTrue(parser.newObject(jsonStringComplexObject).isJsonArray("array"));
		assertFalse(parser.newObject(jsonStringComplexObject).isJsonArray("object"));
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, parser.newObject(jsonStringComplexObject).getJsonObject("object").length());
	}
	
	@Test
	public void testGetJSONObjectAsString() throws Exception {
		assertEquals("Should be able to pull the raw text of a JSON Object using getString(key).",
				parser.newObject(jsonStringSimpleObject).toString().length(),
				parser.newObject(parser.newObject(jsonStringComplexObject).getString("object")).toString().length());
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertFalse(parser.newObject(jsonStringComplexObject).isJsonObject("array"));
		assertTrue(parser.newObject(jsonStringComplexObject).isJsonObject("object"));
	}

	@Test
	public void testGetString() throws Exception {
		assertEquals("value", parser.newObject(jsonStringSimpleObject).getString("string"));
	}

	@Test
	public void testIsString() throws Exception {
		assertTrue(parser.newObject(jsonStringSimpleObject).isString("string"));
		assertTrue(parser.newObject(jsonStringSimpleObject).isString("int"));
		assertTrue(parser.newObject(jsonStringSimpleObject).isString("boolean"));
	}

	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, parser.newObject(jsonStringSimpleObject).getInt("int"));
	}

	@Test
	public void testIsInt() throws Exception {
		assertFalse(parser.newObject(jsonStringSimpleObject).isInt("string"));
		assertTrue(parser.newObject(jsonStringSimpleObject).isInt("int"));
		assertFalse(parser.newObject(jsonStringSimpleObject).isInt("boolean"));
	}

	@Test
	public void testGetBoolean() throws Exception {
		assertEquals(true, parser.newObject(jsonStringSimpleObject).getBoolean("boolean"));
	}

	@Test
	public void testIsBoolean() throws Exception {
		assertFalse(parser.newObject(jsonStringSimpleObject).isBoolean("string"));
		assertFalse(parser.newObject(jsonStringSimpleObject).isBoolean("int"));
		assertTrue(parser.newObject(jsonStringSimpleObject).isBoolean("boolean"));
	}

	@Test
	public void testHas() throws Exception {
		assertTrue(parser.newObject(jsonStringSimpleObject).has("string"));
		assertTrue(parser.newObject(jsonStringSimpleObject).has("int"));
		assertTrue(parser.newObject(jsonStringSimpleObject).has("boolean"));
		assertTrue(parser.newObject(jsonStringSimpleObject).has("null"));
	}

	@Test
	public void testIsNull() throws Exception {
		assertFalse(parser.newObject(jsonStringSimpleObject).isNull("string"));
		assertFalse(parser.newObject(jsonStringSimpleObject).isNull("int"));
		assertFalse(parser.newObject(jsonStringSimpleObject).isNull("boolean"));
		assertTrue(parser.newObject(jsonStringSimpleObject).isNull("null"));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(4, parser.newObject(jsonStringSimpleObject).length());
	}

}
