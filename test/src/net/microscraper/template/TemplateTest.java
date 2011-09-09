package net.microscraper.template;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;

import java.io.UnsupportedEncodingException;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.DatabaseView;
import net.microscraper.template.StringTemplate;
import net.microscraper.template.TemplateCompilationException;
import net.microscraper.util.Encoder;

import org.junit.Before;
import org.junit.Test;

public class TemplateTest {
	
	private @Mocked DatabaseView input;
	private @Mocked Encoder encoder;
	
	private static final String key = randomString();
	private static final String missingTag = randomString();
	private static final String value = "has been substituted";
	private static final String encodedValue = "has+been+substituted";
	private static final String validTemplateMissingTag =
			"A valid " + StringTemplate.DEFAULT_OPEN_TAG + missingTag + StringTemplate.DEFAULT_CLOSE_TAG + ".";
	private static final String validTemplateRaw =
			"A valid " + StringTemplate.DEFAULT_OPEN_TAG + key + StringTemplate.DEFAULT_CLOSE_TAG + ".";
	private static final String validTemplateCompiled = "A valid " + value + ".";
	private static final String validTemplateCompiledEncoded = "A valid " + encodedValue + ".";
	
	@Before
	public void setup() throws Exception {
		new NonStrictExpectations() {{
			input.get(key); result = value;
		}};
	}
	
	@Test
	public void testMustacheTemplateCompiles() throws TemplateCompilationException {
		new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
	}
	
	@Test(expected = TemplateCompilationException.class)
	public void testInvalidMustacheDoesNotCompile() throws TemplateCompilationException {
		new StringTemplate(StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
	}
	
	@Test
	public void testSubSuccessful() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		StringSubstitution sub = template.sub(input);
		assertFalse(sub.isMissingTags());
		assertEquals(validTemplateCompiled, sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessful() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateMissingTag, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		StringSubstitution sub = template.sub(input);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { missingTag }, sub.getMissingTags());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue;
		}};
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		StringSubstitution sub = template.subEncoded(input, encoder);
		assertFalse(sub.isMissingTags());
		assertEquals(validTemplateCompiledEncoded, sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = encodedValue; times = 0;
		}};
		StringTemplate template = new StringTemplate(validTemplateMissingTag, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		StringSubstitution sub = template.subEncoded(input, encoder);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] {missingTag}, sub.getMissingTags());
	}
	
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testSubEncodedInvalidEncoding() throws TemplateCompilationException, UnsupportedEncodingException {
		new Expectations() {{
			encoder.encode(value); result = new UnsupportedEncodingException();
		}};
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		template.subEncoded(input, encoder);
	}

	@Test
	public void testToString() throws TemplateCompilationException {
		StringTemplate template = new StringTemplate(validTemplateRaw, StringTemplate.DEFAULT_OPEN_TAG, StringTemplate.DEFAULT_CLOSE_TAG);
		assertEquals(validTemplateRaw, template.toString());
	}

}
