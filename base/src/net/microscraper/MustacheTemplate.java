package net.microscraper;

import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;

/**
 * Mustache-like substitutions from {@link Variables}.
 * This can substitute tags within {{ and }} with a value from {@link Variables}.
 * @author john
 *
 */
public final class MustacheTemplate {
	private final String string;
	
	/**
	 * 
	 * @param string The String from which Mustache tags will substituted by Variables.
	 * @throws MustacheTemplateException If <code>string</code> cannot be turned into a
	 * {@link MustacheTemplate}.
	 */
	public MustacheTemplate(String string) throws MustacheTemplateException {
		this.string = string;
	}
	
	/**
	 * Mustache-compile this {@link MustacheTemplate}.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link URL}'s compiled url as a {@link java.net.url}.
	 * @throws MissingVariableException If {@link Variables} was missing a key.
	 */
	public String compile(Variables variables) throws MissingVariableException {
		return compile(string, variables, null, null);
	}

	/**
	 * Attempt to compile a Mustache template with a {@link Variables}, URL encoding each value.
	 * @param template A string containing the template to compile.
	 * @param variables A {@link Variables} instance.
	 * @param browser The {@link Browser} used for encoding.
	 * @param encoding The encoding to use.
	 * @return The string, with tags substituted using the variables.
	 * @throws MissingVariableException The Variables instance was was missing one of the tags.
	 */
	public String compileEncoded(String template, Variables variables, Browser browser, String encoding)
		throws MissingVariableException {
		return compile(template, variables, browser, encoding);
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link MustacheTemplate}.
	 */
	public String toString() {
		return string;
	}
	

	public static final String open_tag = "{{";
	public static final String close_tag = "}}";
	
	private String compile(String template, Variables variables, Browser browser, String encoding)
		throws MissingVariableException {
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		String result = "";
		if(template == null) {
			throw new MustacheTemplateException("Cannot compile null string in mustache.");
		}
		while((open_tag_start_pos = template.indexOf(open_tag, close_tag_end_pos)) != -1) {
			
			// Pass unmodified text from the end of the last closed tag to the start of the current open tag.
			result += template.substring(close_tag_end_pos, open_tag_start_pos);
			
			int close_tag_start_pos = template.indexOf(close_tag, open_tag_start_pos);
			if(close_tag_start_pos == -1)
				throw new MustacheTemplateException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_start_pos + open_tag.length(), close_tag_start_pos);
			
			close_tag_end_pos = close_tag_start_pos + close_tag.length();
			if(variables.containsKey(tag)) {
				if(browser != null) {
					try {
						result += browser.encode(variables.get(tag), encoding);
					} catch(BrowserException e) {
						throw new MustacheTemplateException
								("Could not encode " + Utils.quote(variables.get(tag)) + " with " + encoding);
					}
				} else {
					result += variables.get(tag);
				}
			} else {
				throw new MissingVariableException(variables, tag);
			}
		}
		return result + template.substring(close_tag_end_pos);
	}
	
}
