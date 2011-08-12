package net.microscraper.interfaces.regexp;

/**
 * Exception to indicate that the pattern did not have the backreference group
 * it was expected to have.
 * @author john
 *
 */
public class MissingGroupException extends RegexpException {
	private final int missingGroup;
	public MissingGroupException(PatternInterface pattern, int missingGroup) {
		super(pattern, "Backreference group " + missingGroup + " not in pattern.");
		this.missingGroup = missingGroup;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -1808377327875482874L;
	
}