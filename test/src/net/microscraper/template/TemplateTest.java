package net.microscraper.template;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.template.Template;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;
import static net.microscraper.util.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

public class TemplateTest {
	
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
	public void testMustacheTemplateCompiles() throws TemplateCompilationException {
		Template.compile(validTemplateRaw);
	}
	
	@Test(expected = TemplateCompilationException.class)
	public void testInvalidMustacheDoesNotCompile() throws TemplateCompilationException {
		Template.compile("{{");
	}
	
	@Test
	public void testSubSuccessful() throws TemplateCompilationException {
		Template template = Template.compile(validTemplateRaw);
		Execution sub = template.sub(variables);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiled, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessful() throws TemplateCompilationException {
		Template template = Template.compile(validTemplateRaw);
		Execution sub = template.sub(empty);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] { key }, sub.getMissingVariables());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = encodedValue;
		}};
		Template template = Template.compile(validTemplateRaw);
		Execution sub = template.sub(variables, encoder, encoding);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiledEncoded, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = encodedValue; times = 0;
		}};
		Template template = Template.compile(validTemplateRaw);
		Execution sub = template.sub(empty, encoder, encoding);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] {key}, sub.getMissingVariables());
	}
	
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testSubEncodedInvalidEncoding() throws TemplateCompilationException, UnsupportedEncodingException {
		final String encoding = randomString();
		new Expectations() {{
			encoder.encode(value, encoding); result = new UnsupportedEncodingException();
		}};
		Template template = Template.compile(validTemplateRaw);
		template.sub(variables, encoder, encoding);
	}

	@Test
	public void testToString() throws TemplateCompilationException {
		Template template = Template.compile(validTemplateRaw);
		assertEquals(validTemplateRaw, template.toString());
	}

}
