package net.microscraper.json;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.client.Browser;
import net.microscraper.file.FileLoader;
import net.microscraper.uri.Uri;

import org.junit.Before;
import org.junit.Test;

public abstract class JsonParserTest {
	private JsonParser jsonInterface;
	
	private @Mocked Uri location1, location2;
	private static final String path1 = "this/is/path/1";
	private static final String path2 = "this/is/path/2";
	
	private @Mocked FileLoader fileLoader;
	private @Mocked Browser browser;
	
	
	@Before
	public void setUp() throws Exception {
		jsonInterface = getJsonParser();
		new NonStrictExpectations() {{
				onInstance(location1).toString(); result = path1;
				onInstance(location2).toString(); result = path2;
		}};
	}
	
	protected abstract JsonParser getJsonParser();

	@Test
	public void testLoadWithoutReferences() throws Exception {		
		new NonStrictExpectations() {{	
			fileLoader.load(location1); result = "{ \"name\" = \"value\" }";
		}};
		
		assertEquals("Not generating JSON Object with value.",
				"value", jsonInterface.load(location1).getString("name"));
	}
	
	@Test
	public void testLoadWithReferences() throws Exception {
		new NonStrictExpectations() {{	
			onInstance(location1).resolve(path2); result = location2;
			fileLoader.load(location1); result = "{ \"$ref\" = \"" + path2 + "\" }";
			fileLoader.load(location2); result = "{ \"name\" = \"value\" }";
		}};
		assertEquals("Not generating JSON Object with referenced value.",
				"value", jsonInterface.load(location1).getString("name"));
	}
}
