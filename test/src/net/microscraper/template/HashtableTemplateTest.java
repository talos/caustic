package net.microscraper.template;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;
import net.microscraper.util.StringMap;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {
	StringMap input;
	Encoder encoder;
	
	String key, value, multiWordKey, multiWordValue, alreadyEncodedKey, alreadyEncodedValue;
	
	@Before
	public void setUp() throws Exception {
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		key = randomString();
		value = randomString();
		multiWordKey = randomString() + " " + randomString();
		multiWordValue = randomString() + " " + randomString();
		alreadyEncodedKey = encoder.encode(multiWordKey);
		alreadyEncodedValue = encoder.encode(multiWordValue);
		input = new StringMap();
		input.put(key, value);
		input.put(multiWordKey, multiWordValue);
		input.put(alreadyEncodedKey, alreadyEncodedValue);
	}

	@Test
	public void testSizeStartsZero() {
		assertEquals(0, new HashtableTemplate().size());
	}

	@Test
	public void testSubSuccessful() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		hash.put(new StringTemplate("{{" + key + "}}", "{{" ,"}}"),
				new StringTemplate("{{" + value + "}}", "{{", "}}"));
		hash.put(new StringTemplate("{{" + multiWordKey + "}}", "{{" ,"}}"),
				new StringTemplate("{{" + multiWordValue + "}}", "{{", "}}"));
		hash.put(new StringTemplate("{{" + alreadyEncodedKey + "}}", "{{" ,"}}"),
				new StringTemplate("{{" + alreadyEncodedValue + "}}", "{{", "}}"));
		HashtableSubstitution exc = hash.sub(input);
		
		assertFalse(exc.isMissingTags());
		
		exc.getSubstituted();
		
	}
	
	@Test
	public void testSubEncoded() {
		fail("Not yet implemented");
	}

	@Test
	public void testMerge() {
		fail("Not yet implemented");
	}

}
