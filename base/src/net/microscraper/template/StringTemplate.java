package net.microscraper.template;

import java.util.Vector;

import net.microscraper.database.Database;
import net.microscraper.database.Scope;
import net.microscraper.util.Encoder;

/**
 * {@link String} substitutions using {@link Database}.
 * This substitutes a key within {@link #openTag} and
 * {@link #closeTag} with a value from {@link HashtableDatabase}.
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
	
	/**
	 * The {@link Database} to pull values for substitutions from.
	 */
	private final Database database;
	
	private final String template;
	
	/**
	 * {@link String} to mark the start of a key that will be substituted with a value
	 * from {@link HashtableDatabase}.
	 */
	private final String openTag;
	
	/**
	 * {@link String} to mark the end of a key that will be substituted with a value
	 * from {@link HashtableDatabase}.
	 */
	private final String closeTag;

	/**
	 * Compile a {@link StringTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link StringTemplate}.
	 * @param openTag The {@link String} that opens a tag.
	 * @param closeTag The {@link String} that closes a tag.
	 * @param database The {@link Database} to use to pull values for substitutions.
	 * @throws TemplateCompilationException If <code>template</code> cannot be turned into a
	 * {@link StringTemplate}.
	 */
	public StringTemplate(String template, String openTag, String closeTag, Database database)
			throws TemplateCompilationException {
		this.template = template;
		this.openTag = openTag;
		this.closeTag = closeTag;
		this.database = database;
		
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
	 * @param scope The {@link Scope} to use getting data from {@link #database}.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 */
	public StringSubstitution sub(Scope scope) {
		return subEncoded(scope, null);
	}
	
	/**
	 * Substitute the values from a {@link HashtableDatabase} into the {@link StringTemplate},
	 * and encode each value upon inserting it.
	 * @param scope The {@link Scope} to use getting data from {@link #database}.
	 * @param encoder The {@link Encoder} to use when encoding values.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 */
	public StringSubstitution subEncoded(Scope scope, Encoder encoder) {
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
			String value = database.get(scope, tag);
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
			return StringSubstitution.newSuccess(result + template.substring(close_tag_end_pos));
		} else {
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return StringSubstitution.newMissingTags(missingTagsAry);
		}
		
	}
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link StringTemplate}.
	 */
	public String toString() {
		return template;
	}
}
