package net.caustic.deserializer;

import java.util.Hashtable;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.caustic.database.Database;
import net.caustic.database.InMemoryDatabase;
import net.caustic.deserializer.DeserializerResult;
import net.caustic.deserializer.JSONDeserializer;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.Instruction;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.instruction.SerializedInstruction;
import net.caustic.regexp.Pattern;
import net.caustic.scope.Scope;
import net.caustic.uri.URILoader;
import net.caustic.util.StringUtils;
import static net.caustic.deserializer.JSONDeserializer.*;
import static org.junit.Assert.*;

import org.json.me.JSONArray;
import org.json.me.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class JsonDeserializerTest {
	
	@Capturing URILoader loader;
	
	private JSONDeserializer deserializer;
	private final String userDir = StringUtils.USER_DIR;
	
	private Database db;
	private Scope scope;
	
	@Before
	public void setUp() throws Exception {
		db = new InMemoryDatabase();
		scope = db.newScope();
		deserializer = new DefaultJSONDeserializer();
	}
	
	@Test
	public void testDeserializeSimpleLoadFromJsonSucceeds() throws Exception {
		JSONObject load = new JSONObject().put(LOAD, "http://www.foo.com/");
		
		DeserializerResult result = deserializer.deserialize(load.toString(), db, scope, userDir);
		assertTrue(result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Load);
	}
	
	@Test
	public void testDeserializeSimpleLoadFromUriSucceeds() throws Exception {		
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put(LOAD, "http://www.foo.com/").toString();
		}};
		
		DeserializerResult result = deserializer.deserialize("/uri", db, scope, userDir);
		assertTrue(result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Load);
	}

	@Test
	public void testDeserializeSimpleFindFromJsonSucceeds() throws Exception {
		JSONObject find = new JSONObject().put(FIND, "^foo$");
		
		DeserializerResult result = deserializer.deserialize(find.toString(), db, scope, userDir);
		assertTrue(result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Find);
	}

	@Test
	public void testDeserializeSimpleFindFromUriSucceeds() throws Exception {
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put(FIND, "^foo$").toString();
		}};
		
		DeserializerResult result = deserializer.deserialize("/uri", db, scope, userDir);
		assertTrue(result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Find);
	}

	@Test
	public void testEmptyObjFails() throws Exception {
		JSONObject empty = new JSONObject();
		
		DeserializerResult result = deserializer.deserialize(empty.toString(), db, scope, userDir);
		assertNotNull("Should have failed because neither Find nor Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testPointerJSON() throws Exception {
		
		new Expectations() {{
			loader.load("/pointer"); result = "/uri";
			loader.load("/uri"); result = new JSONObject().put(FIND, "^foo$").toString();
		}};
		
		DeserializerResult result = deserializer.deserialize("/pointer", db, scope, userDir);
		assertTrue(result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Find);
	}
	
	@Test
	public void testArrayOfJSON(@Mocked HttpBrowser browser) throws Exception {
		JSONArray ary = new JSONArray();
		
		ary.put(new JSONObject().put(FIND, "^foo$"));
		ary.put(new JSONObject().put(LOAD, "http://www.google.com/"));
		
		DeserializerResult dResult = deserializer.deserialize(ary.toString(), db, scope, userDir);
		
		Instruction instruction = dResult.getInstruction();
		
		// the actual instructions are contained in the child array.
		Instruction[] instructions = instruction.execute(null, db, scope, browser).getChildren();
		
		assertEquals(2, instructions.length);
		assertTrue(instructions[0] instanceof SerializedInstruction);
		assertTrue(instructions[1] instanceof SerializedInstruction);
	}
	
	@Test
	public void testRandomKeyFails() throws Exception {
		JSONObject bad = new JSONObject().put("foo", "bar");
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because a random key-value was added.", 
				result.getFailedBecause());
	}
	
	@Test
	public void testLoadAndFindInInstructionFails() throws Exception {
		JSONObject bad = new JSONObject();
		bad.put(FIND, "^foo$");
		bad.put(LOAD, "http://www.foo.com/");
				
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because both a Find and a Load were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put(FIND, "^foo$");
		
		bad.put(MAX_MATCH, 10);
		bad.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because both " + MAX_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}

	@Test
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		JSONObject bad = new JSONObject().put(FIND, "^foo$");

		bad.put(MIN_MATCH, 0);
		bad.put(MATCH, 5);
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because both " + MIN_MATCH + " and " + MATCH + " were defined.",
				result.getFailedBecause());
	}
	
	@Test
	public void testDefaultsToAllMatches(@Capturing final Pattern pattern, @Mocked HttpBrowser browser) throws Exception {
		JSONObject findObj = new JSONObject().put(FIND, "^foo");
		
		final String stringSource = "foo foo foo bar";
		
		Instruction find = deserializer.deserialize(findObj.toString(), db, scope, userDir).getInstruction();
		find.execute(stringSource, db, scope, browser);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, Pattern.FIRST_MATCH, Pattern.LAST_MATCH);
		}};
	}

	@Test
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put(FIND, "^foo$");
		
		bad.put(MIN_MATCH, 6);
		bad.put(MAX_MATCH, 4);
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because of invalid positive " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		JSONObject bad = new JSONObject().put(FIND, "^foo$");

		bad.put(MIN_MATCH, -4);
		bad.put(MAX_MATCH, -5);
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertNotNull("Should have failed because of invalid negative " + MIN_MATCH + " to " +MAX_MATCH + " range.",
				result.getFailedBecause());
	}
	
	@Test
	public void testExtendsObjectSetsFindAttribute() throws Exception {
		JSONObject obj = new JSONObject()
			.put(EXTENDS, new JSONObject().put(FIND, "^foo$"));
		
		DeserializerResult result = deserializer.deserialize(obj.toString(), db, scope, userDir);
		assertTrue("Should be a Find.", result.getInstruction() != null);
		assertTrue(result.getInstruction() instanceof Find);
	}
	
	@Test
	public void testExtendsStringSetsFindAttribute() throws Exception {
		new Expectations() {{
			loader.load("/uri"); result = new JSONObject().put(FIND, "^foo$").toString();
		}};
		
		JSONObject obj = new JSONObject().put(EXTENDS, "/uri");
				
		DeserializerResult result = deserializer.deserialize(obj.toString(), db, scope, userDir);
		assertTrue(result.getInstruction() instanceof Find);
	}
	
	@Test
	public void testExtendsArrayObjectSetsFindAttribute() throws Exception {
		JSONObject extendedFind = new JSONObject()
			.put(EXTENDS, new JSONArray().put(new JSONObject().put(FIND, "^foo$")));
		
		DeserializerResult result = deserializer.deserialize(extendedFind.toString(), db, scope, userDir);
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
	}
	*/
	@Test
	public void testNoSelfReferenceInline() throws Exception {
		JSONObject bad = new JSONObject().put(FIND, "^foo$");
		bad.put(THEN, SELF);
		
		DeserializerResult result = deserializer.deserialize(bad.toString(), db, scope, userDir);
		assertTrue(result.getFailedBecause() != null);
	}
	
	@Test
	public void testSelfReference(@Capturing final Pattern pattern, final @Mocked HttpBrowser browser) throws Exception {
		
		final int recursions = 100;
		
		final String source = "foo";
		final String[] matches = new String[] { source };
		final JSONObject obj = new JSONObject().put(FIND, "^foo$");
		obj.put(THEN, SELF);
		
		new NonStrictExpectations() {{
			loader.load("/uri"); result = obj.toString();
		}};
		
		new Expectations() {{
			pattern.match(source, anyString, anyInt, anyInt); result = matches; times = recursions +1;
		}};
		
		Instruction instruction = new SerializedInstruction("/uri", deserializer, userDir);
		
		InstructionResult result = instruction.execute(source, db, scope, browser);
		Instruction child = result.getChildren()[0];
		// step through instruction for a certain number of steps
		for(int i = 0 ; i < recursions; i ++ ) {
			//child = child.scrape().getChildren()[0];
			child = child.execute(source, db, scope, browser).getChildren()[0];
		}
	}
	
	@Test 
	public void testSelfReferenceArray(@Capturing final Pattern pattern, final @Mocked HttpBrowser browser) throws Exception {		
		final JSONObject obj = new JSONObject();
		obj.put(LOAD, "http://www.foo.com");
		obj.put(THEN, new JSONArray().put(SELF).put(SELF));
		
		new NonStrictExpectations() {{
			loader.load("/uri"); result = obj.toString();
		}};
				
		Instruction instruction = new SerializedInstruction("/uri", deserializer, userDir);
		
		InstructionResult result;
		result = instruction.execute(null, db, scope, browser);
		//List<Instruction> children = new ArrayList<Instruction>();
		//children.addAll(Arrays.asList(result.getChildren()));
		assertEquals(2, result.getChildren().length);
		
		result = result.getChildren()[0].execute(null, db, scope, browser);
		assertEquals(2, result.getChildren().length);
		
		result = result.getChildren()[0].execute(null, db, scope, browser);
		assertEquals(2, result.getChildren().length);
	}

	@Test
	public void testDeserializeSimpleHead(final @Mocked HttpBrowser browser) throws Exception {
		new Expectations() {{
			browser.head("http://www.foo.com/", (Hashtable) any);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put(JSONDeserializer.LOAD, "http://www.foo.com/").put(METHOD, HttpBrowser.HEAD);
		
		Load load = (Load) deserializer.deserialize(obj.toString(), db, scope, userDir).getInstruction();
		load.execute(null, db, scope, browser);
	}
	
	@Test
	public void testDeserializeSimpleGetDefaultMethod(final @Mocked HttpBrowser browser) throws Exception {
		new Expectations() {{
			browser.get("http://www.foo.com/", (Hashtable) any, (Pattern[]) any);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put(LOAD, "http://www.foo.com/");
		Load load = (Load) deserializer.deserialize(obj.toString(), db, scope, userDir).getInstruction();
		load.execute(null, db, scope, browser);
	}

	@Test
	public void testDeserializeSimpleGetExplicitMethod(final @Mocked HttpBrowser browser) throws Exception {
		new Expectations() {{
			browser.get("http://www.foo.com/", (Hashtable) any, (Pattern[]) any);
		}};
		
		JSONObject obj = new JSONObject();
		obj.put(LOAD, "http://www.foo.com/").put(METHOD, HttpBrowser.GET);
		Load load = (Load) deserializer.deserialize(obj.toString(), db, scope, userDir).getInstruction();
		load.execute(null, db, scope, browser);
	}
	

	@Test
	public void testDeserializeSimplePostWithoutData(final @Mocked HttpBrowser browser) throws Exception {
		new Expectations() {{
			browser.post("http://www.foo.com/", (Hashtable) any, (Pattern[]) any, "");
		}};
		
		JSONObject obj = new JSONObject();
		obj.put(LOAD, "http://www.foo.com/").put(METHOD, HttpBrowser.POST);		
		Load load = (Load) deserializer.deserialize(obj.toString(), db, scope, userDir).getInstruction();
		load.execute(null, db, scope, browser);
	}
}
