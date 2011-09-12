package net.microscraper.template;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.util.Hashtable;

import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {
	DatabaseView input;
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
		input = new InMemoryDatabaseView();
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
				StringTemplate.staticTemplate(value));
		hash.put(StringTemplate.staticTemplate(multiWordKey), 
				new StringTemplate("{{" + multiWordKey + "}}", "{{" ,"}}"));
		hash.put(new StringTemplate("{{" + alreadyEncodedKey + "}}", "{{" ,"}}"),
				new StringTemplate("{{" + alreadyEncodedKey + "}}", "{{" ,"}}"));
		HashtableSubstitution exc = hash.sub(input);
		
		assertFalse(exc.isMissingTags());
		
		@SuppressWarnings("unchecked")
		Hashtable<String, String> subbed = exc.getSubstituted();
		
		assertTrue(subbed.containsKey(value));
		assertEquals(value, subbed.get(value));
		
		assertTrue(subbed.containsKey(multiWordKey));
		assertEquals(multiWordValue, subbed.get(multiWordKey));
		
		assertTrue(subbed.containsKey(alreadyEncodedValue));
		assertEquals(alreadyEncodedValue, subbed.get(alreadyEncodedValue));

	}

	@Test
	public void testMerge() throws Exception {
		HashtableTemplate hash1 = new HashtableTemplate();
		HashtableTemplate hash2 = new HashtableTemplate();
		
		hash1.put(StringTemplate.staticTemplate(key),
				new StringTemplate("{{" + key + "}}", "{{", "}}"));
		hash2.put(StringTemplate.staticTemplate(multiWordKey), 
				new StringTemplate("{{" + multiWordKey + "}}", "{{", "}}"));
		
		hash1.merge(hash2);
		
		HashtableSubstitution sub = hash1.sub(input);
		assertFalse(sub.isMissingTags());
		
		@SuppressWarnings("unchecked")
		Hashtable<String, String> subbed = sub.getSubstituted();
		assertTrue(subbed.containsKey(key));
		assertEquals(value, subbed.get(key));
		assertTrue(subbed.containsKey(multiWordKey));
		assertEquals(multiWordValue, subbed.get(multiWordKey));
	}

	@Test(expected = HashtableSubstitutionOverwriteException.class)
	public void testOverwriteException() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		
		hash.put(StringTemplate.staticTemplate(value),
				new StringTemplate("{{" + key + "}}", "{{", "}}"));
		hash.put(new StringTemplate("{{" + key + "}}", "{{", "}}"), 
				new StringTemplate("{{" + multiWordKey + "}}", "{{", "}}"));
				
		hash.sub(input);
	}
}
