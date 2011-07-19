package net.microscraper.client.interfaces;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import net.microscraper.client.impl.JakartaRegexpCompiler;
import net.microscraper.client.impl.JavaUtilRegexpCompiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PatternInterfaceTest {
	private RegexpCompiler re;
	private PatternInterface pat;

	private static final String quickBrownFox = "The quick brown fox jumped over the lazy dog.";
	
	/*@Before
	public void setUp() throws Exception {
	}*/
	
	public PatternInterfaceTest(final RegexpCompiler regexpCompiler) {
		this.re = regexpCompiler;
	}
	
	@Parameters
	public static Collection<RegexpCompiler[]> implementations() {
		return Arrays.asList(new RegexpCompiler[][] {
				{ new JakartaRegexpCompiler()  },
				{ new JavaUtilRegexpCompiler() }
		});
	}
	
	@Test
	public void testCaseInsensitive() {
		String input = "alpha beta gamma";
		PatternInterface betaCapsSensitive  = re.compile("BETA", false, false, false);
		PatternInterface betaLowerSensitive = re.compile("beta", false, false, false);
		PatternInterface betaCapsInsensitive  = re.compile("BETA", true, false, false);
		PatternInterface betaLowerInsensitive = re.compile("beta", true, false, false);
		assertFalse("Case sensitive uppercase should not match lowercase input.", betaCapsSensitive.matches(input));
		assertTrue("Case sensitive lowercase should match lowercase input.", betaLowerSensitive.matches(input));
		assertTrue("Case insensitive uppercase should match lowercase input.", betaCapsInsensitive.matches(input));
		assertTrue("Case insensitive lowercase should match lowercase input.", betaLowerInsensitive.matches(input));
	}
	
	@Test
	public void testMatchesString() {
		pat = re.compile("the", false, false, false);
		
		assertTrue("Match failed.", pat.matches(quickBrownFox));
	}

	@Test
	public void testMatchesStringInt() {
		pat = re.compile("\\w{3}", false, false, false);
		
		for(int i = 0 ; i < 4 ; i ++) {
			assertTrue("Couldn't find match" + Integer.toString(i), pat.matches(quickBrownFox, i));
		}
	}

	@Test
	public void testMatch() throws Exception {
		pat = re.compile("((\\w+ ){3})", false, false, false);
		
		assertEquals("Didn't match the whole pattern.", "The quick brown ", pat.match(quickBrownFox, "$1", 0));
		assertEquals("Didn't match first backreference.", "The quick brown ", pat.match(quickBrownFox, "$1", 0));
		assertEquals("Didn't match second backreference.", "The ", pat.match(quickBrownFox, "$2", 0));
	}

	@Test
	public void testAllMatches() {
		fail("Not yet implemented");
	}

}
