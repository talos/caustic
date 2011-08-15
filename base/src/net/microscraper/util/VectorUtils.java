package net.microscraper.util;

import java.util.Vector;

/**
 * {@link Vector} utilities for Java ME.
 * @author talos
 *
 */
public class VectorUtils {

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

	public VectorUtils() {
		super();
	}

}