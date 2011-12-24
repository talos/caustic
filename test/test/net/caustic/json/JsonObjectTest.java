package net.caustic.json;

import static org.junit.Assert.*;

import org.json.me.JSONObject;
import org.junit.Test;

public class JsonObjectTest {
	
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";
	
		
	@Test
	public void testGetJSONArray() throws Exception {
		assertEquals(4, new JSONObject(jsonStringComplexObject).getJSONArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertFalse(new JSONObject(jsonStringComplexObject).optJSONArray("array") == null);
		assertTrue(new JSONObject(jsonStringComplexObject).optJSONArray("object") == null);
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, new JSONObject(jsonStringComplexObject).getJSONObject("object").length());
	}
	
	@Test
	public void testGetJSONObjectAsString() throws Exception {
		assertEquals("Should be able to pull the raw text of a JSON Object using getString(key).",
				new JSONObject(jsonStringSimpleObject).toString().length(),
				new JSONObject(new JSONObject(jsonStringComplexObject).getString("object")).toString().length());
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertTrue(new JSONObject(jsonStringComplexObject).optJSONObject("array") == null);
		assertFalse(new JSONObject(jsonStringComplexObject).optJSONObject("object") == null);
	}

	@Test
	public void testGetString() throws Exception {
		assertEquals("value", new JSONObject(jsonStringSimpleObject).getString("string"));
	}

	@Test
	public void testIsString() throws Exception {
		assertFalse(new JSONObject(jsonStringSimpleObject).optString("string") == null);
		assertFalse(new JSONObject(jsonStringSimpleObject).optString("int") == null);
		assertFalse(new JSONObject(jsonStringSimpleObject).optString("boolean") == null);
	}

	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, new JSONObject(jsonStringSimpleObject).getInt("int"));
	}

	@Test
	public void testIsInt() throws Exception {
		assertTrue(new JSONObject(jsonStringSimpleObject).optInt("string") == 0);
		assertFalse(new JSONObject(jsonStringSimpleObject).optInt("int") == 0);
		assertTrue(new JSONObject(jsonStringSimpleObject).optInt("boolean") == 0);
	}

	@Test
	public void testGetBoolean() throws Exception {
		assertEquals(true, new JSONObject(jsonStringSimpleObject).getBoolean("boolean"));
	}

	@Test
	public void testIsBoolean() throws Exception {
		assertTrue(new JSONObject(jsonStringSimpleObject).optBoolean("string") == false);
		assertTrue(new JSONObject(jsonStringSimpleObject).optBoolean("int") == false);
		assertFalse(new JSONObject(jsonStringSimpleObject).optBoolean("boolean") == false);
	}

	@Test
	public void testHas() throws Exception {
		assertTrue(new JSONObject(jsonStringSimpleObject).has("string"));
		assertTrue(new JSONObject(jsonStringSimpleObject).has("int"));
		assertTrue(new JSONObject(jsonStringSimpleObject).has("boolean"));
		assertTrue(new JSONObject(jsonStringSimpleObject).has("null"));
	}

	@Test
	public void testIsNull() throws Exception {
		assertFalse(new JSONObject(jsonStringSimpleObject).isNull("string"));
		assertFalse(new JSONObject(jsonStringSimpleObject).isNull("int"));
		assertFalse(new JSONObject(jsonStringSimpleObject).isNull("boolean"));
		assertTrue(new JSONObject(jsonStringSimpleObject).isNull("null"));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(4, new JSONObject(jsonStringSimpleObject).length());
	}

}
