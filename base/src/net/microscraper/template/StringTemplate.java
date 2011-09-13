package net.microscraper.template;

import java.util.Vector;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.util.Encoder;

/**
 * {@link String} substitutions using {@link DatabaseView}.
 * This substitutes a key within {@link #openTag} and
 * {@link #closeTag} with a value from {@link DatabaseView}.
 * @author john
 *
 */
public final class StringTemplate {

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
	 * from {@link DatabaseView}.
	 */
	private final String openTag;
	
	/**
	 * {@link String} to mark the end of a key that will be substituted with a value
	 * from {@link DatabaseView}.
	 */
	private final String closeTag;
	
	private final boolean isStatic;
	
	/**
	 * Compile an always-successful {@link StringTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link StringTemplate},
	 * which will always be returned as the success value of {@link StringSubstitution}.
	 */
	private StringTemplate(String template) {
		this.template = template;
		this.openTag = null;
		this.closeTag = null;
		this.isStatic = true;
	}
	
	/**
	 * Compile a {@link StringTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link StringTemplate}.
	 * @param openTag The {@link String} that opens a tag.
	 * @param closeTag The {@link String} that closes a tag.
	 * @throws TemplateCompilationException If <code>template</code> cannot be turned into a
	 * {@link StringTemplate}.
	 */
	public StringTemplate(String template, String openTag, String closeTag)
			throws TemplateCompilationException {
		this.template = template;
		this.openTag = openTag;
		this.closeTag = closeTag;
		this.isStatic = false;
		
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
	 * Substitute the values from a {@link Variables} into the {@link StringTemplate}.
	 * @param input The {@link DatabaseView} to use when substituting.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 * @throws DatabaseReadException if <code>input</code> could not be read from.
	 */
	public StringSubstitution sub(DatabaseView input) throws DatabaseReadException {
		return subEncoded(input, null);
	}
	
	/**
	 * Substitute the values from a {@link HashtableDatabase} into the {@link StringTemplate},
	 * and encode each value upon inserting it.
	 * @param input The {@link DatabaseView} to use when substituting.
	 * @param encoder The {@link Encoder} to use when encoding values.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 * @throws DatabaseReadException if <code>input</code> could not be read from.
	 */
	public StringSubstitution subEncoded(DatabaseView input, Encoder encoder) throws DatabaseReadException {
		if(isStatic == true) {
			return StringSubstitution.success(template);
		}
		int close_tag_end_pos = 0;
		int open_tag_start_pos;
		String result = "";
		Vector missingTags = new Vector();
		while((open_tag_start_pos = template.indexOf(openTag, close_tag_end_pos)) != -1) {
			
			// Pass unmodified text from the end of the last closed tag to the start of the current open tag.
			result += template.substring(close_tag_end_pos, open_tag_start_pos);
			
			int close_tag_start_pos = template.indexOf(closeTag, open_tag_start_pos);
			//if(close_tag_start_pos == -1)
			//	throw new IllegalStateException("No close tag for opening tag at position " + open_tag_start_pos + " in Mustache template " + template);
			
			String tag = template.substring(open_tag_start_pos + openTag.length(), close_tag_start_pos);
			
			close_tag_end_pos = close_tag_start_pos + closeTag.length();
			String value = (String) input.get(tag);
			if(value != null) {
				if(encoder != null) {
					result += encoder.encode(value);	
				} else {
					result += value;
				}
			} else {
				//return Substitution.fail(tag);
				missingTags.add(tag);
			}
		}
		
		if(missingTags.size() == 0) {
			return StringSubstitution.success(result + template.substring(close_tag_end_pos));
		} else {
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return StringSubstitution.missingTags(missingTagsAry);
		}
		
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link StringTemplate}.
	 */
	public String toString() {
		return template;
	}
	
	/**
	 * Creates a {@link StringTemplate} that will run successfully no matter what
	 * {@link DatabaseView} is passed to {@link #sub}.
	 * @param alwaysSubbed The {@link String} that will always be returned from
	 * the returned object's {@link StringSubstitution}
	 * @return an always-successful {@link StringTemplate}.
	 */
	public static StringTemplate staticTemplate(String alwaysSubbed) {
		return new StringTemplate(alwaysSubbed);
	}
}
