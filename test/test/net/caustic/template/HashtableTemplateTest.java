package net.caustic.template;

import static org.junit.Assert.*;
import static net.caustic.regexp.StringTemplate.*;

import java.util.Hashtable;

import net.caustic.database.DatabaseView;
import net.caustic.database.InMemoryDatabase;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.template.HashtableSubstitution;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.template.HashtableTemplate;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StaticStringTemplate;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {	
	private DatabaseView view;
	private Encoder encoder;
	private RegexpCompiler compiler;
		
	@Before
	public void setUp() throws Exception {
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		compiler = new JavaUtilRegexpCompiler(encoder);
		view = new DatabaseView(new InMemoryDatabase());
	}

	@Test
	public void testSizeStartsZero() {
		assertEquals(0, new HashtableTemplate().size());
	}
	
	@Test
	public void testSubSuccessful() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		
		view.put("encoded key", "this key should be encoded");
		view.put("encoded value", "this value should be encoded");
		view.put("not encoded key", "this key should not be encoded");
		view.put("not encoded value", "this value should not be encoded");
		
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
		
		HashtableSubstitution exc = hash.sub(view);
		
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
	public void testMerge() throws Exception {
		HashtableTemplate hash1 = new HashtableTemplate();
		HashtableTemplate hash2 = new HashtableTemplate();
		
		view.put("bill clinton", "charmer");
		view.put("george clinton", "chiller");
		
		hash1.put(new StaticStringTemplate("george clinton"),
				compiler.newTemplate("{{{george clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		hash2.put(new StaticStringTemplate("bill clinton"), 
				compiler.newTemplate("{{{bill clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		
		hash1.merge(hash2);
		
		HashtableSubstitution sub = hash1.sub(view);
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
		
		view.put("overwriting", "key");
		
		hash.put(new StaticStringTemplate("key"),
				new StaticStringTemplate("value"));
		
		hash.put(compiler.newTemplate("{{{overwriting}}}", ENCODED_PATTERN,
						UNENCODED_PATTERN), 
				new StaticStringTemplate("value"));
				
		hash.sub(view);
	}
}
