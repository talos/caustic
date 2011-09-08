package net.microscraper.util;

import java.util.Enumeration;
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

	/**
	 * Compare two {@link Vector}s to see if they have the same elements.  Uses
	 * {@link Vector#contains(Object)} for each element of one in the other, and
	 * checks to see that their length is the same.
	 * @param vector1 A {@link Vector}.
	 * @param vector2 A {@link Vector} to compare.
	 * @return <code>true</code> if <code>vector2</code> has all the elements in 
	 * <code>vector1</code> and is the same length. <code>false</code> otherwise.
	 */
	public static final boolean haveSameElements(Vector vector1, Vector vector2) {
		if(vector1.size() == vector2.size()) {
			Enumeration e = vector1.elements();
			while(e.hasMoreElements()) {
				Object element = e.nextElement();
				if(!vector2.contains(element)) {
					return false; // early exit from enumeration.
				}
			}
			return true; // if the enumeration is complete without an early exit
		}
		return false;
	}

}