package net.caustic.template;

import static org.junit.Assert.*;
import static net.caustic.regexp.StringTemplate.*;

import java.util.Hashtable;

import net.caustic.database.Database;
import net.caustic.database.MemoryDatabase;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.scope.Scope;
import net.caustic.template.HashtableSubstitution;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.template.HashtableTemplate;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StaticStringTemplate;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {	
	private Database db;
	private Scope scope;
	private Encoder encoder;
	private RegexpCompiler compiler;
		
	@Before
	public void setUp() throws Exception {
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		compiler = new JavaUtilRegexpCompiler(encoder);
		db = new MemoryDatabase();
		scope = db.newDefaultScope();
	}

	@Test
	public void testSizeStartsZero() {
		assertEquals(0, new HashtableTemplate().size());
	}
	
	@Test
	public void testSubSuccessful() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		
		db.put(scope, "encoded key", "this key should be encoded");
		db.put(scope, "encoded value", "this value should be encoded");
		db.put(scope, "not encoded key", "this key should not be encoded");
		db.put(scope, "not encoded value", "this value should not be encoded");
		
		hash.put(compiler.newTemplate("{{encoded key}}",
				ENCODED_PATTERN, UNENCODED_PATTERN),
				new StaticStringTemplate("value"));
		
		hash.put(new StaticStringTemplate("multiple word key"), 
				compiler.newTemplate("{{" + "encoded value" + "}}",
						ENCODED_PATTERN, UNENCODED_PATTERN ));
		
		hash.put(compiler.newTemplate("{{{" + "not encoded key" + "}}}",
				ENCODED_PATTERN, UNENCODED_PATTERN ),
				compiler.newTemplate("{{{" + "not encoded value" + "}}}", 
						ENCODED_PATTERN, UNENCODED_PATTERN));
		
		HashtableSubstitution exc = hash.sub(db, scope);
		
		assertFalse(exc.isMissingTags());
		
		@SuppressWarnings("unchecked")
		Hashtable<String, String> subbed = exc.getSubstituted();
		
		assertTrue(subbed.containsKey("this+key+should+be+encoded"));
		assertEquals("value", subbed.get("this+key+should+be+encoded"));
		
		assertTrue(subbed.containsKey("multiple word key"));
		assertEquals("this+value+should+be+encoded", subbed.get("multiple word key"));
		
		assertTrue(subbed.containsKey("this key should not be encoded"));
		assertEquals("this value should not be encoded", subbed.get("this key should not be encoded"));

	}

	@Test
	public void testExtend() throws Exception {
		HashtableTemplate hash1 = new HashtableTemplate();
		HashtableTemplate hash2 = new HashtableTemplate();
		
		db.put(scope, "bill clinton", "charmer");
		db.put(scope, "george clinton", "chiller");
		
		hash1.put(new StaticStringTemplate("george clinton"),
				compiler.newTemplate("{{{george clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		hash2.put(new StaticStringTemplate("bill clinton"), 
				compiler.newTemplate("{{{bill clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		
		hash1.extend(hash2, true);
		
		HashtableSubstitution sub = hash1.sub(db, scope);
		assertFalse(sub.isMissingTags());
		
		@SuppressWarnings("unchecked")
		Hashtable<String, String> subbed = sub.getSubstituted();
		assertTrue(subbed.containsKey("george clinton"));
		assertEquals("chiller", subbed.get("george clinton"));
		assertTrue(subbed.containsKey("bill clinton"));
		assertEquals("charmer", subbed.get("bill clinton"));
	}

	@Test(expected = HashtableSubstitutionOverwriteException.class)
	public void testOverwriteException() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		
		db.put(scope, "overwriting", "key");
		
		hash.put(new StaticStringTemplate("key"),
				new StaticStringTemplate("value"));
		
		hash.put(compiler.newTemplate("{{{overwriting}}}", ENCODED_PATTERN,
						UNENCODED_PATTERN), 
				new StaticStringTemplate("value"));
				
		hash.sub(db, scope);
	}
}
