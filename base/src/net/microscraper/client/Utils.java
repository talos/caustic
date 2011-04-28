/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package net.microscraper.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author john
 *
 */
public class Utils {
	/**
	 * Join an array of strings with a joinString.
	 * @param strings
	 * @param joinString
	 * @return
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

	/**
	 * Split a string into words based off of spaces without using regex or .split().
	 * @param input
	 * @param splitter
	 * @return
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
	 * Copy one vector into another.
	 * @param vector1
	 * @param vector2
	 */
	public static final void vectorIntoVector(Vector vector1, Vector vector2) {
		for(int i = 0; i < vector1.size(); i++) {
			vector2.addElement(vector1.elementAt(i));
		}
	}
	
	/**
	 * Copy an array into a vector.
	 * @param array
	 * @param vector
	 */
	public static final void arrayIntoVector(Object[] array, Vector vector) {
		for(int i = 0; i < array.length; i++) {
			vector.addElement(array[i]);
		}
	}

	/**
	 * Test to see if two arrays are equal.
	 * @param array1
	 * @param array2
	 * @return True if array1 has all members of array2 and vice versa, regardless of order.
	 */
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
	
	/**
	 * Generates a hashcode based off an array of objects.  Each individual object should implement
	 * hashCode().
	 * @param array
	 * @return
	 */
	public static int arrayHashCode(Object[] array) {
		int hashCode = 0;
		for (int i = 0; i < array.length ; i ++) {
			hashCode += array[i].hashCode();
		}
		return hashCode;
	}
	
	/**
	 * Copy one Hashtable into another. Preexisting keys in hashtable2 will be overwritten.
	 * @param hashtable1
	 * @param hashtable2
	 */
	public static final void hashtableIntoHashtable(Hashtable hashtable1, Hashtable hashtable2) {
		Enumeration keys = hashtable1.keys();
		while(keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = hashtable1.get(key);
			hashtable2.put(key, value);
		}
	}
	
	public static class HashtableWithNulls {
		private final Hashtable hashtable = new Hashtable();
		private boolean objectAtNullAssigned = false;
		private Object objectAtNull = null;
		public boolean containsKey(Object key) {
			if(key == null)
				return objectAtNullAssigned;
			else
				return hashtable.containsKey(key);
		}
		public void put(Object key, Object value) {
			if(key == null) {
				objectAtNull = value;
				objectAtNullAssigned = true;
			} else {
				hashtable.put(key, value);
			}
		}
		public Object get(Object key) {
			if(key == null)
				return objectAtNull;
			else
				return hashtable.get(key);
		}
	}
}
