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
		assertEquals(4, parser.parse(jsonStringComplexObject).getJsonArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertTrue(parser.parse(jsonStringComplexObject).isJsonArray("array"));
		assertFalse(parser.parse(jsonStringComplexObject).isJsonArray("object"));
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, parser.parse(jsonStringComplexObject).getJsonObject("object").length());
	}
	
	@Test
	public void testGetJSONObjectAsString() throws Exception {
		assertEquals("Should be able to pull the raw text of a JSON Object using getString(key).",
				parser.parse(jsonStringSimpleObject).toString().length(),
				parser.parse(parser.parse(jsonStringComplexObject).getString("object")).toString().length());
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertFalse(parser.parse(jsonStringComplexObject).isJsonObject("array"));
		assertTrue(parser.parse(jsonStringComplexObject).isJsonObject("object"));
	}

	@Test
	public void testGetString() throws Exception {
		assertEquals("value", parser.parse(jsonStringSimpleObject).getString("string"));
	}

	@Test
	public void testIsString() throws Exception {
		assertTrue(parser.parse(jsonStringSimpleObject).isString("string"));
		assertTrue(parser.parse(jsonStringSimpleObject).isString("int"));
		assertTrue(parser.parse(jsonStringSimpleObject).isString("boolean"));
	}

	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, parser.parse(jsonStringSimpleObject).getInt("int"));
	}

	@Test
	public void testIsInt() throws Exception {
		assertFalse(parser.parse(jsonStringSimpleObject).isInt("string"));
		assertTrue(parser.parse(jsonStringSimpleObject).isInt("int"));
		assertFalse(parser.parse(jsonStringSimpleObject).isInt("boolean"));
	}

	@Test
	public void testGetBoolean() throws Exception {
		assertEquals(true, parser.parse(jsonStringSimpleObject).getBoolean("boolean"));
	}

	@Test
	public void testIsBoolean() throws Exception {
		assertFalse(parser.parse(jsonStringSimpleObject).isBoolean("string"));
		assertFalse(parser.parse(jsonStringSimpleObject).isBoolean("int"));
		assertTrue(parser.parse(jsonStringSimpleObject).isBoolean("boolean"));
	}

	@Test
	public void testHas() throws Exception {
		assertTrue(parser.parse(jsonStringSimpleObject).has("string"));
		assertTrue(parser.parse(jsonStringSimpleObject).has("int"));
		assertTrue(parser.parse(jsonStringSimpleObject).has("boolean"));
		assertTrue(parser.parse(jsonStringSimpleObject).has("null"));
	}

	@Test
	public void testIsNull() throws Exception {
		assertFalse(parser.parse(jsonStringSimpleObject).isNull("string"));
		assertFalse(parser.parse(jsonStringSimpleObject).isNull("int"));
		assertFalse(parser.parse(jsonStringSimpleObject).isNull("boolean"));
		assertTrue(parser.parse(jsonStringSimpleObject).isNull("null"));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(4, parser.parse(jsonStringSimpleObject).length());
	}

}
