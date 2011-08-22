package net.microscraper.template;

/**
 * This is thrown when a {@link String} cannot be turned into a {@link Template}.
 * @author john
 *
 */
public class TemplateCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5637439870858686438L;
	public TemplateCompilationException(String msg) {
		super(msg);
	}
}
