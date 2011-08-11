package net.microscraper.interfaces.json;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.impl.json.JSONME;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.impl.uri.JavaNetURI;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.file.FileLoader;
import net.microscraper.interfaces.regexp.RegexpCompiler;
import net.microscraper.interfaces.uri.URIInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JSONInterfaceTest {
	private final Class<JSONInterface> klass;
	private JSONInterface jsonInterface;
	
	private @Mocked URIInterface location1, location2;
	private static final String path1 = "this/is/path/1";
	private static final String path2 = "this/is/path/2";
	
	private @Mocked FileLoader fileLoader;
	private @Mocked Browser browser;
	
	public JSONInterfaceTest(final Class<JSONInterface> klass) throws Exception {
		this.klass = klass;
	}
	
	@Parameters
	public static Collection<Class[]> implementations() {
		return Arrays.asList(new Class[][] {
				{ JSONME.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		jsonInterface = klass.getConstructor(FileLoader.class, Browser.class)
				 .newInstance(fileLoader, browser);
		new NonStrictExpectations() {{
				location1.isFile(); result = true;
				location2.isFile(); result = true;
				
				location1.getSchemeSpecificPart(); result = path1;
				location2.getSchemeSpecificPart(); result = path2;
		}};
	}
	
	@Test
	public void testLoadWithoutReferences() throws Exception {		
		new NonStrictExpectations() {{	
			fileLoader.load(path1); result = "{ \"name\" = \"value\" }";
		}};
		
		assertEquals("Not generating JSON Object with value.",
				"value", jsonInterface.load(location1).getString("name"));
	}
	
	@Test
	public void testLoadWithReferences() throws Exception {
		new NonStrictExpectations() {{	
			location1.resolve(path2); result = location2;
			fileLoader.load(path1); result = "{ \"$ref\" = \"" + path2 + "\" }";
			fileLoader.load(path2); result = "{ \"name\" = \"value\" }";
		}};
		assertEquals("Not generating JSON Object with referenced value.",
				"value", jsonInterface.load(location1).getString("name"));
	}
}
