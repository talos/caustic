package net.microscraper.client;

import java.util.Hashtable;

/**
 * Mustache-like substitutions from Hashtables.
 * This does not currently support any commenting.
 * @author john
 *
 */
public class Mustache {
	public static final String open_tag = "{{";
	public static final String close_tag = "}}";
	
	/**
	 * Attempt to compile a template from data in a Hashtable.
	 * @param template
	 * @param variables
	 * @return
	 * @throws TemplateException The template was invalid.
	 * @throws MissingVariable The Hashtable was missing a variable.
	 */
	public static String compile(String template, Hashtable variables)
				throws TemplateException, MissingVariable {
		int close_tag_pos = 0;
		int open_tag_pos;
		String result = "";
		while((open_tag_pos = template.indexOf(open_tag, close_tag_pos)) != -1) {
			result += template.substring(open_tag_pos, close_tag_pos);
			
			close_tag_pos = template.indexOf(close_tag, open_tag_pos);
			if(close_tag_pos == -1)
				throw new TemplateException("No close tag for opening tag at position " + open_tag_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_pos + 2, close_tag_pos - 2);
			
			if(variables.containsKey(tag))
				result += variables.get(tag);
			else
				throw new MissingVariable(tag);
		}
		return result + template.substring(close_tag_pos);
	}
	
	public static class TemplateException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5637439870858686438L;

		public TemplateException(String msg) { super(msg); }
	}
	
	public static class MissingVariable extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8720790457856091375L;

		public MissingVariable(String tag) { super("Variable " + tag + " is missing, cannot compile template."); }
	}
}
