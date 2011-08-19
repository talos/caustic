package net.microscraper.util;

import java.util.Vector;


/**
 * String utilities for Java ME.
 * @author john
 *
 */
public class StringUtils {
	/**
	 * Join an array of {@link String}s with another {@link String}.
	 * @param strings The array of {@link String}s.
	 * @param joinString The {@link String} to join <code>strings</code> with.
	 * @return <code>strings</code> as a single {@link String} joined by
	 * <code>joinString</code>.
	 */
	public static String join(String[] strings, String joinString) {
		String joined = "";
		for(int i = 0; i < strings.length; i++) {
			joined += strings[i];
			if(i < strings.length -1)
				joined += joinString;
		}
		return joined;
	}
	
	
	public static String join(int[] integers, String joinString) {
		String joined = "";
		for(int i = 0; i < integers.length; i++) {
			joined += Integer.toString(integers[i]);
			if(i < integers.length -1)
				joined += joinString;
		}
		return joined;
	}
	
	public static String quoteJoin(String[] strings) {
		return quote(join(strings, QUOTATION + ", " + QUOTATION));
	}
	
	public static String truncate(String string, int length) {
		if(string == null)
			return "";
		if(string.length() < length) {
			return string;
		} else {
			return string.substring(0, length);
		}
	}

	/**
	 * Split a string into words based off of spaces without using {@link java.util.regex.Pattern}
	 * or {@link String#split(String)}
	 * which are not available in Java ME.
	 * @param input The String to split.
	 * @param splitter The String to split with.
	 * @return An array of {@link String}s resulting from the split.
	 */
	public static String[] split(String input, String splitter) {
		if(input == null) {
			return new String[] { };
		} else if(input.equals("")) {
			return new String[] { };
		} else {
			int splitLoc;
			String wordsString = input.trim();
			Vector splitString = new Vector();
	    	do {
	    		splitLoc = wordsString.indexOf(splitter);
	    		String word;
	    		switch(splitLoc) {
	    			case 0:
	    				word = "";
	        			wordsString = wordsString.substring(splitter.length());
	        			break;
	    			case -1:
	    				word = wordsString;
	    				break;
	    			default:
	        			word = wordsString.substring(0, splitLoc);
	        			wordsString = wordsString.substring(splitLoc + splitter.length());
	        			break;
	    		}
	    		splitString.addElement(word);
	    	} while(splitLoc != -1);
	    	String[] output = new String[splitString.size()];
	    	splitString.copyInto(output);
	    	return output;
		}	
	}
	
	/**
	 * The quotation character used by {@link StringUtils}.
	 * @see #quote(String)
	 * @see #quote(int)
	 */		
	public final static char QUOTATION = '"';
	
	/**
	 * Return the {@link Object} with {@link #QUOTATION}s around it.
	 * @param objToQuote The {@link Object} to quote.  Uses the 
	 * {@link String#valueOf(Object)} method.
	 * @return The object as a quoted string.
	 * @see #QUOTATION
	 * @see String#valueOf(Object)
	 * @see #quote(int)
	 */
	public static String quote(Object objToQuote) {
		return QUOTATION + String.valueOf(objToQuote) + QUOTATION;
	}

	/**
	 * Return the int as a {@link String} with {@link #QUOTATION}
	 * around it.
	 * @param integerToQuote The int to quote.
	 * @return The integer, as a quoted string.
	 * @see #quote(Object)
	 */
	public static String quote(int integerToQuote) {
		return quote(Integer.toString(integerToQuote));
	}
}
