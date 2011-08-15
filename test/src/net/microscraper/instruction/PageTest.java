package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.json.JSONArrayInterface;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import static net.microscraper.test.TestUtils.*;
import static net.microscraper.instruction.Page.*;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class PageTest {
	
	@Mocked JSONObjectInterface obj;
	@Mocked Database database;
	@Mocked Browser browser;
	@Mocked RegexpCompiler compiler;
	@Mocked Variables variables;

	String url = randomString();

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
		Page page = new Page(obj);
		page.execute(compiler, browser, variables, null, database);
		
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
		Page page = new Page(obj);
		page.execute(compiler, browser, variables, null, database);
		
		new Verifications() {
			{
				database.store(anyString, content, 0); $ = "Did not store page response in database.";
			}
		};
	}

	@Test(expected = DeserializationException.class)
	public void testCannotDeserializeWithoutUrl(@Mocked final JSONObjectInterface objWithoutUrl) throws Exception {
		new Page(objWithoutUrl);
	}

	@Test(expected = DeserializationException.class)
	public void testCannotDeserializeInvalidMethod() throws Exception {
		new NonStrictExpectations() {{
			obj.has(METHOD); result = true;
			obj.getString(METHOD); result = "not a method";
		}};
		new Page(obj);
	}
	
	@Test
	public void testLoadsPreloadsBefore() throws Exception {
		final String preloadUrl1 = randomString();
		final String preloadUrl2 = randomString();
		
		new NonStrictExpectations() {
			JSONArrayInterface preloadPages;
			JSONObjectInterface preload1, preload2;
			{
				preloadPages.length(); result = 2;
				preload1.getString(URL); result = preloadUrl1;
				preload2.getString(URL); result = preloadUrl2;
				preloadPages.getJSONObject(0); result = preload1;
				preloadPages.getJSONObject(1); result = preload2;
				obj.has(PRELOAD); result = true;
				obj.getJSONArray(PRELOAD); result = preloadPages;
			}
		};
		Page page = new Page(obj);
		page.execute(compiler, browser, variables, null, database);

		new Verifications() {{
			browser.get(preloadUrl1, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
			browser.get(preloadUrl2, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
		}};
	}
	
	@Test
	public void testPageNameDefaultsToUrl() throws Exception {
		Page page = new Page(obj);
		page.execute(compiler, browser, variables, null, database);
		
		new Verifications() {{
			database.store(url, anyString, 0);
		}};
	}
	
	@Test
	public void testPageSubstitutions(@Mocked final JSONObjectInterface objWithSubstitutableUrl) throws Exception {
		final String key = "anything";
		final String value = "google";
		final String substitutableUrl = "http://www.{{" + key + "}}.com/";
		final String substitutedUrl = "http://www." + value + ".com/";
		new NonStrictExpectations() {{
			objWithSubstitutableUrl.getString(URL); result = substitutableUrl;
			variables.containsKey(key); result = true;
			variables.get(key); result = value;
			browser.encode(value, anyString); result = value;
			//browser.get(anyString, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any); result = "";
		}};
		
		Page page = new Page(objWithSubstitutableUrl);
		page.execute(compiler, browser, variables, null, database);
		
		new Verifications() {{
			database.store(substitutedUrl, null, 0);
			browser.get(substitutedUrl, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any);
		}};
	}
}
