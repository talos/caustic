package net.microscraper.interfaces.json;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.uri.URIInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JSONInterfaceObjectTest {
	private final Class<JSONInterface> klass;
	private JSONInterface jsonInterface;
	
	private @Mocked FileLoader fileLoader;
	private @Mocked Browser browser;
	private @Mocked URIInterface location;
	private String path = "path";
	
	private static final String jsonStringSimpleObject = 
			"{ \"string\" : \"value\", \"int\" : 1, \"boolean\" : true, \"null\" : null }";
	private static final String jsonStringSimpleArray = 
			"[ \"string\", 1, true , null ]";
	private static final String jsonStringComplexObject =
			"{ \"object\" : " + jsonStringSimpleObject + ", \"array\" : " + jsonStringSimpleArray + " }";
	/*private static final String jsonStringComplexArray =
			"[ " + jsonStringSimpleObject + ", " + jsonStringSimpleArray + "]";
	*/
	public JSONInterfaceObjectTest(final Class<JSONInterface> klass) throws Exception {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class[]> implementations() {
		return Arrays.asList(new Class[][] {
				{ JSONME.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		jsonInterface = klass.getConstructor(FileLoader.class, Browser.class)
				 .newInstance(fileLoader, browser);
		new NonStrictExpectations() {{
				location.isFile(); result = true;
				location.getSchemeSpecificPart(); result = path;
		}};
	}
	
	private JSONInterfaceObject getObject(final String jsonString) throws Exception {
		new NonStrictExpectations() {{
			fileLoader.load(path); result = jsonString;
		}};
		
		return jsonInterface.load(location);
	}
	
	@Test
	public void testGetJSONArray() throws Exception {
		assertEquals(4, getObject(jsonStringComplexObject).getJSONArray("array").length());
	}

	@Test
	public void testIsJSONArray() throws Exception {
		assertTrue(getObject(jsonStringComplexObject).isJSONArray("array"));
		assertFalse(getObject(jsonStringComplexObject).isJSONArray("object"));
	}

	@Test
	public void testGetJSONObject() throws Exception {
		assertEquals(4, getObject(jsonStringComplexObject).getJSONObject("object").length());
	}

	@Test
	public void testIsJSONObject() throws Exception {
		assertFalse(getObject(jsonStringComplexObject).isJSONObject("array"));
		assertTrue(getObject(jsonStringComplexObject).isJSONObject("object"));
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
