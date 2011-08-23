package net.microscraper.json;

import static org.junit.Assert.*;

import net.microscraper.json.JsonParser;
import net.microscraper.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

public abstract class JsonObjectTest {
	private JsonParser jsonParser;
	
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";
	
	
	@Before
	public void setUp() throws Exception {
		jsonParser = getJSONParser();
	}
	
	protected abstract JsonParser getJSONParser();

	private JsonObject getObject(String jsonString) throws Exception {
		return jsonParser.parse(jsonString);
	}
	
	@Test
	public void testGetJSONArray() throws Exception {
		assertEquals(4, getObject(jsonStringComplexObject).getJsonArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertTrue(getObject(jsonStringComplexObject).isJsonArray("array"));
		assertFalse(getObject(jsonStringComplexObject).isJsonArray("object"));
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, getObject(jsonStringComplexObject).getJsonObject("object").length());
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertFalse(getObject(jsonStringComplexObject).isJsonObject("array"));
		assertTrue(getObject(jsonStringComplexObject).isJsonObject("object"));
	}

	@Test
	public void testGetString() throws Exception {
		assertEquals("value", getObject(jsonStringSimpleObject).getString("string"));
	}

	@Test
	public void testIsString() throws Exception {
		assertTrue(getObject(jsonStringSimpleObject).isString("string"));
		assertTrue(getObject(jsonStringSimpleObject).isString("int"));
		assertTrue(getObject(jsonStringSimpleObject).isString("boolean"));
	}

	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, getObject(jsonStringSimpleObject).getInt("int"));
	}

	@Test
	public void testIsInt() throws Exception {
		assertFalse(getObject(jsonStringSimpleObject).isInt("string"));
		assertTrue(getObject(jsonStringSimpleObject).isInt("int"));
		assertFalse(getObject(jsonStringSimpleObject).isInt("boolean"));
	}

	@Test
	public void testGetBoolean() throws Exception {
		assertEquals(true, getObject(jsonStringSimpleObject).getBoolean("boolean"));
	}

	@Test
	public void testIsBoolean() throws Exception {
		assertFalse(getObject(jsonStringSimpleObject).isBoolean("string"));
		assertFalse(getObject(jsonStringSimpleObject).isBoolean("int"));
		assertTrue(getObject(jsonStringSimpleObject).isBoolean("boolean"));
	}

	@Test
	public void testHas() throws Exception {
		assertTrue(getObject(jsonStringSimpleObject).has("string"));
		assertTrue(getObject(jsonStringSimpleObject).has("int"));
		assertTrue(getObject(jsonStringSimpleObject).has("boolean"));
		assertTrue(getObject(jsonStringSimpleObject).has("null"));
	}

	@Test
	public void testIsNull() throws Exception {
		assertFalse(getObject(jsonStringSimpleObject).isNull("string"));
		assertFalse(getObject(jsonStringSimpleObject).isNull("int"));
		assertFalse(getObject(jsonStringSimpleObject).isNull("boolean"));
		assertTrue(getObject(jsonStringSimpleObject).isNull("null"));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(4, getObject(jsonStringSimpleObject).length());
	}

}
