package net.microscraper.json;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Load;
import static net.microscraper.json.JsonDeserializer.*;

import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	
	private @Mocked JsonObject obj;
	private @Mocked JsonParser parser;
	private JsonDeserializer deserializer;
	
	private static final String googleString = "http://www.google.com";
	private static final String patternString = ".*";
	
	@Before
	public void setUp() throws Exception {
		deserializer = new JsonDeserializer(parser);
	}
	
	@Test
	public void testDeserializeSimpleLoad() throws Exception {
		final String jsonString = "{" + 
				"\"" + URL + "\" : \"" + googleString + "\"" +
				"}";
		new NonStrictExpectations() {
			{
				parser.parse(jsonString); result = obj;
				obj.getString(URL); result = googleString;
			}
		};
		Load load = deserializer.deserializeLoad(jsonString);
		new Verifications() {
			{
				obj.getString(URL);
			}
		};
	}

	@Test
	public void testDeserializeSimpleFind() throws Exception {
		final String jsonString = "{" + 
				"\"" + PATTERN + "\" : \"" + patternString + "\"" +
				"}";
		new NonStrictExpectations() {
			JsonObject obj;
			{
				parser.parse(jsonString); result = obj;
				obj.getString(PATTERN); result = patternString;
			}
		};
		Find find = deserializer.deserializeFind(jsonString);
		new Verifications() {
			{
				obj.getString(PATTERN);
			}
		};
	}

}
