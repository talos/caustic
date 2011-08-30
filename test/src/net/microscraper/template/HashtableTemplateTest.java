package net.microscraper.template;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import net.microscraper.database.Database;
import net.microscraper.database.HashtableDatabase;
import net.microscraper.database.Scope;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.IntUUIDFactory;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {
	Database database;
	Scope scope;
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
		database = new HashtableDatabase(new IntUUIDFactory());
		scope = database.getScope();
		database.storeOneToOne(scope, key, value);
		database.storeOneToOne(scope, multiWordKey, multiWordValue);
		database.storeOneToOne(scope, alreadyEncodedKey, alreadyEncodedValue);
	}

	@Test
	public void testSizeStartsZero() {
		assertEquals(0, new HashtableTemplate());
	}

	@Test
	public void testSubSuccessful() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		hash.put(new Template("{{" + key + "}}", "{{" ,"}}", database),
				new Template("{{" + value + "}}", "{{", "}}", database));
		hash.put(new Template("{{" + multiWordKey + "}}", "{{" ,"}}", database),
				new Template("{{" + multiWordValue + "}}", "{{", "}}", database));
		hash.put(new Template("{{" + alreadyEncodedKey + "}}", "{{" ,"}}", database),
				new Template("{{" + alreadyEncodedValue + "}}", "{{", "}}", database));
		Execution exc = hash.sub(scope);
		
		assertTrue(exc.isSuccessful());
		
		exc.getExecuted();
		
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
