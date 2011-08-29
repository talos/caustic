package net.microscraper.template;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Database;
import net.microscraper.template.Template;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;
import static net.microscraper.util.TestUtils.*;

import org.junit.Before;
import org.junit.Test;

public class TemplateTest {
	
	//@Mocked Variables variables, empty;
	@Mocked Database database, empty;
	@Mocked Encoder encoder;
	private int id = 0;
	
	private static final String key = "template";
	private static final String value = "has been substituted";
	private static final String encodedValue = "has+been+substituted";
	private static final String validTemplateRaw =
			"A valid " + Template.DEFAULT_OPEN_TAG + key + Template.DEFAULT_CLOSE_TAG + ".";
	private static final String validTemplateCompiled = "A valid " + value + ".";
	private static final String validTemplateCompiledEncoded = "A valid " + encodedValue + ".";
	
	@Before
	public void setup() {
		new NonStrictExpectations() {{
			database.get(id, key); result = value;
		}};
	}
	
	@Test
	public void testMustacheTemplateCompiles() throws TemplateCompilationException {
		new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
	}
	
	@Test(expected = TemplateCompilationException.class)
	public void testInvalidMustacheDoesNotCompile() throws TemplateCompilationException {
		new Template(Template.DEFAULT_OPEN_TAG, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
	}
	
	@Test
	public void testSubSuccessful() throws TemplateCompilationException {
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		Execution sub = template.sub(id);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiled, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessful() throws TemplateCompilationException {
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, empty);
		Execution sub = template.sub(id);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] { key }, sub.getMissingVariables());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue;
		}};
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		Execution sub = template.subEncoded(id, encoder);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiledEncoded, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue; times = 0;
		}};
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, empty);
		Execution sub = template.subEncoded(id, encoder);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] {key}, sub.getMissingVariables());
	}
	
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testSubEncodedInvalidEncoding() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = new UnsupportedEncodingException();
		}};
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		template.subEncoded(id, encoder);
	}

	@Test
	public void testToString() throws TemplateCompilationException {
		Template template = new Template(validTemplateRaw, Template.DEFAULT_OPEN_TAG, Template.DEFAULT_CLOSE_TAG, database);
		assertEquals(validTemplateRaw, template.toString());
	}

}
