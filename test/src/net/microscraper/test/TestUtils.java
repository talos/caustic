package net.microscraper.test;

import java.util.Random;
import java.util.Vector;

import net.microscraper.Executable;
import net.microscraper.Utils;

public class TestUtils {
	
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static Random rnd = new Random();
	
	public static int getRandomInt(int max) {
		return rnd.nextInt(max);
	}
	
	/**
	 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
	 * @param length The length of the random string.
	 * @return A {@link String} of random characters of the specified length.
	 */
	public static String makeRandomString(int length) {
	   StringBuilder sb = new StringBuilder(length);
	   for( int i = 0; i < length; i++ ) {
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   }
	   return sb.toString();
	}
	
	/**
	 * Recursively {@link Executable#run} an {@link Executable} and all its children.
	 * @param executable The {@link Executable} to recursively {@link Executable#run}.
	 */
	public static void recursiveRun(Executable executable) {
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
	}
}
