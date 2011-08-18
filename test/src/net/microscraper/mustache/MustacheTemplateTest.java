package net.microscraper.mustache;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.util.Encoder;
import net.microscraper.util.Substitution;
import net.microscraper.util.Variables;
import static net.microscraper.test.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

public class MustacheTemplateTest {
	
	@Mocked Variables variables, empty;
	@Mocked Encoder encoder;
	
	private static final String key = "template";
	private static final String value = "has been substituted";
	private static final String encodedValue = "has+been+substituted";
	private static final String validTemplateRaw = "A valid {{" + key + "}}.";
	private static final String validTemplateCompiled = "A valid " + value + ".";
	private static final String validTemplateCompiledEncoded = "A valid " + encodedValue + ".";
	
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
	
	@Test(expected = MustacheCompilationException.class)
	public void testInvalidMustacheDoesNotCompile() throws MustacheCompilationException {
		MustacheTemplate.compile("{{");
	}
	
	@Test
	public void testSubSuccessful() throws MustacheCompilationException {
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		Substitution sub = template.sub(variables);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiled, sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessful() throws MustacheCompilationException {
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		Substitution sub = template.sub(empty);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] { key }, sub.getMissingVariables());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws MustacheCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = encodedValue;
		}};
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		Substitution sub = template.sub(variables, encoder, encoding);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiledEncoded, sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws MustacheCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = encodedValue; times = 0;
		}};
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		Substitution sub = template.sub(empty, encoder, encoding);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] {key}, sub.getMissingVariables());
	}
	
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testSubEncodedInvalidEncoding() throws MustacheCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = new UnsupportedEncodingException();
		}};
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		template.sub(variables, encoder, encoding);
	}

	@Test
	public void testToString() throws MustacheCompilationException {
		MustacheTemplate template = MustacheTemplate.compile(validTemplateRaw);
		assertEquals(validTemplateRaw, template.toString());
	}

}
