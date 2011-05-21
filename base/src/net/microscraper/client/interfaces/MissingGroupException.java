package net.microscraper.client.interfaces;

/**
 * Exception to indicate that the pattern did not have the backreference group it was expected to have.
 * @author john
 *
 */
public class MissingGroupException extends Exception {
	
	public MissingGroupException(PatternInterface pattern, int group) {
		super("'" + pattern.toString() + "' did not contain a group '"
				+ Integer.toString(group) + "'");
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1808377327875482874L;
	
}