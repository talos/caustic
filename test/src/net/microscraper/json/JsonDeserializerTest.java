package net.microscraper.json;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.instruction.Instruction;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;
import static net.microscraper.json.JsonDeserializer.*;
import static net.microscraper.util.TestUtils.randomInt;
import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.*;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	private @Mocked RegexpCompiler compiler;
	private @Mocked Browser browser;
	private @Mocked Encoder encoder;
	private @Mocked UriResolver uriResolver;
	private @Mocked URILoader uriLoader;
	private @Mocked Variables variables;

	private JsonParser parser;
	private JsonDeserializer deserializer;
	
	private static final String googleString = "http://www.google.com";
	private static final String patternString = ".*";
	
	private String emptyJson;
	private String userDir;
	private String loadPath;
	private String findPath;
	private JSONObject load;
	private JSONObject find;
	
	@Before
	public void setUp() throws Exception {
		parser = new JsonMEParser();
		
		loadPath = randomString();
		load = new JSONObject().put(LOAD, googleString);
		findPath = randomString();
		find = new JSONObject().put(FIND, patternString);
		deserializer = new JsonDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
		emptyJson = new JSONObject().toString();
		userDir = randomString();
		
		new NonStrictExpectations() {
			String loadUri = randomString();
			String findUri = randomString();
			{
				uriResolver.resolve(userDir, loadPath); result = loadUri;			
				uriResolver.resolve(userDir, findPath); result = findUri;
				uriLoader.load(loadUri); result = load;
				uriLoader.load(findUri); result = find;
			}
		};
	}
	
	@Test
	public void testDeserializeSimpleLoadFromJsonSucceeds() throws Exception {
		Execution exc = deserializer.deserializeString(load.toString(), variables, userDir);
		assertTrue(exc + " should be a Load.", exc.isSuccessful());
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {
		Execution exc = deserializer.deserializeString(loadPath, variables, userDir);
		assertTrue(exc + " should be a Load.", exc.isSuccessful());
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceeds() throws Exception {
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should be a Find.", exc.isSuccessful());
	}

	@Test
	public void testDeserializeSimpleFindFromUriSucceeds() throws Exception {
		Execution exc = deserializer.deserializeString(findPath, variables, userDir);
		assertTrue(exc + " should be a Find.", exc.isSuccessful());
	}

	@Test
	public void testEmptyObjFails() throws Exception {
		Execution exc = deserializer.deserializeString(emptyJson, variables, userDir);
		assertTrue(exc + " should have failed because neither Find nor Load were defined.", exc.hasFailed());
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		find.put(randomString(), randomString());
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because a random key-value was added.", exc.hasFailed());
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		find.put(LOAD, randomString());
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because both a Find and a Load were defined.", exc.hasFailed());
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		find.put(MAX_MATCH, 10);
		find.put(MATCH, 5);
		
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because both " + MAX_MATCH + " and " + MATCH + " were defined.", exc.hasFailed());
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		find.put(MIN_MATCH, 0);
		find.put(MATCH, 5);
		
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because both " + MIN_MATCH + " and " + MATCH + " were defined.", exc.hasFailed());
	}
	
	@Test
	public void testDefaultsToAllMatches(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		
		new NonStrictExpectations() {{
			compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		Instruction instruction = (Instruction) deserializer.deserializeString(find.toString(), variables, userDir).getExecuted();
		instruction.execute(stringSource, variables);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, Pattern.FIRST_MATCH, Pattern.LAST_MATCH);
		}};
	}

	@Test
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = randomInt() + 1;
		final int max = randomInt(min);
		find.put(MIN_MATCH, min);
		find.put(MAX_MATCH, max);
		
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because of invalid positive " + MIN_MATCH + " to " +MAX_MATCH + " range.", exc.hasFailed());
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int max = 0 - (randomInt() + 1);
		final int min = 0 - (randomInt(0 - max));
		find.put(MIN_MATCH, min);
		find.put(MAX_MATCH, max);
		
		Execution exc = deserializer.deserializeString(find.toString(), variables, userDir);
		assertTrue(exc + " should have failed because of invalid negative " + MIN_MATCH + " to " +MAX_MATCH + " range.", exc.hasFailed());
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, find);
		
		Execution exc = deserializer.deserializeString(extendedFind.toString(), variables, userDir);
		assertTrue(exc + " should be a Find.", exc.isSuccessful());
	}
	

	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, findPath);
				
		Execution exc = deserializer.deserializeString(extendedFind.toString(), variables, userDir);
		assertTrue(exc + " should be a Find.", exc.isSuccessful());
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, new JSONArray().put(find));
		
		Execution exc = deserializer.deserializeString(extendedFind.toString(), variables, userDir);
		assertTrue(exc + " should be a Find.", exc.isSuccessful());
	}
}
