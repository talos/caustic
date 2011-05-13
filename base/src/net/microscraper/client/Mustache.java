package net.microscraper.client;

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
	public static String compile(String template, Variables variables)
				throws MustacheTemplateException, MissingVariable {
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
			if(variables.containsKey(tag))
				result += variables.get(tag);
			else
				throw new MissingVariable(tag);
		}
		return result + template.substring(close_tag_end_pos);
	}
	
}
