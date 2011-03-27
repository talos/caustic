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
	
	/**
	 * Copy all values from a Hashtable into an array.
	 * @param hashtable
	 * @param keys
	 * @param array 
	 * @return
	 */
	public static void hashtableValues(Hashtable hashtable, Object[] array) {
		Enumeration keys = hashtable.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			i++;
			Object key = keys.nextElement();
			array[i] = hashtable.get(key);
		}
	}

	/**
	 * Copy all keys from a Hashtable into an array.
	 * @param hashtable
	 * @param keys
	 * @param array 
	 * @return
	 */
	public static void hashtableKeys(Hashtable hashtable, Object[] array) {
		Enumeration keys = hashtable.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			i++;
			array[i] = keys.nextElement();
		}
	}
	
	/**
	 * Copy a selection of keys from a Hashtable into another array.
	 * @param hashtable
	 * @param keys
	 * @param array 
	 * @return
	 */
	public static void selectHashtableValuesIntoArray(Hashtable hashtable, Object[] keys, Object[] array) {
		for(int i = 0; i < keys.length; i++) {
			array[i] = hashtable.get(keys[i]);
		}
	}
	
}
