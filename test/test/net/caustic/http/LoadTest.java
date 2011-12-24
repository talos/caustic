package net.caustic.http;

import static org.junit.Assert.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrict;
import net.caustic.Load;
import net.caustic.Response;
import net.caustic.http.BrowserResponse;
import net.caustic.http.HttpBrowser;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.template.HashtableTemplate;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StringMap;


import org.junit.Before;
import org.junit.Test;

public class LoadTest {
	
	private static HashtableTemplate empty = new HashtableTemplate();
	
	@NonStrict private StringMap tags;
	@Mocked private HttpBrowser browser;
	private RegexpCompiler compiler;
	
	@Before
	public void setUp() throws Exception {
		compiler  = new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8));
	}
	
	@Test
	public void testLoadRequiresForce() throws Exception {
		Load load = new Load("description", "uri", compiler.newTemplate("url"), new String[] {}, "get",
				empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, false);
		assertTrue(response.wait);
	}
	/*
	@Test
	public void testHeadCausesHeadWithZeroLengthResponse() throws Exception {
		
		Load load = new Load("description", "uri", compiler.newTemplate("url"), new String[] {}, "head",
				new HashtableTemplate(), new HashtableTemplate(), new HashtableTemplate());
		new Expectations() {{
			browser.request("url", "head", (Hashtable) any, (String[]) any, null);
				result = new BrowserResponse(null, null);
		}};
		
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertFalse(response.wait);
		
		assertNotNull(response.content);
		assertEquals("", response.content);
	}*/

	@Test
	public void testGetCausesGetWithOneResponse() throws Exception {
		new Expectations() {{
			browser.request("swag", "get", (Hashtable) any, (String[]) any, anyString);
				result = new BrowserResponse("everything is purple", new String[] {});
		}};
		
		Load load = new Load("description", "uri", compiler.newTemplate("swag"), new String[] {},
				"get", empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		
		assertNotNull(response.content);
		assertEquals("everything is purple", response.content);
	}
	
	@Test
	public void testUrlIsSubstituted() throws Exception {
		
		new Expectations() {{
			tags.get("roses"); result = "red";
			browser.request("red", "get", (Hashtable) any, (String[]) any, anyString);
				result = new BrowserResponse("and violets are blue", new String[] {});
		}};
		Load load = new Load("description", "uri", compiler.newTemplate("{{roses}}"), new String[] {},
				"get", empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertEquals(response.content, "and violets are blue");
	}
	

	@Test
	public void testUrlIsSubstitutedEscaped() throws Exception {
		new Expectations() {{
			tags.get("roses"); result = "are red";
			browser.request("are+red", "get", (Hashtable) any, (String[]) any, anyString);
				result = new BrowserResponse("and violets are blue", new String[] {});
		}};
		
		Load load = new Load("description", "uri", compiler.newTemplate("{{roses}}"), new String[] {},
				"get", empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertEquals(response.content, "and violets are blue");
	}
	
	
	@Test
	public void testPostMethodSetsZeroLengthPost() throws Exception {
		new Expectations() {{
			browser.request("url", "post", (Hashtable) any, (String[]) any, "");
				result = new BrowserResponse("and violets are blue", new String[] {});
		}};
		
		Load load = new Load("description", "uri", compiler.newTemplate("url"), new String[] {},
				"post", empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertEquals(response.content, "and violets are blue");
	}
	
	@Test
	public void testPostDataSetsPostMethod() throws Exception {
		new Expectations() {{
			browser.request("url", "post", (Hashtable) any, (String[]) any, "");
				result = new BrowserResponse("and violets are blue", new String[] {});
		}};
		
		Load load = new Load("description", "uri", compiler.newTemplate("url"), new String[] {},
				"get", empty, empty, empty);
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertEquals(response.content, "and violets are blue");
	}
	
	@Test
	public void testPostDataIsSubbed() throws Exception {
		new Expectations() {{
			tags.get("post"); result = "postal data";
			browser.request("url", "post", (Hashtable) any, (String[]) any, "postal+data");
				result = new BrowserResponse("and violets are blue", new String[] {});
		}};
		
		Load load = new Load("description", "uri", compiler.newTemplate("url"), new String[] {},
				"post", empty, empty, compiler.newTemplate("{{post}}"));
		Response response = load.execute("id", tags, new String[] {}, browser, true);
		assertEquals(response.content, "and violets are blue");
	}
}
