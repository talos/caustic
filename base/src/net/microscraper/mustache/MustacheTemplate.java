package net.microscraper.mustache;

import net.microscraper.client.Browser;
import net.microscraper.client.BrowserException;
import net.microscraper.util.StringUtils;
import net.microscraper.util.Variables;

/**
 * Mustache-like substitutions from {@link Variables}.
 * This can substitute tags within {{ and }} with a value from {@link Variables}.
 * @author john
 *
 */
public final class MustacheTemplate {

	/**
	 * 
	 * @param template The {@link String} to convert into a {@link MustacheTemplate}.
	 * @return A {@link MustacheTemplate}.
	 * @throws MustacheCompilationException If <code>template</code> cannot be turned into a
	 * {@link MustacheTemplate}.
	 */
	public static MustacheTemplate compile(String template)
			throws MustacheCompilationException {
		return new MustacheTemplate(template, null);
	}

	/**
	 * 
	 * @param template The {@link String} to convert into a {@link MustacheTemplate}.
	 * @return A {@link MustacheTemplate}.
	 * @throws MustacheCompilationException If <code>template</code> cannot be turned into a
	 * {@link MustacheTemplate}.
	 */
	public static MustacheTemplate compile(String template, Encoder encoder)
			throws MustacheCompilationException {
		return new MustacheTemplate(template, encoder);
	}
	
	private final String template;
	private final Encoder encoder;
	
	
	/**
	 * Mustache-compile this {@link MustacheTemplate}.
	 * @param variables A {@link Variables} instance to compile with.
	 * @return The {@link URL}'s compiled url as a {@link java.net.url}.
	 */
	private MustacheSubstitution(String template, Encoder encoder) throws MustacheCompilationException {
		this.template = template;
		this.encoder = encoder;
		
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		while((open_tag_start_pos = template.indexOf(open_tag, close_tag_end_pos)) != -1) {
						
			int close_tag_start_pos = template.indexOf(close_tag, open_tag_start_pos);
			if(close_tag_start_pos == -1)
				throw new MustacheCompilationException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);			
			close_tag_end_pos = close_tag_start_pos + close_tag.length();
		}
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link MustacheTemplate}.
	 */
	public String toString() {
		return template;
	}
	

	public static final String open_tag = "{{";
	public static final String close_tag = "}}";
	
	public MustacheSubstitution sub(Variables variables) {
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		String result = "";
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
					result += encoder.encode(variables.get(tag));	
				} else {
					result += variables.get(tag);
				}
			} else {
				return MustacheSubstitution.failed(tag);
			}
		}
		return MustacheSubstitution.success(result + template.substring(close_tag_end_pos));
	}
	
}
