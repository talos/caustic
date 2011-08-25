package net.microscraper.json;

import java.util.Iterator;
import java.util.Vector;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.database.HashtableDatabase;
import net.microscraper.database.Variables;
import net.microscraper.instruction.Executable;
import net.microscraper.instruction.Instruction;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.VectorUtils;
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
	private @Mocked Pattern pattern;
	
	private JsonParser parser;
	private JsonDeserializer deserializer;
	
	private static final String urlString = "URL " + randomString();
	private static final String patternString = "PATTERN " + randomString();
	
	private Variables variables;
	private String emptyJson;
	private String userDir;
	private String loadPath;
	private String findPath;
	private JSONObject load;
	private JSONObject find;
	
	@Before
	public void setUp() throws Exception {
		parser = new JsonMEParser();
		
		loadPath = "LOAD PATH " + randomString();
		load = new JSONObject().put(LOAD, urlString);
		findPath = "FIND PATH " + randomString();
		find = new JSONObject().put(FIND, patternString);
		deserializer = new JsonDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
		emptyJson = new JSONObject().toString();
		userDir = "USER DIR " + randomString();
		
		variables = new HashtableDatabase().open();

		final String loadUri = "LOAD URI " + randomString();
		final String findUri = "FIND URI " + randomString();
		new NonStrictExpectations() {
			{
				uriResolver.resolve(anyString, SELF);
				
				uriResolver.resolve(userDir, ""); result = userDir;
				uriResolver.resolve(loadPath, ""); result = loadPath;
				uriResolver.resolve(findPath, ""); result = findPath;
				
				uriResolver.resolve(userDir, loadPath); result = loadUri;			
				uriResolver.resolve(userDir, findPath); result = findUri;
				uriLoader.load(loadUri); result = load.toString();
				uriLoader.load(findUri); result = find.toString();
				
				compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
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
	/*
	@Test
	public void textExtendsAccretes() throws Exception {
		JSONObject somePosts =
				new JSONObject()
					.put(POSTS, new JSONObject()
						.put(randomString(), randomString())
						.put(randomString(), randomString()));
		JSONObject morePosts = 
				new JSONObject()
					.put(POSTS,  new JSONObject()
						.put(randomString(), randomString())
						.put(randomString(), randomString()));
		load.put(EXTENDS, new JSONArray().put(somePosts).put(morePosts));
		new Expectations() {{
			browser.post(urlString, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any,
					}));
		}};
		
		Execution exc;
		exc = deserializer.deserializeString(load.toString(), variables, userDir);
		Instruction instruction = (Instruction) exc.getExecuted();
		
		instruction.execute(null, variables);
	}*/
	
	@Test
	public void testSelfReference() throws Exception {
		final int recursions = 100;
		
		final String source = randomString();
		final String[] matches = new String[] { source };
		find.put(THEN, SELF);
		
		new NonStrictExpectations() {{
			uriLoader.load(userDir); result = find.toString();
		}};
		
		new Expectations() {{
			pattern.match(source, anyString, anyInt, anyInt); result = matches; times = recursions +1;
		}};
		
		Execution exc;
		exc = deserializer.deserializeString(find.toString(), variables, userDir);
		Instruction instruction = (Instruction) exc.getExecuted();
		
		Executable child = ((Executable[]) instruction.execute(source, variables).getExecuted())[0];
		for(int i = 0 ; i < recursions; i ++ ) {
			child = ((Executable[]) child.execute().getExecuted())[0];
		}
	}
	

	@Test 
	public void testSelfReferenceArray() throws Exception {
		final int recursions = 10;
		
		final String source = randomString();
		final String[] matches = new String[] { source };
		find.put(THEN, new JSONArray().put(SELF).put(SELF));

		new NonStrictExpectations() {{
			uriLoader.load(userDir); result = find.toString();
		}};
		
		new Expectations() {{
			pattern.match(source, anyString, anyInt, anyInt); result = matches;
					times = Double.valueOf(Math.pow(2, recursions)).intValue() -1;
		}};
		
		Execution exc;
		exc = deserializer.deserializeString(find.toString(), variables, userDir);
		Instruction instruction = (Instruction) exc.getExecuted();
		
		Vector<Executable> children = new Vector<Executable>();
		VectorUtils.arrayIntoVector((Executable[]) instruction.execute(source, variables).getExecuted(), children);
		for(int i = 1 ; i < recursions; i ++ ) {
			assertEquals(Double.valueOf(Math.pow(2, i)).intValue(), children.size());
			
			Vector<Executable> recursedChildren = new Vector<Executable>();
			
			Iterator<Executable> iter = children.listIterator();
			while(iter.hasNext()) {
				VectorUtils.arrayIntoVector(((Executable[]) iter.next().execute().getExecuted()), recursedChildren);
			}
			
			children.clear();
			children = recursedChildren;
		}
	}
}
