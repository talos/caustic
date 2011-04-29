package net.microscraper.client;

import net.microscraper.database.Execution;
import net.microscraper.database.Status.DelayExecution;

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
	public static String compile(Execution caller, String template, Variables variables)
				throws TemplateException, MissingVariable {
		int close_tag_pos = 0;
		int open_tag_pos;
		String result = "";
		if(template == null) {
			throw new TemplateException("Cannot compile null string in mustache.");
		}
		while((open_tag_pos = template.indexOf(open_tag, close_tag_pos)) != -1) {
			result += template.substring(open_tag_pos, close_tag_pos);
			
			close_tag_pos = template.indexOf(close_tag, open_tag_pos);
			if(close_tag_pos == -1)
				throw new TemplateException("No close tag for opening tag at position " + open_tag_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_pos + open_tag.length(), close_tag_pos);
			
			close_tag_pos += close_tag.length();
			if(variables.containsKey(tag))
				result += variables.get(tag);
			else
				throw new MissingVariable(caller, tag, variables);
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
	
	public static class MissingVariable extends Exception implements DelayExecution {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8720790457856091375L;
		public final String missingVariable;
		private final Execution caller;
		public MissingVariable(Execution caller, String missingVariable, Variables variables) {
			this.missingVariable = missingVariable;
			this.caller = caller;
		}
		public Execution callerExecution() {
			return caller;
		}
		public String reason() {
			return "Missing variable " + missingVariable;
		}
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(!(obj instanceof MissingVariable))
				return false;
			MissingVariable other = (MissingVariable) obj;
			if(this.caller.equals(other.caller) && this.missingVariable.equals(other.missingVariable))
				return true;
			return false;
		}
	}
}
