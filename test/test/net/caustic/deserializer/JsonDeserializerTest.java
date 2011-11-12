package net.caustic.deserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.caustic.database.DatabaseView;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DeserializerResult;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.json.JsonMEParser;
import net.caustic.json.JsonParser;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.uri.URILoader;
import net.caustic.uri.UriResolver;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StringUtils;
import static net.caustic.deserializer.JSONDeserializer.*;
import static net.caustic.util.TestUtils.randomInt;
import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JsonDeserializerTest {

	private final Class<JsonParser> klass;
	
	@Mocked HttpBrowser browser;
	@Mocked UriResolver resolver;
	@Mocked URILoader loader;
	
	private JSONDeserializer deserializer;
	private RegexpCompiler compiler;
	
	private static final String urlString = "URL " + randomString();
	private static final String patternString = "PATTERN " + randomString();
	
	private String emptyJson;
	private String loadPath;
	private String findPath;
	private JSONObject loadObj;
	private JSONObject findObj;
		
	private final String userDir = StringUtils.USER_DIR;
	
	private DatabaseView input;
	
	/**
	 * Convenience method to return {@link DeserializerResult} reusing an input and URI.
	 * @param obj The {@link JSONObject} to deserialize
	 * @return the {@link DeserializerResult}.
	 */
	private DeserializerResult deserialize(JSONObject obj) throws InterruptedException {
		return deserializer.deserialize(obj.toString(), input, userDir);
	}
	
	@Parameters
	public static Collection<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
			{ JsonMEParser.class }
		});
	}
	
	public JsonDeserializerTest(Class<JsonParser> klass) {
		this.klass = klass;
	}
	
	@Before
	public void setUp() throws Exception {
		
		JsonParser parser = klass.newInstance();

		parser = new JsonMEParser();
		input = new DatabaseView(new InMemoryDatabase());
		
		loadPath = "LOAD PATH " + randomString();
		loadObj = new JSONObject().put(LOAD, urlString);
		findPath = "FIND PATH " + randomString();
		findObj = new JSONObject().put(FIND, patternString);
		compiler = new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8));
		deserializer = new JSONDeserializer(parser, compiler, resolver, loader);
		emptyJson = new JSONObject().toString();
		
		final String loadUri = "LOAD URI " + randomString();
		final String findUri = "FIND URI " + randomString();
		new NonStrictExpectations() {
			{
				resolver.resolve(anyString, SELF);
				
				resolver.resolve(userDir, ""); result = userDir;
				resolver.resolve(loadPath, ""); result = loadPath;
				resolver.resolve(findPath, ""); result = findPath;
				
				resolver.resolve(userDir, loadPath); result = loadUri;			
				resolver.resolve(userDir, findPath); result = findUri;
				loader.load(loadUri); result = loadObj.toString();
				loader.load(findUri); result = findObj.toString();
			}
		};
	}
	
	@Test
	public void testDeserializeSimpleLoadFromJsonSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(loadObj.toString(), input, userDir);
		assertTrue(result.getInstruction() != null);
		//assertEquals("Load", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Load);
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(loadPath, input, userDir);
		assertTrue(result.getInstruction() != null);
		//assertEquals("Load", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Load);
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertTrue(result.getInstruction() != null);
		//assertEquals("Find", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Find);
	}

	@Test
	public void testDeserializeSimpleFindFromUriSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(findPath, input, userDir);
		assertTrue(result.getInstruction() != null);
		//assertEquals("Find", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Find);
	}

	@Test
	public void testEmptyObjFails() throws Exception {
		DeserializerResult result = deserializer.deserialize(emptyJson, input, userDir);
		assertNotNull("Should have failed because neither Find nor Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		findObj.put(randomString(), randomString());
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because a random key-value was added.", 
				result.getFailedBecause());
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		findObj.put(LOAD, randomString());
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because both a Find and a Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		findObj.put(MAX_MATCH, 10);
		findObj.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because both " + MAX_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		findObj.put(MIN_MATCH, 0);
		findObj.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because both " + MIN_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefaultsToAllMatches(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();

		Instruction find = deserializer.deserialize(findObj.toString(), input, userDir).getInstruction();
		find.execute(stringSource, input, browser);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, Pattern.FIRST_MATCH, Pattern.LAST_MATCH);
		}};
	}

	@Test
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = randomInt() + 1;
		final int max = randomInt(min);
		findObj.put(MIN_MATCH, min);
		findObj.put(MAX_MATCH, max);
		
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because of invalid positive " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int max = 0 - (randomInt() + 1);
		final int min = 0 - (randomInt(0 - max));
		findObj.put(MIN_MATCH, min);
		findObj.put(MAX_MATCH, max);
		
		DeserializerResult result = deserializer.deserialize(findObj.toString(), input, userDir);
		assertNotNull("Should have failed because of invalid negative " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, findObj);
		
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		assertTrue("Should be a Find.", result.getInstruction() != null);
	}
	
	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, findPath);
				
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		//assertEquals("Find", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Find);
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, new JSONArray().put(findObj));
		
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		//assertEquals("Find", result.getInstruction().getClass().getSimpleName());
		assertTrue(result.getInstruction() instanceof Find);

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
	public void testSelfReference(@Mocked final Pattern pattern) throws Exception {
		final int recursions = 100;
		
		final String source = randomString();
		final String[] matches = new String[] { source };
		findObj.put(THEN, SELF);
		
		new NonStrictExpectations() {{
			loader.load(userDir); result = findObj.toString();
		}};
		
		new Expectations() {{
			pattern.match(source, anyString, anyInt, anyInt); result = matches; times = recursions +1;
		}};
		
		//Find find = deserializer.deserialize(findObj.toString(), input, userDir).getFind();
		Instruction instruction = new SerializedInstruction(findObj.toString(), deserializer, userDir);
		
		InstructionResult result = instruction.execute(source, input, browser);
		Instruction child = result.getChildren()[0];
		for(int i = 0 ; i < recursions; i ++ ) {
			//child = child.scrape().getChildren()[0];
			child = child.execute(source, input, browser).getChildren()[0];
		}
	}
	

	@Test 
	public void testSelfReferenceArray(@Mocked final Pattern pattern) throws Exception {
		final int recursions = 10;
		
		final String source = randomString();
		final String[] matches = new String[] { source };
		findObj.put(THEN, new JSONArray().put(SELF).put(SELF));

		new NonStrictExpectations() {{
			loader.load(userDir); result = findObj.toString();
		}};
		
		new Expectations() {{
			pattern.match(source, anyString, anyInt, anyInt); result = matches;
					times = Double.valueOf(Math.pow(2, recursions)).intValue();
		}};
		
		//Instruction instruction = deserializer.deserialize(findObj.toString(), input, userDir).getInstruction();
		
		Instruction instruction = new SerializedInstruction(findObj.toString(), deserializer, userDir);
		
		instruction.execute(source, input, browser);
		List<Instruction> children = new ArrayList<Instruction>();
		children.addAll(Arrays.asList(instruction.execute(source, input, browser).getChildren()));
		for(int i = 1 ; i < recursions; i ++ ) {
			assertEquals(Double.valueOf(Math.pow(2, i)).intValue(), children.size());
			
			List<Instruction> recursedChildren = new ArrayList<Instruction>();
			
			Iterator<Instruction> iter = children.listIterator();
			while(iter.hasNext()) {
				recursedChildren.addAll(Arrays.asList(iter.next().execute(source, input, browser).getChildren()));
			}
			
			children.clear();
			children = recursedChildren;
		}
	}
	

	@Test
	public void testDeserializeSimpleHead() throws Exception {
		final String url = randomString();
		new Expectations() {{
			browser.head(url, (Hashtable) any);
		}};
		
		JSONObject simpleHead = new JSONObject().put(JSONDeserializer.LOAD, url).put(METHOD, HttpBrowser.HEAD);
		Load load = (Load) deserialize(simpleHead).getInstruction();
		load.execute(null, input, browser);
	}
	
	@Test
	public void testDeserializeSimpleGetDefaultMethod() throws Exception {
		final String url = randomString();
		new Expectations() {{
			browser.get(url, (Hashtable) any, (Pattern[]) any);
		}};
		
		JSONObject simpleGet = new JSONObject().put(LOAD, url);
		Load load = (Load) deserialize(simpleGet).getInstruction();
		load.execute(null, input, browser);
	}

	@Test
	public void testDeserializeSimpleGetExplicitMethod() throws Exception {
		final String url = randomString();
		new Expectations() {{
			browser.get(url, (Hashtable) any, (Pattern[]) any);
		}};
		
		JSONObject simpleGet = new JSONObject().put(LOAD, url).put(METHOD, HttpBrowser.GET);
		Load load = (Load) deserialize(simpleGet).getInstruction();
		load.execute(null, input, browser);
	}
	

	@Test
	public void testDeserializeSimplePostWithoutData() throws Exception {
		final String url = randomString();
		new Expectations() {{
			browser.post(url, (Hashtable) any, (Pattern[]) any, "");
		}};
		
		JSONObject simplePost =
				new JSONObject().put(LOAD, url).put(METHOD, HttpBrowser.POST);		
		Load load = (Load) deserialize(simplePost).getInstruction();
		load.execute(null, input, browser);
	}
	
	@Test
	public void testDeserializeSimpleFind() throws Exception {
		final String patternStr = randomString();
		final String source = randomString();
		JSONObject simpleFind = new JSONObject().put(FIND, patternStr);
		DeserializerResult result = deserializer.deserialize(simpleFind.toString(), input, userDir);
		assertTrue(result.getInstruction() != null);
		
		Find find = (Find) result.getInstruction();
		find.execute(source, input, browser);
	}
}
