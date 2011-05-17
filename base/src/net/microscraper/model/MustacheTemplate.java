package net.microscraper.model;

/**
 * A string where Mustache-style demarcation denotes where Variables are taken out.
 * @author john
 *
 */
public final class MustacheTemplate {
	public final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 */
	public MustacheTemplate(String string) {
		this.string = string;
	}
}
