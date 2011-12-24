package net.caustic.template;

import static org.junit.Assert.*;
import static net.caustic.regexp.StringTemplate.*;

import java.util.Hashtable;

import mockit.Expectations;
import mockit.NonStrict;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.template.HashtableSubstitution;
import net.caustic.template.HashtableSubstitutionOverwriteException;
import net.caustic.template.HashtableTemplate;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StaticStringTemplate;
import net.caustic.util.StringMap;

import org.junit.Before;
import org.junit.Test;

public class HashtableTemplateTest {
	@NonStrict StringMap context;
	private Encoder encoder;
	private RegexpCompiler compiler;
		
	@Before
	public void setUp() throws Exception {
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		compiler = new JavaUtilRegexpCompiler(encoder);
	}

	@Test
	public void testSizeStartsZero() {
		assertEquals(0, new HashtableTemplate().size());
	}
	
	@Test
	public void testSubSuccessful() throws Exception {
		HashtableTemplate hash = new HashtableTemplate();
		
		new Expectations() {{
			context.get("encoded key"); result = "this key should be encoded";
			context.get("encoded value"); result = "this value should be encoded";
			context.get("not encoded key"); result ="this key should not be encoded";
			context.get("not encoded value"); result = "this value should not be encoded";
		}};
		
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
		
		HashtableSubstitution exc = hash.sub(context);
		
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
		
		new Expectations() {{
			context.get("bill clinton"); result ="charmer";
			context.get("george clinton"); result = "chiller";			
		}};
		
		hash1.put(new StaticStringTemplate("george clinton"),
				compiler.newTemplate("{{{george clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		hash2.put(new StaticStringTemplate("bill clinton"), 
				compiler.newTemplate("{{{bill clinton}}}", ENCODED_PATTERN, UNENCODED_PATTERN));
		
		hash1.extend(hash2, true);
		
		HashtableSubstitution sub = hash1.sub(context);
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
		
		new Expectations() {{
			context.get("overwriting"); result = "key";			
		}};
		
		hash.put(new StaticStringTemplate("key"),
				new StaticStringTemplate("value"));
		
		hash.put(compiler.newTemplate("{{{overwriting}}}", ENCODED_PATTERN,
						UNENCODED_PATTERN), 
				new StaticStringTemplate("value"));
				
		hash.sub(context);
	}
}
