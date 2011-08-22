package net.microscraper.json;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Encoder;
import static net.microscraper.json.JsonDeserializer.*;
import static net.microscraper.util.TestUtils.randomInt;
import static net.microscraper.util.TestUtils.randomString;

import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	private final String loadJson = randomString();
	private final String findJson = randomString();
	private final String emptyJson = "";
	private @Mocked JsonObject loadObj, findObj, emptyObj;
	private @Mocked JsonParser parser;
	private @Mocked RegexpCompiler compiler;
	private @Mocked Browser browser;
	private @Mocked Encoder encoder;
	
	private JsonDeserializer deserializer;
	
	private static final String googleString = "http://www.google.com";
	private static final String patternString = ".*";
	
	@Before
	public void setUp() throws Exception {
		deserializer = new JsonDeserializer(parser, compiler, browser, encoder);
		new NonStrictExpectations() {{
			parser.parse(loadJson); result = loadObj;
			loadObj.has(LOAD); result = true;
			loadObj.getString(LOAD); result = googleString;
			
			parser.parse(findJson); result = findObj;
			findObj.has(FIND); result = true;
			findObj.getString(FIND); result = patternString;
			
			parser.parse(emptyJson); result = emptyObj;
		}};
	}
	
	@Test
	public void testDeserializeSimpleLoad() throws Exception {
		deserializer.deserializeJson(loadJson);
	}

	@Test
	public void testDeserializeSimpleFind() throws Exception {
		deserializer.deserializeJson(findJson);
	}

	@Test(expected=DeserializationException.class)
	public void testEmptyObjThrowsException() throws Exception {
		deserializer.deserializeJson("");
	}
	
	@Test(expected=DeserializationException.class)
	public void testLoadAndFindInInstructionThrowsException() throws Exception {
		new NonStrictExpectations() {{
			findObj.has(LOAD); result = true;
		}};
		deserializer.deserializeJson(findJson);
	}
	
	@Test(expected=DeserializationException.class)
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			findObj.has(MAX_MATCH); result = true;
			findObj.has(MATCH); result = true;
		}};
		deserializer.deserializeJson(findJson);
	}

	@Test(expected=DeserializationException.class)
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			findObj.has(MIN_MATCH); result = true;
			findObj.has(MATCH); result = true;
		}};
		deserializer.deserializeJson(findJson);
	}
	
	@Test
	public void testDefaultsToAllMatches(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		final String patternString = randomString();
		
		new NonStrictExpectations() {{
			compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		//Instruction instruction = deserializer.deserializeInstruction(findJson);
		/*
		new Verifications() {{
			pattern.match(stringSource, anyString, FIRST_MATCH, LAST_MATCH);
		}};*/
	}

	@Test(expected = DeserializationException.class)
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = randomInt() + 1;
		final int max = randomInt(min);
		new NonStrictExpectations() {{
			findObj.has(MIN_MATCH); result = true;
			findObj.has(MAX_MATCH); result = true;
			findObj.getInt(MIN_MATCH); result = min;
			findObj.getInt(MAX_MATCH); result = max;
		}};
		
		deserializer.deserializeJson(findJson);
	}
	

	@Test(expected = DeserializationException.class)
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int max = 0 - (randomInt() + 1);
		final int min = 0 - (randomInt(0 - max));
		new NonStrictExpectations() {{
			findObj.has(MIN_MATCH); result = true;
			findObj.has(MAX_MATCH); result = true;
			findObj.getInt(MIN_MATCH); result = min;
			findObj.getInt(MAX_MATCH); result = max;
		}};
		
		deserializer.deserializeJson(findJson);
	}
	
	@Test
	public void testMatchDefaultsToFullRange(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		final String patternString = randomString();
		/*
		new NonStrictExpectations() {{
				source.getValue(); result = stringSource;
				obj.getString(MustachePattern.PATTERN); result = patternString;
				compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		new Find(obj).generateResults(compiler, browser, variables, source, database);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, FIRST_MATCH, LAST_MATCH);
		}};*/
	}
	
}
