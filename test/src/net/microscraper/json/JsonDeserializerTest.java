package net.microscraper.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Scraper;
import net.microscraper.client.ScraperResult;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.HashtableDatabaseView;
import net.microscraper.deserializer.DeserializerResult;
import net.microscraper.deserializer.JSONDeserializer;
import net.microscraper.http.HttpBrowser;
import net.microscraper.instruction.Instruction;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.URILoader;
import net.microscraper.uri.UriResolver;
import net.microscraper.util.Encoder;
import static net.microscraper.util.TestUtils.randomInt;
import static net.microscraper.util.TestUtils.randomString;
import static net.microscraper.deserializer.JSONDeserializer.*;
import static org.junit.Assert.*;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	private @Mocked RegexpCompiler compiler;
	private @Mocked HttpBrowser browser;
	private @Mocked Encoder encoder;
	private @Mocked UriResolver uriResolver;
	private @Mocked URILoader uriLoader;
	private @Mocked Pattern pattern;
	
	private JsonParser parser;
	private JSONDeserializer deserializer;
	
	private static final String urlString = "URL " + randomString();
	private static final String patternString = "PATTERN " + randomString();
	
	private String emptyJson;
	private String userDir;
	private String loadPath;
	private String findPath;
	private DatabaseView input;
	private JSONObject load;
	private JSONObject find;
	
	@Before
	public void setUp() throws Exception {
		parser = new JsonMEParser();
		input = new HashtableDatabaseView(new Hashtable<String, String>());
		
		loadPath = "LOAD PATH " + randomString();
		load = new JSONObject().put(LOAD, urlString);
		findPath = "FIND PATH " + randomString();
		find = new JSONObject().put(FIND, patternString);
		deserializer = new JSONDeserializer(parser, compiler, browser, encoder, uriResolver, uriLoader);
		emptyJson = new JSONObject().toString();
		userDir = "USER DIR " + randomString();
		
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
		DeserializerResult result = deserializer.deserialize(load.toString(), input, userDir);
		assertTrue(result.isSuccess());
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(loadPath, input, userDir);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testDeserializeSimpleFindFromUriSucceeds() throws Exception {
		DeserializerResult result = deserializer.deserialize(findPath, input, userDir);
		assertTrue(result.isSuccess());
	}

	@Test
	public void testEmptyObjFails() throws Exception {
		DeserializerResult result = deserializer.deserialize(emptyJson, input, userDir);
		assertNotNull("Should have failed because neither Find nor Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		find.put(randomString(), randomString());
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because a random key-value was added.", 
				result.getFailedBecause());
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		find.put(LOAD, randomString());
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because both a Find and a Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		find.put(MAX_MATCH, 10);
		find.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because both " + MAX_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		find.put(MIN_MATCH, 0);
		find.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because both " + MIN_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefaultsToAllMatches(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		
		new NonStrictExpectations() {{
			compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		Instruction instruction = deserializer.deserialize(find.toString(), input, userDir).getInstruction();
		instruction.execute(stringSource, input);
		
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
		
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because of invalid positive " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int max = 0 - (randomInt() + 1);
		final int min = 0 - (randomInt(0 - max));
		find.put(MIN_MATCH, min);
		find.put(MAX_MATCH, max);
		
		DeserializerResult result = deserializer.deserialize(find.toString(), input, userDir);
		assertNotNull("Should have failed because of invalid negative " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, find);
		
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		assertTrue("Should be a Find.", result.isSuccess());
	}
	

	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, findPath);
				
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		assertTrue("Should be a Find.", result.isSuccess());
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject().put(EXTENDS, new JSONArray().put(find));
		
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), input, userDir);
		assertTrue("Should be a Find.", result.isSuccess());
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
		
		Instruction instruction = deserializer.deserialize(find.toString(), input, userDir).getInstruction();
		
		ScraperResult result = instruction.execute(source, input);
		Scraper child = result.getChildren()[0];
		for(int i = 0 ; i < recursions; i ++ ) {
			child = child.scrape().getChildren()[0];
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
					times = Double.valueOf(Math.pow(2, recursions)).intValue();
		}};
		
		Instruction instruction = deserializer.deserialize(find.toString(), input, userDir).getInstruction();
		
		instruction.execute(source, input);
		List<Scraper> children = new ArrayList<Scraper>();
		children.addAll(Arrays.asList(instruction.execute(source, input).getChildren()));
		for(int i = 1 ; i < recursions; i ++ ) {
			assertEquals(Double.valueOf(Math.pow(2, i)).intValue(), children.size());
			
			List<Scraper> recursedChildren = new ArrayList<Scraper>();
			
			Iterator<Scraper> iter = children.listIterator();
			while(iter.hasNext()) {
				recursedChildren.addAll(Arrays.asList(iter.next().scrape().getChildren()));
			}
			
			children.clear();
			children = recursedChildren;
		}
	}
	
}
