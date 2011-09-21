package net.microscraper.template;

import net.microscraper.regexp.StringTemplate;

/**
 * This is thrown when a {@link String} cannot be turned into a {@link StringTemplate}.
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
