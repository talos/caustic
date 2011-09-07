package net.microscraper.template;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.template.StringTemplate;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.util.Encoder;
import net.microscraper.util.Execution;

import org.junit.Before;
import org.junit.Test;

public class TemplateTest {
	
	@Mocked Database database, empty;
	@Mocked Encoder encoder;
	private Scope scope;
	
	private static final String key = "template";
	private static final String value = "has been substituted";
	private static final String encodedValue = "has+been+substituted";
	private static final String validTemplateRaw =
			"A valid " + StringTemplate.DEFAULT_OPEN_TAG + key + StringTemplate.DEFAULT_CLOSE_TAG + ".";
	private static final String validTemplateCompiled = "A valid " + value + ".";
	private static final String validTemplateCompiledEncoded = "A valid " + encodedValue + ".";
	
	@Before
	public void setup() throws Exception {
		new NonStrictExpectations() {{
			database.getDefaultScope(); result = scope;
			empty.getDefaultScope(); result = scope;
			database.get(scope, key); result = value;
		}};
	}
	
	@Test
	public void testMustacheTemplateCompiles() throws TemplateCompilationException {
		new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
	}
	
	@Test(expected = TemplateCompilationException.class)
	public void testInvalidMustacheDoesNotCompile() throws TemplateCompilationException {
		new StringTemplate(StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
	}
	
	@Test
	public void testSubSuccessful() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		Execution sub = template.sub(scope);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiled, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessful() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, empty);
		Execution sub = template.sub(scope);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] { key }, sub.getMissingVariables());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue;
		}};
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		Execution sub = template.subEncoded(scope, encoder);
		assertTrue(sub.isSuccessful());
		assertEquals(validTemplateCompiledEncoded, sub.getExecuted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue; times = 0;
		}};
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, empty);
		Execution sub = template.subEncoded(scope, encoder);
		assertFalse(sub.isSuccessful());
		assertArrayEquals(new String[] {key}, sub.getMissingVariables());
	}
	
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testSubEncodedInvalidEncoding() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = new UnsupportedEncodingException();
		}};
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		template.subEncoded(scope, encoder);
	}

	@Test
	public void testToString() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG, database);
		assertEquals(validTemplateRaw, template.toString());
	}

}
