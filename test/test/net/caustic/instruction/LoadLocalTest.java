package net.caustic.instruction;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.caustic.database.DatabaseView;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StaticStringTemplate;


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
		InstructionResult result = load.execute(null, input, mockBrowser);
		assertNotNull(result.getResults());
		assertEquals(url.toString(), result.getName());
	}

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		final String response = randomString();
		new Expectations() {{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
		}};
		InstructionResult result = load.execute(null, input, mockBrowser);
		assertEquals(url.toString(), result.getName());
		assertEquals(response, result.getResults()[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final String value = randomString();
		final String name = "query";
		final StringTemplate url = compiler.newTemplate("http://www.google.com/?q={{" + name + "}}", StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		final String subbed = "http://www.google.com/?q=" + value;
		new Expectations() {{
			input.get(name); result = value;
			mockBrowser.get(subbed, (Hashtable) any, (Pattern[]) any);
		}};
		Load load = new Load(url);
		InstructionResult result = load.execute(null, input, mockBrowser);
		assertEquals(subbed, result.getName());
	}
	
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "");
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(HttpBrowser.POST);
		load.execute(null, input, mockBrowser);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final StringTemplate postData = compiler.newTemplate(randomString(), StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString());
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, input, mockBrowser);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final StringTemplate postData = compiler.newTemplate("{{" + key + "}}",
				StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		new Expectations() {{
			input.get(key); result = value;
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, input, mockBrowser);
	}
	

	@Test
	public void testSendsResponseBodyToFind(@Mocked("execute") final Find find) throws Exception {
		final String response = randomString();
		new Expectations() {
			{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any); result = response;
			find.execute(response, input, mockBrowser);
		}};
		
		load.then(find);
		
		InstructionResult result = load.execute(null, input, mockBrowser);
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getChildren().length);
		
		result.getChildren()[0].execute(result.getResults()[0], input, mockBrowser);
	}
}
