package net.microscraper.instruction;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.DatabaseView;
import net.microscraper.http.CookieManager;
import net.microscraper.http.HttpBrowser;
import net.microscraper.http.JavaNetCookieManager;
import net.microscraper.http.JavaNetHttpRequester;
import net.microscraper.http.RateLimitManager;
import net.microscraper.regexp.JavaUtilRegexpCompiler;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.JavaNetHttpUtils;
import net.microscraper.util.StaticStringTemplate;


import org.junit.Before;
import org.junit.Test;

public class LoadLocalTest {
	
	@Mocked private DatabaseView input;
	@Injectable private HttpBrowser mockBrowser;
	private RegexpCompiler compiler;
	private Load load;
	private StringTemplate url;
	
	@Before
	public void setUp() throws Exception {
		url = new StaticStringTemplate(randomString());
		compiler  = new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8));
		load = new Load(url);
		
		new NonStrictExpectations() {{
			mockBrowser.copy(); result = mockBrowser;
		}};
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			mockBrowser.head(url.toString(), (Hashtable) any);
		}};
		load.setMethod(HttpBrowser.HEAD);
		LoadResult result = load.execute(mockBrowser, input);
		assertNotNull(result.getResponseBody());
		assertEquals(url.toString(), result.getUrl());
	}

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		final String response = randomString();
		new Expectations() {{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
		}};
		LoadResult result = load.execute(mockBrowser, input);
		assertEquals(url.toString(), result.getUrl());
		assertEquals(response, result.getResponseBody());
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final StringTemplate url = compiler.newTemplate("http://www.google.com/?q={{" + name + "}}", StringTemplate.DEFAULT_ENCODED_PATTERN, StringTemplate.DEFAULT_NOT_ENCODED_PATTERN);
		final String subbed = "http://www.google.com/?q=" + value;
		new Expectations() {{
			input.get(name); result = value;
			mockBrowser.get(subbed, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(url);
		LoadResult result = load.execute(mockBrowser, input);
		assertEquals(subbed, result.getUrl());
	}
	
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "");
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(HttpBrowser.POST);
		load.execute(mockBrowser, input);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final StringTemplate postData = compiler.newTemplate(randomString(), StringTemplate.DEFAULT_ENCODED_PATTERN, StringTemplate.DEFAULT_NOT_ENCODED_PATTERN);
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(mockBrowser, input);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final StringTemplate postData = compiler.newTemplate("{{" + key + "}}",
				StringTemplate.DEFAULT_ENCODED_PATTERN, StringTemplate.DEFAULT_NOT_ENCODED_PATTERN);
		new Expectations() {{
			input.get(key); result = value;
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(mockBrowser, input);
	}
	

	@Test
	public void testSendsResponseBodyToFind(@Mocked final Find find) throws Exception {
		final String response = randomString();
		new Expectations() {
			FindResult findResult;
			{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
			find.execute(response, input); result = findResult;
		}};
		
		Instruction instruction = new Instruction(load);
		instruction.setChildren(new Instruction[] { new Instruction(find) });

		
		InstructionResult result = instruction.execute(null, input, mockBrowser);
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getChildren().length);
		
		result.getChildren()[0].execute(result.getResults()[0], input, mockBrowser);
	}
}
