package net.caustic.regexp;

import static org.junit.Assert.*;
import static net.caustic.util.TestUtils.*;

import java.util.Arrays;
import java.util.Collection;

import net.caustic.regexp.JakartaRegexpCompiler;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.Pattern;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PatternTest {
	private final RegexpCompiler re;
	private Pattern pat;
	private String testClass;

	private static final String quickBrownFox = "The quick brown fox jumped over the lazy dog.";
	
	public PatternTest(final RegexpCompiler regexpCompiler) {
		this.re = regexpCompiler;
		this.testClass = regexpCompiler.getClass().getSimpleName();
	}
	
	@Parameters
	public static Collection<RegexpCompiler[]> implementations() throws Exception {
		return Arrays.asList(new RegexpCompiler[][] {
				{ new JakartaRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8))  },
				{ new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8)) }
		});
	}
	
	@Test
	public void testMatchesStringInt() {
		pat = re.newPattern("\\w{3}", false, false, false);
		
		for(int i = 0 ; i < 4 ; i ++) {
			assertTrue(testClass + " couldn't find match " + Integer.toString(i), pat.matches(quickBrownFox, i));
		}
	}

	@Test
	public void testMatch() throws Exception {
		pat = re.newPattern("((\\w+ ){3})", false, false, false);
		
		assertEquals(testClass + " didn't match the initial whole pattern.", "The quick brown ", pat.match(quickBrownFox, "$0", 0,0)[0]);
		assertEquals(testClass + " didn't match initial parent backreference.", "The quick brown ", pat.match(quickBrownFox, "$1", 0,0)[0]);

		assertEquals(testClass + " didn't match the second whole pattern.", "fox jumped over ", pat.match(quickBrownFox, "$0", 1,1)[0]);
		assertEquals(testClass + " didn't match second parent backreference.", "fox jumped over ", pat.match(quickBrownFox, "$1",1, 1)[0]);
	}
	
	@Test
	public void testMatchNestedBackreferences() throws Exception {
		pat = re.newPattern("((\\w+ ){3})", false, false, false);
		
		assertEquals(testClass + " didn't match initial nested backreference.", "brown ", pat.match(quickBrownFox, "$2",0, 0)[0]);
		assertEquals(testClass + " didn't match second nested backreference.", "over ", pat.match(quickBrownFox, "$2", 1,1)[0]);
	}

	@Test
	public void testAllMatches() throws Exception {
		String input = "And he's fine, fine, fine/I know he's fine, fine, fine/I know he's a fine";
		pat = re.newPattern("fine", false, false, false);
		String substitution = "$0 penguin";
		
		String[] allMatches = pat.match(input, substitution, 0, -1);
		
		for(int i = 0 ; i < allMatches.length ; i ++) {
			assertEquals(testClass + " didn't substitute match correctly.", "fine penguin", allMatches[i]);
		}
	}

	@Test
	public void testCaseInsensitive() {
		String input = "alpha beta gamma";
		Pattern betaCapsSensitive  = re.newPattern("BETA", false, false, false);
		Pattern betaLowerSensitive = re.newPattern("beta", false, false, false);
		Pattern betaCapsInsensitive  = re.newPattern("BETA", true, false, false);
		Pattern betaLowerInsensitive = re.newPattern("beta", true, false, false);
		assertFalse(testClass + " case sensitive uppercase should not match lowercase input.", betaCapsSensitive.matches(input, Pattern.FIRST_MATCH));
		assertTrue(testClass + " case sensitive lowercase should match lowercase input.", betaLowerSensitive.matches(input, Pattern.FIRST_MATCH));
		assertTrue(testClass + " case insensitive uppercase should match lowercase input.", betaCapsInsensitive.matches(input, Pattern.FIRST_MATCH));
		assertTrue(testClass + " case insensitive lowercase should match lowercase input.", betaLowerInsensitive.matches(input, Pattern.FIRST_MATCH));
	}
	
	@Test
	public void testMatchRange() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.newPattern("b\\w+s", false, false, false);
		String sub = "$0";
		
		String[] allMatches = pat.match(input, sub, 0, -1);
		assertEquals(3, allMatches.length);
		assertEquals("boleros", allMatches[allMatches.length - 1]);
		
		String[] middleMatch = pat.match(input, sub, 1, -2);
		assertEquals(1, middleMatch.length);
		assertEquals("bicycles", middleMatch[0]);
		
		String[] firstTwo = pat.match(input, sub, 0, 1);
		assertEquals(2, firstTwo.length);
		assertEquals("briskets", firstTwo[0]);
		assertEquals("bicycles", firstTwo[1]);
		
		String[] lastTwo = pat.match(input, sub, -2, -1);
		assertEquals(2, lastTwo.length);
		assertEquals("bicycles", lastTwo[0]);
		assertEquals("boleros", lastTwo[1]);
	}
	
	@Test
	public void testMatchNumber() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.newPattern("b\\w+s", false, false, false);
		String sub = "$0";
		
		String firstMatch = pat.match(input, sub, 0,0)[0];
		String secondMatch = pat.match(input, sub, 1,1)[0];
		String thirdMatch = pat.match(input, sub, 2,2)[0];
		
		String lastMatch  = pat.match(input, sub, -1, -1)[0];
		String secondToLastMatch = pat.match(input, sub, -2,-2)[0];
		String thirdToLastMatch = pat.match(input, sub, -3,-3)[0];
		
		assertEquals(firstMatch, "briskets");
		assertEquals(secondMatch, "bicycles");
		assertEquals(thirdMatch, "boleros");
		
		assertEquals(lastMatch, "boleros");
		assertEquals(secondToLastMatch, "bicycles");
		assertEquals(thirdToLastMatch, "briskets");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidMatchRangeIllegalArgument() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.newPattern("b\\w+s", false, false, false);
		String sub = "$0";

		pat.match(input, sub, -1, -2);
	}
	
	@Test
	public void testNonMatch() throws Exception {
		String input = "briskets, bicycles, and boleros";
		pat = re.newPattern("b\\w+s", false, false, false);
		String sub = "$0";
		
		assertArrayEquals(new String[] { }, pat.match(input, sub, 4, 4));
	}
	
	@Test()
	public void testExclusionCharacter() throws Exception {
		String input = "<table attr='value'><tr><td>cell</td></tr></table>";
		pat = re.newPattern("<table[^>]*>(.*)</table>", false, false, false);
		String sub = "$1";
		
		String match = pat.match(input, sub, 0, 0)[0];
		
		assertEquals("<tr><td>cell</td></tr>", match);
	}
	

	@Test()
	public void testNonGreedy() throws Exception {
		String input = "<table attr='value'><tr><td>cell</td></tr></table>";
		pat = re.newPattern("td>([^<]*?)<", false, false, false);
		String sub = "$1";
		
		String match = pat.match(input, sub, 0, 0)[0];
		
		assertEquals("cell", match);
	}
	

	@Test()
	public void testMatchCompilingPatternsSpeed() throws Exception {
		int numTests = 100;
		int inputSize = 500000;
		String testPattern = "[a-c]{3}(\\d+)";
		
		for(int i = 0 ; i < numTests ; i ++) {
			String input = randomString(inputSize);
			pat = re.newPattern(testPattern, false, false, false);
			String sub = "$1";
			pat.match(input, sub, 0, 0);
		}
	}
	

	@Test()
	public void testMatchPrecompiledPatternsSpeed() throws Exception {
		int numTests = 100;
		int inputSize = 500000;
		String testPattern = "[a-c]{3}(\\d+)";
		pat = re.newPattern(testPattern, false, false, false);
		
		for(int i = 0 ; i < numTests ; i ++) {
			String input = randomString(inputSize);
			String sub = "$1";
			pat.match(input, sub, 0, 0);
		}
	}
}
