package net.microscraper.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * A set of utilities, several of which are meant to substitute for 
 * methods not available in the Java ME environment.
 * @author john
 *
 */
public class Utils {
	/**
	 * Join an array of strings with a joinString.
	 * @param strings
	 * @param joinString
	 * @return The strings, joined by joinString.
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
	
	/*public static String join(Object[] objects, String joinString) {
		String joined = "";
		for(int i = 0; i < objects.length; i++) {
			joined += objects[i].toString();
			if(i < objects.length -1)
				joined += joinString;
		}
		return joined;
	}*/
	
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
			return string.substring(0, length - 1);
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
		int splitLoc = 0;
		String wordsString = input.trim();
		Vector splitString = new Vector();
		if(input.equals("")) {
			return new String[] { };
		} else {
	    	do {
	    		splitLoc = wordsString.indexOf(splitter);
	    		String word;
	    		switch(splitLoc) {
	    			case 0:
	        			wordsString = wordsString.substring(splitter.length());
	        			continue;
	    			case -1:
	    				word = wordsString;
	    				break;
	    			default:
	        			word = wordsString.substring(0, splitLoc);
	        			wordsString = wordsString.substring(splitLoc);
	    		}
	    		splitString.addElement(word);
	    	} while(splitLoc != -1);
		}
		
    	String[] output = new String[splitString.size()];
    	splitString.copyInto(output);
    	return output;
	}
	
	/**
	 * Return the string with quotations around it. (ex.: a string => "a string")
	 * @param stringToQuote The String to quote.
	 * @return The string, quoted.
	 */
	public static String quote(String stringToQuote) {
		return "\"" + stringToQuote + "\"";
	}

	/**
	 * Return the int as a String with quotations around it. (ex.: 8 => "8")
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
	 * Test to see if two arrays are equal.
	 * @param array1 The first array.
	 * @param array2 The second array.
	 * @return True if array1 has all members of array2 and vice versa, regardless of order.
	 */
	/*
	public static boolean arraysEqual(Object[] array1, Object[] array2) {
		if(array1.length != array2.length)
			return false;
		Vector vector = new Vector();
		Utils.arrayIntoVector(array1, vector);
		for(int i = 0; i < array2.length; i ++) {
			if(vector.contains(array2[i]))
				return false;
		}
		return true;
	}
	*/
	/**
	 * Generates a hashcode based off an array of objects.  Each individual object should implement
	 * hashCode().
	 * @param array
	 * @return
	 */
	/*
	public static int arrayHashCode(Object[] array) {
		int hashCode = 0;
		for (int i = 0; i < array.length ; i ++) {
			hashCode += array[i].hashCode();
		}
		return hashCode;
	}*/
	
	/**
	 * Copy one Hashtable into another. Preexisting keys in hashtable2 will be overwritten.
	 * @param hashtable1
	 * @param hashtable2
	 *//*
	public static final void hashtableIntoHashtable(Hashtable hashtable1, Hashtable hashtable2) {
		Enumeration keys = hashtable1.keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = hashtable1.get(key);
			hashtable2.put(key, value);
		}
	}
	*/
	/**
	 * Turn Form-encoded data into an array of {@link NameValuePair}s.
	 * @param formEncodedData A String of form data to convert.
	 * @param encoding The encoding to use.  "UTF-8" recommended.
	 * @return An array of {@link NameValuePair}s
	 * @throws UnsupportedEncodingException If the encoding is not supported.
	 */
	public static NameValuePair[] formEncodedDataToNameValuePairs(String formEncodedData,
				String encoding) throws UnsupportedEncodingException {
		String[] split = Utils.split(formEncodedData, "&");
		NameValuePair[] pairs = new NameValuePair[split.length];
		for(int i = 0 ; i < split.length; i++) {
			String[] pair = Utils.split(formEncodedData, "=");
			pairs[i] = new UnencodedNameValuePair(
					URLDecoder.decode(pair[0], encoding),
					URLDecoder.decode(pair[1], encoding));
		}
		return pairs;
	}
}
