package net.microscraper.mustache;

import java.io.UnsupportedEncodingException;
import java.util.Vector;

import net.microscraper.util.Encoder;
import net.microscraper.util.Substitutable;
import net.microscraper.util.Execution;
import net.microscraper.util.Variables;

/**
 * Mustache-like substitutions from {@link Variables}.
 * This can substitute tags within {{ and }} with a value from {@link Variables}.
 * @author john
 *
 */
public final class MustacheTemplate implements Substitutable {
	private final String template;
	
	private MustacheTemplate(String template) throws MustacheCompilationException {
		this.template = template;
		
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		
		// Test for validity.
		while((open_tag_start_pos = template.indexOf(open_tag, close_tag_end_pos)) != -1) {
						
			int close_tag_start_pos = template.indexOf(close_tag, open_tag_start_pos);
			if(close_tag_start_pos == -1)
				throw new MustacheCompilationException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);			
			close_tag_end_pos = close_tag_start_pos + close_tag.length();
		}
	}

	public static final String open_tag = "{{";
	public static final String close_tag = "}}";
	
	/**
	 * Compile a {@link MustacheTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link MustacheTemplate}.
	 * @return A {@link MustacheTemplate}.
	 * @throws MustacheCompilationException If <code>template</code> cannot be turned into a
	 * {@link MustacheTemplate}.
	 */
	public static MustacheTemplate compile(String template)
			throws MustacheCompilationException {
		return new MustacheTemplate(template);
	}
	

	/**
	 * Substitute the values from a {@link Variables} into the {@link MustacheTemplate}.
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
	 * Substitute the values from a {@link Variables} into the {@link MustacheTemplate},
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
		while((open_tag_start_pos = template.indexOf(open_tag, close_tag_end_pos)) != -1) {
			
			// Pass unmodified text from the end of the last closed tag to the start of the current open tag.
			result += template.substring(close_tag_end_pos, open_tag_start_pos);
			
			int close_tag_start_pos = template.indexOf(close_tag, open_tag_start_pos);
			//if(close_tag_start_pos == -1)
			//	throw new IllegalStateException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_start_pos + open_tag.length(), close_tag_start_pos);
			
			close_tag_end_pos = close_tag_start_pos + close_tag.length();
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
			return Execution.fail(missingVariablesAry);
		}
		
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link MustacheTemplate}.
	 */
	public String toString() {
		return template;
	}
	
}
