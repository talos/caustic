package net.microscraper.interfaces;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.interfaces.regexp.InvalidRangeException;
import net.microscraper.interfaces.regexp.NoMatchesException;
import net.microscraper.interfaces.regexp.PatternInterface;
import net.microscraper.interfaces.regexp.RegexpCompiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PatternInterfaceTest {
	private RegexpCompiler re;
	private PatternInterface pat;
	private String testClass;

	private static final String quickBrownFox = "The quick brown fox jumped over the lazy dog.";
	
	/*@Before
	public void setUp() throws Exception {
	}*/
	
	public PatternInterfaceTest(final RegexpCompiler regexpCompiler) {
		this.re = regexpCompiler;
		this.testClass = regexpCompiler.getClass().getSimpleName();
	}
	
	@Parameters
	public static Collection<RegexpCompiler[]> implementations() {
		return Arrays.asList(new RegexpCompiler[][] {
				{ new JakartaRegexpCompiler()  },
				{ new JavaUtilRegexpCompiler() }
		});
	}
	
	
	@Test
	public void testMatchesString() {
		pat = re.compile("the", false, false, false);
		
		assertTrue(testClass + " match failed.", pat.matches(quickBrownFox));
	}

	@Test
	public void testMatchesStringInt() {
		pat = re.compile("\\w{3}", false, false, false);
		
		for(int i = 0 ; i < 4 ; i ++) {
			assertTrue(testClass + " couldn't find match " + Integer.toString(i), pat.matches(quickBrownFox, i));
		}
	}

	@Test
	public void testMatch() throws Exception {
		pat = re.compile("((\\w+ ){3})", false, false, false);
		
		assertEquals(testClass + " didn't match the initial whole pattern.", "The quick brown ", pat.match(quickBrownFox, "$0", 0));
		assertEquals(testClass + " didn't match initial parent backreference.", "The quick brown ", pat.match(quickBrownFox, "$1", 0));

		assertEquals(testClass + " didn't match the second whole pattern.", "fox jumped over ", pat.match(quickBrownFox, "$0", 1));
		assertEquals(testClass + " didn't match second parent backreference.", "fox jumped over ", pat.match(quickBrownFox, "$1", 1));
	}
	
	@Test
	public void testMatchNestedBackreferences() throws Exception {
		pat = re.compile("((\\w+ ){3})", false, false, false);
		
		assertEquals(testClass + " didn't match initial nested backreference.", "brown ", pat.match(quickBrownFox, "$2", 0));
		assertEquals(testClass + " didn't match second nested backreference.", "over ", pat.match(quickBrownFox, "$2", 1));
	}

	@Test
	public void testAllMatches() throws Exception {
		String input = "And he's fine, fine, fine/I know he's fine, fine, fine/I know he's a fine";
		pat = re.compile("fine", false, false, false);
		String substitution = "$0 penguin";
		
		String[] allMatches = pat.allMatches(input, substitution, 0, -1);
		
		for(int i = 0 ; i < allMatches.length ; i ++) {
			assertEquals(testClass + " didn't substitute match correctly.", "fine penguin", allMatches[i]);
		}
	}

	@Test
	public void testCaseInsensitive() {
		String input = "alpha beta gamma";
		PatternInterface betaCapsSensitive  = re.compile("BETA", false, false, false);
		PatternInterface betaLowerSensitive = re.compile("beta", false, false, false);
		PatternInterface betaCapsInsensitive  = re.compile("BETA", true, false, false);
		PatternInterface betaLowerInsensitive = re.compile("beta", true, false, false);
		assertFalse(testClass + " case sensitive uppercase should not match lowercase input.", betaCapsSensitive.matches(input));
		assertTrue(testClass + " case sensitive lowercase should match lowercase input.", betaLowerSensitive.matches(input));
		assertTrue(testClass + " case insensitive uppercase should match lowercase input.", betaCapsInsensitive.matches(input));
		assertTrue(testClass + " case insensitive lowercase should match lowercase input.", betaLowerInsensitive.matches(input));
	}
	
	@Test
	public void testMatchRange() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.compile("b\\w+s", false, false, false);
		String sub = "$0";
		
		String[] allMatches = pat.allMatches(input, sub, 0, -1);
		assertEquals(3, allMatches.length);
		assertEquals("boleros", allMatches[allMatches.length - 1]);
		
		String[] middleMatch = pat.allMatches(input, sub, 1, -2);
		assertEquals(1, middleMatch.length);
		assertEquals("bicycles", middleMatch[0]);
		
		String[] firstTwo = pat.allMatches(input, sub, 0, 1);
		assertEquals(2, firstTwo.length);
		assertEquals("briskets", firstTwo[0]);
		assertEquals("bicycles", firstTwo[1]);
		
		String[] lastTwo = pat.allMatches(input, sub, -2, -1);
		assertEquals(2, lastTwo.length);
		assertEquals("bicycles", lastTwo[0]);
		assertEquals("boleros", lastTwo[1]);
	}
	
	@Test
	public void testMatchNumber() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.compile("b\\w+s", false, false, false);
		String sub = "$0";
		
		String firstMatch = pat.match(input, sub, 0);
		String secondMatch = pat.match(input, sub, 1);
		String thirdMatch = pat.match(input, sub, 2);
		
		String lastMatch  = pat.match(input, sub, -1);
		String secondToLastMatch = pat.match(input, sub, -2);
		String thirdToLastMatch = pat.match(input, sub, -3);
		
		assertEquals(firstMatch, "briskets");
		assertEquals(secondMatch, "bicycles");
		assertEquals(thirdMatch, "boleros");
		
		assertEquals(lastMatch, "boleros");
		assertEquals(secondToLastMatch, "bicycles");
		assertEquals(thirdToLastMatch, "briskets");
	}
	
	@Test(expected=InvalidRangeException.class)
	public void testInvalidMatchRange() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.compile("b\\w+s", false, false, false);
		String sub = "$0";

		pat.allMatches(input, sub, -1, -2);
	}
	
	@Test(expected=NoMatchesException.class)
	public void testNonMatch() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.compile("b\\w+s", false, false, false);
		String sub = "$0";
		
		pat.match(input, sub, 4);
	}
}
