package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.client.DeserializationException;
import net.microscraper.database.Database;
import net.microscraper.json.JsonArray;
import net.microscraper.json.JsonObject;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import static net.microscraper.test.TestUtils.*;
import static net.microscraper.instruction.Load.*;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class PageTest extends InstructionTest {
	
	@Mocked JsonObject obj;
	@Mocked Database database;
	@Mocked Browser browser;
	@Mocked RegexpCompiler compiler;
	@Mocked Variables variables;

	String url = randomString();

	@Override
	public Load getInstruction(final JsonObject obj) throws Exception {
		final String url = randomString();
		new NonStrictExpectations() {{
			onInstance(obj).getString(URL); result = url;
		}};
		return new Load(obj);
	}
	
	/**
	 * Assign {@link #obj} the URL {@link #url} for each test.
	 */
	@Before
	public void setup() throws Exception {
		new NonStrictExpectations() {{
			onInstance(obj).getString(URL); result = url;
		}};
	}
	
	@Test
	public void testByDefaultDoesntSaveValue() throws Exception {
		Load page = new Load(obj);
		page.generateResults(compiler, browser, variables, null, database);
		
		new Verifications() {{
			database.store(anyString, (String) withNull(), 0); $ = "Stored page response in database by default.";
		}};
	}
	
	@Test
	public void testSavesValue() throws Exception {
		final String content = randomString();
		new NonStrictExpectations() {{
			obj.has(SAVE); result = true; 
			obj.getBoolean(SAVE); result = true;
			browser.get(url, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any); result = content;
		}};
		Load page = new Load(obj);
		page.generateResults(compiler, browser, variables, null, database);
		
		new Verifications() {
			{
				database.store(anyString, content, 0); $ = "Did not store page response in database.";
			}
		};
	}

	@Test(expected = DeserializationException.class)
	public void testCannotDeserializeWithoutUrl(@Mocked final JsonObject objWithoutUrl) throws Exception {
		new Load(objWithoutUrl);
	}

	@Test(expected = DeserializationException.class)
	public void testCannotDeserializeInvalidMethod() throws Exception {
		new NonStrictExpectations() {{
			obj.has(METHOD); result = true;
			obj.getString(METHOD); result = "not a method";
		}};
		new Load(obj);
	}
	
	@Test
	public void testLoadsPreloadsBefore() throws Exception {
		final String preloadUrl1 = randomString();
		final String preloadUrl2 = randomString();
		
		new NonStrictExpectations() {
			JsonArray preloadPages;
			JsonObject preload1, preload2;
			{
				preloadPages.length(); result = 2;
				preload1.getString(URL); result = preloadUrl1;
				preload2.getString(URL); result = preloadUrl2;
				preloadPages.getJsonObject(0); result = preload1;
				preloadPages.getJsonObject(1); result = preload2;
				obj.has(PRELOAD); result = true;
				obj.getJsonArray(PRELOAD); result = preloadPages;
			}
		};
		Load page = new Load(obj);
		page.generateResults(compiler, browser, variables, null, database);

		new Verifications() {{
			browser.get(preloadUrl1, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
			browser.get(preloadUrl2, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
		}};
	}
	
	@Test
	public void testPageNameDefaultsToUrl() throws Exception {
		Load page = new Load(obj);
		page.generateResults(compiler, browser, variables, null, database);
		
		new Verifications() {{
			database.store(url, anyString, 0);
		}};
	}
	
	@Test
	public void testPageSubstitutions(@Mocked final JsonObject objWithSubstitutableUrl) throws Exception {
		final String key = "anything";
		final String value = "google";
		final String substitutableUrl = "http://www.{{" + key + "}}.com/";
		final String substitutedUrl = "http://www." + value + ".com/";
		new NonStrictExpectations() {{
			objWithSubstitutableUrl.getString(URL); result = substitutableUrl;
			variables.containsKey(key); result = true;
			variables.get(key); result = value;
			browser.encode(value, anyString); result = value;
		}};
		
		Load page = new Load(objWithSubstitutableUrl);
		page.execute(compiler, browser, variables, null, database, null);
		
		new Verifications() {{
			database.store(substitutedUrl, null, 0); times = 1;
			browser.get(substitutedUrl, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any); times = 1;
		}};
	}
	
	@Test
	public void testSendsPostIfPostDataDefined() throws Exception {
		final String postData = randomString();
		new NonStrictExpectations() {{
			obj.has(POSTS); result = true;
			obj.getString(POSTS); result = postData;
		}};
		
		new Load(obj).execute(compiler, browser, variables, null, database, null);
		
		new Verifications() {{
			browser.post(url, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any, postData); times =1;
		}};
	}
	
	@Test(expected = DeserializationException.class)
	public void testDeserializationExceptionIfHeadMethodWithPostData() throws Exception {
		final String postData = randomString();
		new NonStrictExpectations() {{
			obj.has(POSTS); result = true;
			obj.getString(POSTS); result = postData;
			obj.has(METHOD); result = true;
			obj.getString(METHOD); result = "head";
		}};
		
		new Load(obj);
	}
	
	@Test(expected = DeserializationException.class)
	public void testDeserializationExceptionIfGetMethodWithPostData() throws Exception {
		final String postData = randomString();
		new NonStrictExpectations() {{
			obj.has(POSTS); result = true;
			obj.getString(POSTS); result = postData;
			obj.has(METHOD); result = true;
			obj.getString(METHOD); result = "get";
		}};
		
		new Load(obj);
	}
}
