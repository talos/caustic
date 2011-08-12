package net.microscraper;

/**
 * This is thrown when {@link Mustache#compile(String, Variables)} fails.
 * @author john
 *
 */
public class MustacheTemplateException extends MicroscraperException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5637439870858686438L;
	public MustacheTemplateException(String msg) {
		super(msg);
	}
}
