package net.microscraper.mustache;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.util.BasicVariables;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class MustacheTemplateTest {
	
	@Mocked Variables variables;
	@Mocked Encoder encoder;
	
	private static final String key = "template";
	private static final String value = "has been substituted";
	private static final String validTemplateRaw = "A valid {{" + key + "}}.";
	private static final String validTemplateCompiled = "A valid " + value + ".";
	
	@Before
	public void setup() {
		new NonStrictExpectations() {{
			variables.containsKey(key); result = true;
			variables.get(key); result = value;
		}};
	}
	
	@Test
	public void testMustacheTemplateCompiles() throws MustacheCompilationException {
		MustacheTemplate.compile(validTemplateRaw);
	}

	@Test
	public void testMustacheTemplateCompilesEncoded() throws MustacheCompilationException {
		MustacheTemplate.compile(validTemplateRaw, encoder);
	}
	
	@Test
	public void testSubSuccessful() throws MustacheCompilationException {
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		assertTrue(template.sub(variables));
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetSubbedThrowsIllegalState() {
		new MustacheTemplate(validTemplateRaw).getSubbed();
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetMissingThrowsIllegalState() {
		new MustacheTemplate(validTemplateRaw).getMissing();
	}
	
	@Test
	public void testSubEncoded() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
