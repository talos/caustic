package net.microscraper.regexp;

import static org.junit.Assert.*;
import static net.microscraper.util.TestUtils.*;
import static net.microscraper.regexp.StringTemplate.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import net.microscraper.database.DatabaseView;
import net.microscraper.database.InMemoryDatabaseView;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.template.StringSubstitution;
import net.microscraper.util.Encoder;
import net.microscraper.util.JavaNetEncoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringTemplateTest {
	private static Encoder encoder;
	private final RegexpCompiler re;
	private DatabaseView view;
	
	public StringTemplateTest(RegexpCompiler regexpCompiler) {
		this.re = regexpCompiler;
		this.view = new InMemoryDatabaseView();
	}
	
	@Parameters
	public static Collection<RegexpCompiler[]> implementations() throws Exception {
		encoder = new JavaNetEncoder(Encoder.UTF_8);
		return Arrays.asList(new RegexpCompiler[][] {
				{ new JakartaRegexpCompiler(encoder)  },
				{ new JavaUtilRegexpCompiler(encoder) }
		});
	}
	
	@Test
	public void testSubSuccessfulNotEncoded() throws Exception {
		view.put("not encoded", "one & more reasons it <b>should be</b>");
		
		StringTemplate template = re.newTemplate("substituted but {{{not encoded}}}",
				DEFAULT_ENCODED_PATTERN, DEFAULT_NOT_ENCODED_PATTERN);
		StringSubstitution sub = template.sub(view);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted but one & more reasons it <b>should be</b>", sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulNotEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{{is missing}}}",
				DEFAULT_ENCODED_PATTERN, DEFAULT_NOT_ENCODED_PATTERN);
		StringSubstitution sub = template.sub(view);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { "is missing" }, sub.getMissingTags());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws Exception {
		String strToEncode =  "& it very well <i>ought to be</i>";
		view.put("encoded", strToEncode);
		StringTemplate template = re.newTemplate("substituted {{encoded}}",
				DEFAULT_ENCODED_PATTERN, DEFAULT_NOT_ENCODED_PATTERN);
		StringSubstitution sub = template.sub(view);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted " + encoder.encode(strToEncode), sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{is missing}}", DEFAULT_ENCODED_PATTERN, DEFAULT_NOT_ENCODED_PATTERN);
		StringSubstitution sub = template.sub(view);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { "is missing" }, sub.getMissingTags());
	}

	@Test
	public void testToString() {
		String templateString = "un deux trois & quatre";
		StringTemplate template = re.newTemplate(templateString,
				DEFAULT_ENCODED_PATTERN, DEFAULT_NOT_ENCODED_PATTERN);
		assertEquals(templateString, template.toString());
	}

}
