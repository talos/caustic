package net.microscraper.client;

import static org.junit.Assert.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Database;
import net.microscraper.instruction.Find;
import net.microscraper.instruction.Instruction;
import net.microscraper.instruction.Load;
import net.microscraper.json.JsonObject;
import net.microscraper.json.JsonParser;
import net.microscraper.mustache.MustachePattern;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.uri.UriFactory;
import net.microscraper.uri.Uri;
import net.microscraper.util.NameValuePair;
import static net.microscraper.test.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

public class MicroscraperTest {
	
	@Mocked private RegexpCompiler compiler;
	@Mocked private Browser browser;
	@Mocked private UriFactory uriFactory;
	@Mocked private JsonParser jsonParser;
	@Mocked private Database database;
	
	Microscraper microscraper;
	
	private static final String defaultName = "query";
	private static final String defaultValue = "hello";
	
	private static final String uriString = randomString();
	//private static final String uriWithDefaultsString = randomString();
	
	private static final String urlString = "http://www.google.com/";
	private static final String urlWithDefaultsStringRaw = "http://www.google.com/search?q={{" + defaultName + "}}";
	private static final String urlWithDefaultsStringCompiled = "http://www.google.com/search?q=" + defaultValue;
	
	private static final String patternString = "[\\w]*\\sLucky";
	private static final String patternWithDefaultsStringRaw = "{{" + defaultName + "}}\\s+(\\w+)";
	private static final String patternWithDefaultsStringCompiled = defaultValue + "\\s+(\\w+)";
	
	private static final String contentString = randomString();
	//private static final String contentWithDefaultsString = randomString();
	
	private static final String jsonString =
			"{\"" + Load.URL + "\":\"" + urlString + "\",\"" + Instruction.FIND + "\":{\"" + MustachePattern.PATTERN + "\":\"" + patternString + "\"} }";
	private static final String jsonWithDefaultsString =
			"{\"" + Load.URL + "\":\"" + urlWithDefaultsStringRaw + "\",\""+ Instruction.FIND +"\":{\""+MustachePattern.PATTERN+"\":\"" + patternWithDefaultsStringRaw + "\"} }";
	private static final Hashtable defaultsHash = new Hashtable();
	private static final Hashtable[] defaultsHashArray = new Hashtable[] {
		defaultsHash,
		defaultsHash
	};
	
	@Before
	public void setUp() throws Exception {
		microscraper = new Microscraper(compiler, browser, uriFactory, jsonParser, database);
		defaultsHash.put(defaultName, defaultValue);
	}
	
	private void expectJson() throws Exception {
		new Expectations() {
			JsonObject page, find;
			Pattern pattern;
			String[] match = new String[] { randomString() };
			{
				jsonParser.parse((Uri) any, jsonString); result = page;
				page.getString(Load.URL); result = urlString;
				page.getJsonObject(Instruction.FIND); result = find;
				find.getString(MustachePattern.PATTERN); result = patternString;
				browser.get(urlString, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any); result = contentString;
				compiler.compile(patternString, false, false, true); result = pattern;
				pattern.match(contentString, Find.ENTIRE_MATCH, Find.FIRST_MATCH, Find.LAST_MATCH); result = match;
				database.store(urlString, (String) withNull(), withEqual(0)); result = 0;
				database.store(urlString, withEqual(0), patternString, match[0], withEqual(0)); result = 1;
			}
		};
	}
	
	private void recordJsonWithDefaults() throws Exception {
		new Expectations() {
			JsonObject page, find;
			Pattern pattern;
			String[] match = new String[] { randomString() };
			{
				jsonParser.parse((Uri) any, jsonWithDefaultsString); result = page;
				page.getString(Load.URL); result = urlWithDefaultsStringRaw;
				page.getJsonObject(Instruction.FIND); result = find;
				find.getString(MustachePattern.PATTERN); result = patternWithDefaultsStringRaw;
				browser.get(urlWithDefaultsStringCompiled, (NameValuePair[]) any, (NameValuePair[]) any, (Pattern[]) any); result = contentString;
				compiler.compile(patternWithDefaultsStringCompiled, false, false, true); result = pattern;
				pattern.match(contentString, Find.ENTIRE_MATCH, Find.FIRST_MATCH, Find.LAST_MATCH); result = match;
				database.store(urlString, (String) withNull(), withEqual(0)); result = 0;
				database.store(urlString, withEqual(0), patternString, match[0], withEqual(0)); result = 1;
			}
		};
	}
	
	@Test
	public void testMicroscraper() {
		 /* This will fail if setUp() fails. */
	}
	
	@Test
	public void testScrapeFromJsonString() throws DeserializationException, IOException, DatabaseException {
		microscraper.scrapeFromJson(jsonString);
	}
	
	@Test
	public void testScrapeFromJsonStringAndDefaultsHash() throws DeserializationException, IOException, DatabaseException {
		microscraper.scrapeFromJson(jsonString, defaultsHash);
	}

	@Test
	public void testScrapeFromJsonStringAndDefaultsHashArray() throws DeserializationException, IOException, DatabaseException {
		microscraper.scrapeFromJson(jsonString, defaultsHashArray);
	}
	
	@Test
	public void testScrapeFromUriString() throws DeserializationException, IOException, DatabaseException {
		new Expectations() {
			Uri uri;
			{
				uriFactory.fromString(uriString); result = uri;
				uri.load(); result = jsonString;
			}
		};
		
		microscraper.scrapeFromUri(jsonString);
	}

	@Test
	public void testScrapeFromUriStringAndDefaultsHash() {
		new Expectations() {
			Uri uri;
			{
				uriFactory.fromString(uriString); result = uri;
				uri.load(); result = jsonWithDefaultsString;
			}
		};

		microscraper.scrapeFromUri(uriString, defaultsHash);
	}

	@Test
	public void testScrapeFromUriStringAndDefaultsHashArray() {
		new Expectations() {
			Uri uri;
			{
				uriFactory.fromString(uriString); result = uri;
				uri.load(); result = jsonWithDefaultsString;
			}
		};
		
		microscraper.scrapeFromUri(uriString, defaultsHashArray);
	}

	@Test
	public void testSetRateLimit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTimeout() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetMaxResponseSize() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegister() {
		fail("Not yet implemented");
	}

}
