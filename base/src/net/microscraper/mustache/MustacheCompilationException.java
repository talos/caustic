package net.microscraper.mustache;

/**
 * This is thrown when a {@link String} cannot be turned into a {@link MustacheTemplate}.
 * @author john
 *
 */
public class MustacheCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5637439870858686438L;
	public MustacheCompilationException(String msg) {
		super(msg);
	}
}
