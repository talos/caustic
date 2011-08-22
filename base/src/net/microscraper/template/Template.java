package net.microscraper.template;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import net.microscraper.util.Encoder;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * Substitutions using {@link Variables}.
 * This substitutes a key within {@link #openTag} and
 * {@link #closeTag} with a value from {@link Variables}.
 * @author john
 *
 */
public final class Template implements Substitutable {

	/**
	 * Default {@link #openTag}.
	 */
	public static final String DEFAULT_OPEN_TAG = "{{";
	
	/**
	 * Default {@link #closeTag}.
	 */
	public static final String DEFAULT_CLOSE_TAG = "}}";
	
	private final String template;
	
	/**
	 * {@link String} to mark the start of a key that will be substituted with a value
	 * from {@link Variables}.
	 */
	private final String openTag;
	
	/**
	 * {@link String} to mark the end of a key that will be substituted with a value
	 * from {@link Variables}.
	 */
	private final String closeTag;
	
	private Template(String template, String openTag, String closeTag) throws TemplateCompilationException {
		this.template = template;
		this.openTag = openTag;
		this.closeTag = closeTag;
		
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		
		// Test for validity.
		while((open_tag_start_pos = template.indexOf(openTag, close_tag_end_pos)) != -1) {
						
			int close_tag_start_pos = template.indexOf(closeTag, open_tag_start_pos);
			if(close_tag_start_pos == -1)
				throw new TemplateCompilationException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);			
			close_tag_end_pos = close_tag_start_pos + closeTag.length();
		}
	}
	
	
	/**
	 * Compile a {@link Template} from a {@link String} using {@link #DEFAULT_OPEN_TAG}
	 * and {@link #DEFAULT_CLOSE_TAG}
	 * @param template The {@link String} to convert into a {@link Template}.
	 * @return A {@link Template}.
	 * @throws TemplateCompilationException If <code>template</code> cannot be turned into a
	 * {@link Template}.
	 */
	public static Template compile(String template)
			throws TemplateCompilationException {
		return new Template(template, DEFAULT_OPEN_TAG, DEFAULT_CLOSE_TAG);
	}

	/**
	 * Substitute the values from a {@link Variables} into the {@link Template}.
	 * @param variables The {@link Variables} to use in the substitution.
	 * @return A {@link Execution} with the results of the substitution.
	 */
	public Execution sub(Variables variables) {
		try {
			return sub(variables, null, null);
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e); // should be impossible
		};
	}
	
	/**
	 * Substitute the values from a {@link Variables} into the {@link Template},
	 * and encode each value upon inserting it.
	 * @param variables The {@link Variables} to use in the substitution.
	 * @param encoder The {@link Encoder} to use when encoding values.
	 * @param encoding The {@link String} encoding for <code>encoder</code> to use.
	 * @return A {@link Execution} with the results of the substitution.
	 * @throws UnsupportedEncodingException if <code>encoding</code> is not supported.
	 */
	public Execution sub(Variables variables, Encoder encoder, String encoding) throws UnsupportedEncodingException {
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		String result = "";
		Vector missingVariables = new Vector();
		while((open_tag_start_pos = template.indexOf(openTag, close_tag_end_pos)) != -1) {
			
			// Pass unmodified text from the end of the last closed tag to the start of the current open tag.
			result += template.substring(close_tag_end_pos, open_tag_start_pos);
			
			int close_tag_start_pos = template.indexOf(closeTag, open_tag_start_pos);
			//if(close_tag_start_pos == -1)
			//	throw new IllegalStateException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_start_pos + openTag.length(), close_tag_start_pos);
			
			close_tag_end_pos = close_tag_start_pos + closeTag.length();
			if(variables.containsKey(tag)) {
				if(encoder != null) {
					result += encoder.encode(variables.get(tag), encoding);	
				} else {
					result += variables.get(tag);
				}
			} else {
				//return Substitution.fail(tag);
				missingVariables.add(tag);
			}
		}
		
		if(missingVariables.size() == 0) {
			return Execution.success(result + template.substring(close_tag_end_pos));
		} else {
			String[] missingVariablesAry = new String[missingVariables.size()];
			missingVariables.copyInto(missingVariablesAry);
			return Execution.missingVariables(missingVariablesAry);
		}
		
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link Template}.
	 */
	public String toString() {
		return template;
	}
}
