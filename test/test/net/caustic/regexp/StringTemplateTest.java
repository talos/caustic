package net.caustic.regexp;

import static org.junit.Assert.*;
import static net.caustic.regexp.StringTemplate.*;

import java.util.Arrays;
import java.util.Collection;

import net.caustic.database.Database;
import net.caustic.database.DatabaseException;
import net.caustic.database.MemoryDatabase;
import net.caustic.regexp.JakartaRegexpCompiler;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.regexp.StringTemplate;
import net.caustic.scope.Scope;
import net.caustic.template.StringSubstitution;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringTemplateTest {
	private static Encoder encoder;
	private final RegexpCompiler re;
	private Database db;
	private Scope scope;
	
	public StringTemplateTest(RegexpCompiler regexpCompiler) throws DatabaseException {
		this.re = regexpCompiler;
		this.db = new MemoryDatabase();
		this.scope = db.newDefaultScope();
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
		db.put(scope, "not encoded", "one & more reasons it <b>should be</b>");
		
		StringTemplate template = re.newTemplate("substituted but {{{not encoded}}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(db, scope);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted but one & more reasons it <b>should be</b>", sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulNotEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{{is missing}}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(db, scope);
		assertTrue(sub.isMissingTags());
		assertArrayEquals(new String[] { "is missing" }, sub.getMissingTags());
	}
	
	@Test
	public void testSubSuccessfulEncoded() throws Exception {
		String strToEncode =  "& it very well <i>ought to be</i>";
		db.put(scope, "encoded", strToEncode);
		StringTemplate template = re.newTemplate("substituted {{encoded}}",
				ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(db, scope);
		assertFalse(sub.isMissingTags());
		assertEquals("substituted " + encoder.encode(strToEncode), sub.getSubstituted());
	}
	
	@Test
	public void testSubUnsuccessfulEncoded() throws Exception {
		StringTemplate template = re.newTemplate("this {{is missing}}", ENCODED_PATTERN, UNENCODED_PATTERN);
		StringSubstitution sub = template.sub(db, scope);
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
