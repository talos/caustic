package net.microscraper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Vector;

/**
 * A set of utilities, several of which are meant to substitute for 
 * methods not available in the Java ME environment.
 * @author john
 *
 */
public class Utils {
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
	 * @return An array of Strings resulting from the split.
	 */
	public static String[] split(String input, String splitter) {
		if(input.equals("")) {
			return new String[] { "" };
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
	 * The quotation character used by {@link Utils}.
	 * @see #quote(String)
	 * @see #quote(int)
	 */		
	public final static char QUOTATION = '"';
	
	/**
	 * Return the string with {@link #QUOTATION} around it.
	 * @param stringToQuote The {@link String} to quote.
	 * @return The string, quoted.
	 * @see #QUOTATION
	 */
	public static String quote(String stringToQuote) {
		return QUOTATION + stringToQuote + QUOTATION;
	}

	/**
	 * Return the int as a {@link String} with {@link #QUOTATION}
	 * around it.
	 * @param integerToQuote The int to quote.
	 * @return The integer, as a quoted string.
	 */
	public static String quote(int integerToQuote) {
		return quote(Integer.toString(integerToQuote));
	}
	
	/**
	 * Add one vector to the end of another.
	 * @param vector1 The vector to add.  Is not modified.
	 * @param vector2 The vector to add to.  <b>Is</b> modified.
	 */
	public static final void vectorIntoVector(Vector vector1, Vector vector2) {
		for(int i = 0; i < vector1.size(); i++) {
			vector2.addElement(vector1.elementAt(i));
		}
	}
	
	/**
	 * Copy an array into a vector.
	 * @param array The array to add.
	 * @param vector The vector to add the arry to.  Is modified.
	 */
	public static final void arrayIntoVector(Object[] array, Vector vector) {
		for(int i = 0; i < array.length; i++) {
			vector.addElement(array[i]);
		}
	}
	
	/**
	 * Preview an array of {@link NameValuePair}s as a {@link String}.
	 * @param nameValuePairs The {@link NameValuePair}s to preview.
	 * @return A {@link String} previewing <code>nameValuePairs</code>.
	 */
	public static String preview(NameValuePair[] nameValuePairs) {
		String[] joined = new String[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			joined[i] = nameValuePairs[i].getName() + ": " + nameValuePairs[i].getValue();
		}
		return join(joined, ", ");
	}
}
