package net.microscraper.json;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.file.FileLoader;
import net.microscraper.json.JsonParser;
import net.microscraper.json.JsonObject;
import net.microscraper.json.JsonMEParser;
import net.microscraper.regexp.JakartaRegexpCompiler;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

public abstract class JsonObjectTest {
	private JsonParser jsonParser;
	
	private @Mocked FileLoader fileLoader;
	private @Mocked Browser browser;
	private @Mocked Uri location;
	private String path = "path";
	
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";

	
	
	
	@Before
	public void setUp() throws Exception {
		jsonParser = getJSONParser();
		new NonStrictExpectations() {{
			location.toString(); result = path;
		}};
	}
	
	protected abstract JsonParser getJSONParser();

	private JsonObject getObject(final String jsonString) throws Exception {
		new NonStrictExpectations() {{
			fileLoader.load(location); result = jsonString;
		}};
		
		return jsonParser.load(location);
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
