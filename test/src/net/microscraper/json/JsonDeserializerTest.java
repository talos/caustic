package net.microscraper.json;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.database.Database;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Load;
import net.microscraper.instruction.Result;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Variables;
import static net.microscraper.json.JsonDeserializer.*;
import static net.microscraper.test.TestUtils.randomInt;
import static net.microscraper.test.TestUtils.randomString;

import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	private final String loadJson = randomString();
	private final String findJson = randomString();
	private @Mocked JsonObject loadObj, findObj;
	private @Mocked JsonParser parser;
	private @Mocked RegexpCompiler compiler;
	private @Mocked Browser browser;
	
	private JsonDeserializer deserializer;
	
	private static final String googleString = "http://www.google.com";
	private static final String patternString = ".*";
	
	@Before
	public void setUp() throws Exception {
		deserializer = new JsonDeserializer(parser, compiler, browser);
		new NonStrictExpectations() {{
			parser.parse(loadJson); result = loadObj;
			loadObj.getString(LOAD); result = googleString;
			parser.parse(findJson); result = findObj;
			findObj.getString(FIND); result = patternString;
		}};
	}
	
	@Test
	public void testDeserializeSimpleLoad() throws Exception {
		deserializer.deserializeLoad(loadJson);
	}

	@Test
	public void testDeserializeSimpleFind() throws Exception {
		deserializer.deserializeFind(findJson);
	}

	@Test(expected=DeserializationException.class)
	public void testNotALoad() throws Exception {
		deserializer.deserializeFind(loadJson);
	}
	
	@Test(expected=DeserializationException.class)
	public void testNotAFind() throws Exception {
		deserializer.deserializeLoad(findJson);
	}
	
	@Test(expected=DeserializationException.class)
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			findObj.has(MAX_MATCH); result = true;
			findObj.has(MATCH); result = true;
		}};
		deserializer.deserializeFind(findJson);
	}

	@Test(expected=DeserializationException.class)
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			findObj.has(MIN_MATCH); result = true;
			findObj.has(MATCH); result = true;
		}};
		deserializer.deserializeFind(findJson);
	}
	
	@Test
	public void testDefaultsToAllMatches(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		final String patternString = randomString();
		
		new NonStrictExpectations() {{
			compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		Find find = deserializer.deserializeFind(findJson);
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
		
		deserializer.deserializeFind(findJson);
	}
	

	@Test(expected = DeserializationException.class)
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = 0 - (randomInt() + 1);
		final int max = 0 - (randomInt(0 - min));
		new NonStrictExpectations() {{
			findObj.has(MIN_MATCH); result = true;
			findObj.has(MAX_MATCH); result = true;
			findObj.getInt(MIN_MATCH); result = min;
			findObj.getInt(MAX_MATCH); result = max;
		}};
		
		deserializer.deserializeFind(findJson);
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
