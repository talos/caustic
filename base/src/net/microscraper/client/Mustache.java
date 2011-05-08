package net.microscraper.client;

import net.microscraper.resources.definitions.Scraper.ScraperResult;

/**
 * Mustache-like substitutions from Variables.
 * This does not currently support any commenting.
 * @author john
 *
 */
public class Mustache {
	public static final String open_tag = "{{";
	public static final String close_tag = "}}";
	
	/**
	 * Attempt to compile a template from data in a Variables instance.
	 * @param template
	 * @param variables
	 * @return
	 * @throws TemplateException The template was invalid.
	 * @throws MissingVariable The Variables instance was missing a variable.
	 */
	public static String compile(String template, ScraperResult[] variables)
				throws TemplateException, MissingVariable {
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		String result = "";
		if(template == null) {
			throw new TemplateException("Cannot compile null string in mustache.");
		}
		while((open_tag_start_pos = template.indexOf(open_tag, close_tag_end_pos)) != -1) {
			
			// Pass unmodified text from the end of the last closed tag to the start of the current open tag.
			result += template.substring(close_tag_end_pos, open_tag_start_pos);
			
			int close_tag_start_pos = template.indexOf(close_tag, open_tag_start_pos);
			if(close_tag_start_pos == -1)
				throw new TemplateException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_start_pos + open_tag.length(), close_tag_start_pos);
			
			close_tag_end_pos = close_tag_start_pos + close_tag.length();
			if(variables.containsKey(tag))
				result += variables.get(tag);
			else
				throw new MissingVariable(tag, variables);
		}
		return result + template.substring(close_tag_end_pos);
	}
	
	public static class TemplateException extends Exception {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5637439870858686438L;
		public TemplateException(String msg) { super(msg); }
		//public TemplateException(Execution caller, String msg) { super(caller, msg); }
	}
	
	public static class MissingVariable extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8720790457856091375L;
		public final String missingVariable;
		public MissingVariable(String missingVariable, Variables variables) {
			//super(caller);
			super("Missing variable " + missingVariable);
			this.missingVariable = missingVariable;
		}
	}
}
