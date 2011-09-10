package net.microscraper.util;

import static org.junit.Assert.*;

import static net.microscraper.util.TestUtils.*;
import net.microscraper.util.StringUtils;

import org.junit.Test;

public class StringUtilsTest {
	private static final int repetitions = 200;
	private static final int strLen = 100;
	
	@Test
	public void testJoinStringArrayString() {
		String[] stringsToJoin = new String[] {
				"the", "quick", "brown", "fox"
		};
		String joinString = " ";
		
		String joinedString = StringUtils.join(stringsToJoin, joinString);
		assertEquals("Error joining array of strings.", "the quick brown fox", joinedString);
	}
/*
	@Test
	public void testJoinIntArrayString() {
		int[] intsToJoin = new int[] {
				1, 2, 3, 4
		};
		String joinString = "-";
		
		String joinedString = StringUtils.join(intsToJoin, joinString);
		assertEquals("Error joining array of ints.", "1-2-3-4", joinedString);
	}
*/
	@Test
	public void testTruncate() {
		int originalLength = 1000;
		int truncatedLength = 500;
		
		assertEquals("Unexpected length.", truncatedLength,
				StringUtils.quoteAndTruncate(randomString(originalLength), truncatedLength).length());
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
				componentStrings[i] = randomString(randomInt(strLen));
				stringToSplit += componentStrings[i] + splitBy; // stuck with trailing splitter, should be extra blank element at end.
			}
			String[] splitString = StringUtils.split(stringToSplit, splitBy);
			
			assertEquals("String '" + stringToSplit + "' split by '" + splitBy + "' is wrong length", componentStrings.length + 1, splitString.length);
			assertEquals("Missing trailing empty string", "", splitString[splitString.length - 1]);
			for(int i = 0 ; i < componentStrings.length ; i ++) {
				assertEquals(componentStrings[i], splitString[i]);
			}
		}
	}

	@Test
	public void testQuoteString() {
		String quoted = StringUtils.quote(randomString(strLen));
		assertEquals(true, quoted.startsWith(StringUtils.QUOTATION));
		assertEquals(true, quoted.endsWith(StringUtils.QUOTATION));
	}

	@Test
	public void testQuoteInt() {
		String quoted = StringUtils.quote(randomInt(strLen));
		assertEquals(true, quoted.startsWith(StringUtils.QUOTATION));
		assertEquals(true, quoted.endsWith(StringUtils.QUOTATION));
	}

	@Test
	public void testSimpleClassName() {
		Object obj = new Object();
		assertEquals("Object", StringUtils.simpleClassName(obj));
	}

}
