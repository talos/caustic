package net.microscraper.client;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class UtilsTest {
	private static final int repetitions = 200;
	private static final int strLen = 100;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testJoinStringArrayString() {
		String[] stringsToJoin = new String[] {
				"the", "quick", "brown", "fox"
		};
		String joinString = " ";
		
		String joinedString = Utils.join(stringsToJoin, joinString);
		assertEquals("Error joining array of strings.", "the quick brown fox", joinedString);
	}

	@Test
	public void testJoinIntArrayString() {
		int[] intsToJoin = new int[] {
				1, 2, 3, 4
		};
		String joinString = "-";
		
		String joinedString = Utils.join(intsToJoin, joinString);
		assertEquals("Error joining array of ints.", "1-2-3-4", joinedString);
	}

	@Test
	public void testTruncate() {
		int originalLength = 1000;
		int truncatedLength = 500;
		
		assertEquals("Error truncating string.", truncatedLength, Utils.truncate(TestUtils.makeRandomString(originalLength), truncatedLength).length());
	}

	@Test
	public void testSplit() {
		for(int splitByLength = 1 ; splitByLength < strLen ; splitByLength ++) {
			String splitBy = "";
			for(int i = 0 ; i < splitByLength ; i ++) {
				splitBy += "-";
			}
			
			String stringToSplit = "";
			String[] componentStrings = new String[repetitions];
			for(int i = 0 ; i < repetitions ; i++) {
				componentStrings[i] = TestUtils.makeRandomString(TestUtils.getRandomInt(strLen));
				stringToSplit += componentStrings[i] + splitBy; // stuck with trailing splitter, should be extra blank element at end.
			}
			String[] splitString = Utils.split(stringToSplit, splitBy);
			
			assertEquals("String '" + stringToSplit + "' split by '" + splitBy + "' is wrong length", componentStrings.length + 1, splitString.length);
			assertEquals("Missing trailing empty string", "", splitString[splitString.length - 1]);
			for(int i = 0 ; i < componentStrings.length ; i ++) {
				assertEquals(componentStrings[i], splitString[i]);
			}
		}
	}

	@Test
	public void testQuoteString() {
		String quoted = Utils.quote(TestUtils.makeRandomString(strLen));
		assertEquals(true, quoted.startsWith(new String(new char[] { Utils.quotation })));
		assertEquals(true, quoted.endsWith(new String(new char[] { Utils.quotation })));
	}

	@Test
	public void testQuoteInt() {
		String quoted = Utils.quote(TestUtils.getRandomInt(strLen));
		assertEquals(true, quoted.startsWith(new String(new char[] { Utils.quotation })));
		assertEquals(true, quoted.endsWith(new String(new char[] { Utils.quotation })));
	}

	@Test
	public void testVectorIntoVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testArrayIntoVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testFormEncodedDataToNameValuePairs() throws Exception {
		String formEncoded = "number=6&word=bond&string=quick+brown+fox&string2=quick%20brown%20fox";
		NameValuePair[] nameValuePairs = Utils.formEncodedDataToNameValuePairs(formEncoded, "UTF-8");
		assertEquals("Wrong number of name value pairs.", 4, nameValuePairs.length);

		assertEquals("number", nameValuePairs[0].getName());
		assertEquals("6", nameValuePairs[0].getValue());
		
		assertEquals("word", nameValuePairs[1].getName());
		assertEquals("bond", nameValuePairs[1].getValue());

		assertEquals("string", nameValuePairs[2].getName());
		assertEquals("quick brown fox", nameValuePairs[2].getValue());

		assertEquals("string2", nameValuePairs[3].getName());
		assertEquals("quick brown fox", nameValuePairs[3].getValue());
	}

}
