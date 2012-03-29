package net.caustic.json;

import static org.junit.Assert.*;

import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.junit.Test;

public class JSONValueTest {
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";
	
	@Test
	public void testStringOnlyTypeWrapped () throws Exception {
		assertTrue(JSONValue.deserialize("\"this is a string\"", false).isString);
	}
	
	@Test
	public void testStringOnlyValue() throws Exception {
		assertEquals("this is a string", JSONValue.deserialize("\"this is a string\"", false).value);
	}

	@Test
	public void testStringOnlyToStringWrapped() throws Exception {
		assertEquals("\"this is a string\"", JSONValue.deserialize("\"this is a string\"", false).toString());
	}

	@Test
	public void testStringOnlyToStringWrappedArtificial() throws Exception {
		assertEquals("\"this is a string\"", JSONValue.wrapString("this is a string").toString());
	}
	
	public void testUnwrappedGetsWrapped() throws Exception {
		assertEquals("\"this is a string\"", JSONValue.deserialize("this is a string", true).toString());
	}
	
	@Test(expected=JSONException.class)
	public void testUnwrappedThrowsException() throws Exception {
		JSONValue.deserialize("unwrapped string", false);
	}
	
	@Test(expected=JSONException.class)
	public void testBadArray() throws Exception {
		JSONValue.deserialize("[\"bad", false);
	}

	@Test(expected=JSONException.class)
	public void testBadObject() throws Exception {
		JSONValue.deserialize("{\"foo\", \"bar\"}", false);
	}
	
	@Test
	public void testIsArray() throws Exception {
		assertTrue(JSONValue.deserialize(jsonStringSimpleArray, false).isArray);
	}

	@Test
	public void testIsObject() throws Exception {
		assertTrue(JSONValue.deserialize(jsonStringSimpleObject, false).isObject);
	}
	
	@Test
	public void testGetJSONArray() throws Exception {
		assertEquals(4, ((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).getJSONArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).optJSONArray("array") == null);
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).optJSONArray("object") == null);
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, ((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).getJSONObject("object").length());
	}
	
	@Test
	public void testGetJSONObjectAsString() throws Exception {
		assertEquals("Should be able to pull the raw text of a JSON Object using getString(key).",
				((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).toString().length(),
				(JSONValue.deserialize(((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).getString("object"), false).toString().length()));
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).optJSONObject("array") == null);
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringComplexObject, false).value).optJSONObject("object") == null);
	}

	@Test
	public void testGetString() throws Exception {
		assertEquals("value", ((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).getString("string"));
	}

	@Test
	public void testIsString() throws Exception {
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optString("string") == null);
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optString("int") == null);
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optString("boolean") == null);
	}

	@Test
	public void testGetInt() throws Exception {
		assertEquals(1, ((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).getInt("int"));
	}

	@Test
	public void testIsInt() throws Exception {
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optInt("string") == 0);
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optInt("int") == 0);
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optInt("boolean") == 0);
	}

	@Test
	public void testGetBoolean() throws Exception {
		assertEquals(true, ((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).getBoolean("boolean"));
	}

	@Test
	public void testIsBoolean() throws Exception {
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optBoolean("string") == false);
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optBoolean("int") == false);
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).optBoolean("boolean") == false);
	}

	@Test
	public void testHas() throws Exception {
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).has("string"));
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).has("int"));
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).has("boolean"));
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).has("null"));
	}

	@Test
	public void testIsNull() throws Exception {
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).isNull("string"));
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).isNull("int"));
		assertFalse(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).isNull("boolean"));
		assertTrue(((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).isNull("null"));
	}

	@Test
	public void testLength() throws Exception {
		assertEquals(4, ((JSONObject) JSONValue.deserialize(jsonStringSimpleObject, false).value).length());
	}

}
