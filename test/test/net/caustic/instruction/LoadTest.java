package net.caustic.instruction;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.caustic.database.Database;
import net.caustic.http.HttpBrowser;
import net.caustic.instruction.Find;
import net.caustic.instruction.InstructionResult;
import net.caustic.instruction.Load;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StaticStringTemplate;


import org.junit.Before;
import org.junit.Test;

public class LoadTest {
	
	@Mocked private Database db;
	@Mocked private Scope scope;
	@Injectable private HttpBrowser mockBrowser;
	private RegexpCompiler compiler;
	private Load load;
	private StringTemplate url;
	
	@Before
	public void setUp() throws Exception {
		url = new StaticStringTemplate(randomString());
		compiler  = new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8));
		load = new Load(url);
	}
	
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		new Expectations() {{
			mockBrowser.head(url.toString(), (Hashtable) any, db, scope);
		}};
		load.setMethod(HttpBrowser.HEAD);
		InstructionResult result = load.execute(null, db, scope, mockBrowser);
		assertNotNull(result.getResults());
		//assertEquals(url.toString(), result.getName());
	}

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		final String response = randomString();
		new Expectations() {{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any, db, scope); result = response;
		}};
		InstructionResult result = load.execute(null, db, scope, mockBrowser);
		//assertEquals(url.toString(), result.getName());
		assertEquals(response, result.getResults()[0]);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		final StringTemplate url = compiler.newTemplate("http://www.google.com/?q={{roses}}");
		final String subbed = "http://www.google.com/?q=red";
		new Expectations() {{
			db.get(scope, "roses"); result = "red";
			mockBrowser.get(subbed, (Hashtable) any, (Pattern[]) any, db, scope);
		}};
		Load load = new Load(url);
		InstructionResult result = load.execute(null, db, scope, mockBrowser);
		//assertEquals(subbed, result.getName());
	}
	

	@Test
	public void testUrlIsSubstitutedEscaped() throws Exception {
		final StringTemplate url = compiler.newTemplate("http://www.google.com/?q={{query}}");
		final String subbed = "http://www.google.com/?q=a+few+words";
		new Expectations() {{
			db.get(scope, "query"); result = "a few words";
			mockBrowser.get(subbed, (Hashtable) any, (Pattern[]) any, db, scope);
		}};
		Load load = new Load(url);
		InstructionResult result = load.execute(null, db, scope, mockBrowser);
		//assertEquals(subbed, result.getName());
	}
	
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, "", db, scope);
				$ = "Post data should be a zero-length string.";
		}};
		load.setMethod(HttpBrowser.POST);
		load.execute(null, db, scope, mockBrowser);
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		final StringTemplate postData = compiler.newTemplate(randomString(), StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		new Expectations() {{
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, postData.toString(), db, scope);
				$ = "Post data should be set by setting post data.";
		}};
		load.setPostData(postData);
		load.execute(null, db, scope, mockBrowser);
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		final String key = randomString();
		final String value = randomString();
		final StringTemplate postData = compiler.newTemplate("{{" + key + "}}",
				StringTemplate.ENCODED_PATTERN, StringTemplate.UNENCODED_PATTERN);
		new Expectations() {{
			db.get(scope, key); result = value;
			mockBrowser.post(url.toString(), (Hashtable) any, (Pattern[]) any, value, db, scope);
				$ = "Post data should be substituted.";
		}};
		load.setPostData(postData);
		load.execute(null, db, scope, mockBrowser);
	}
	

	@Test
	public void testSendsResponseBodyToFind(@Mocked("execute") final Find find) throws Exception {
		final String response = randomString();
		new Expectations() {
			{
			mockBrowser.get(url.toString(), (Hashtable) any, (Pattern[]) any, db, scope); result = response;
			find.execute(response, db, scope, mockBrowser);
		}};
		
		load.then(find);
		
		InstructionResult result = load.execute(null, db, scope, mockBrowser);
		
		assertTrue(result.isSuccess());
		assertEquals(1, result.getChildren().length);
		
		result.getChildren()[0].execute(result.getResults()[0], db, scope, mockBrowser);
	}
}
