package net.caustic.regexp;

import static org.junit.Assert.*;
import static net.caustic.regexp.StringTemplate.*;

import java.util.Arrays;
import java.util.Collection;

import mockit.Expectations;
import mockit.NonStrict;
import net.caustic.regexp.JakartaRegexpCompiler;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.template.StringSubstitution;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StringMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringTemplateTest {
	private @NonStrict StringMap context;
	private static Encoder encoder;
	private final RegexpCompiler re;
	
	public StringTemplateTest(RegexpCompiler regexpCompiler) {
		this.re = regexpCompiler;
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
		new Expectations() {{
			context.get("not encoded"); result = "one & more reasons it <b>should be</b>";
		}};
		
		StringTemplate template = re.newTemplate("substituted but {{{not encoded}}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(context);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted but one & more reasons it <b>should be</b>", sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulNotEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{{is missing}}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(context);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { "is missing" }, sub.getMissingTags());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws Exception {
		final String strToEncode =  "& it very well <i>ought to be</i>";
		new Expectations() {{
			context.get("encoded"); result = strToEncode;
		}};
		
		StringTemplate template = re.newTemplate("substituted {{encoded}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(context);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted " + encoder.encode(strToEncode), sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{is missing}}", ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(context);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { "is missing" }, sub.getMissingTags());
	}

	@Test
	public void testToString() {
		String templateString = "un deux trois & quatre";
		StringTemplate template = re.newTemplate(templateString,
				ENCODED_PATTERN, UNENCODED_PATTERN);
		assertEquals(templateString, template.toString());
	}

}
