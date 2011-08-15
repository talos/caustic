package net.microscraper.test;

import java.util.Random;
import java.util.Vector;

import net.microscraper.instruction.Executable;
import net.microscraper.util.Utils;

/**
 * Static methods for testing.
 * @author talos
 *
 */
public class TestUtils {
	
	
	private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static Random rnd = new Random();
	
	/**
	 * The default maximum {@link String} length for {@link #randomString()} (exclusive).
	 */
	public static int RANDOM_STRING_LENGTH = 20;
	
	/**
	 * The default maximum {@link int} size for {@link #randomInt()} (exclusive).
	 */
	public static int MAX_RANDOM_INT = 500;
	

	/**
	 * 
	 * @param max The maximum size for this random int (exclusive).
	 * @return A random {@link int} between 0 (inclusive) and <code>max</code> (exclusive).
	 * @see #MAX_RANDOM_INT
	 * @see #randomInt()
	 */
	public static int randomInt(int max) {
		return rnd.nextInt(max);
	}
	
	/**
	 * 
	 * @return A random {@link int} between 0 (inclusive) and <code>{@link #MAX_RANDOM_INT}</code> (exclusive).
	 * @see #MAX_RANDOM_INT
	 * @see #randomInt(int)
	 */
	public static int randomInt() {
		return rnd.nextInt(MAX_RANDOM_INT);
	}
	
	/**
	 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
	 * @param length The length of the random string.
	 * @return A {@link String} of random alphanumeric characters of the specified length.
	 * @see #randomString()
	 */
	public static String randomString(int length) {
	   StringBuilder sb = new StringBuilder(length);
	   for( int i = 0; i < length; i++ ) {
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   }
	   return sb.toString();
	}
	
	/**
	 * Thank you to
	 * <a href="http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java">
	 * Stack Overflow</a>
	 * @return A {@link String} of random alphanumeric characters of length {@link #RANDOM_STRING_LENGTH}.
	 * @see #RANDOM_STRING_LENGTH
	 * @see #randomString(int)
	 */
	public static String randomString() {
		return randomString(RANDOM_STRING_LENGTH);
	}
	
	/**
	 * Recursively {@link Executable#run} an {@link Executable} and all its children.
	 * @param executable The {@link Executable} to recursively {@link Executable#run}.
	 */
	/*public static void recursiveRun(Executable executable) {
		executable.run();
		Executable[] children = executable.getChildren();
		for(int i = 0 ; i < children.length ; i ++) {
			recursiveRun(children[i]);
		}
	}
	
	public static Executable[] getAllChildren(Executable executable) {
		Vector<Executable> children = new Vector<Executable>();
		Executable[] localChildren = executable.getChildren();
		Utils.arrayIntoVector(localChildren, children);
		for(int i = 0 ; i < localChildren.length ; i++) {
			Utils.arrayIntoVector(getAllChildren(localChildren[i]), children);
		}
		return children.toArray(new Executable[0]);
	}*/
}
