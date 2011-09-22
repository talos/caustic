package net.microscraper.template;

import java.util.Vector;

import net.microscraper.database.DatabaseReadException;
import net.microscraper.database.DatabaseView;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.StringTemplate;
import net.microscraper.util.Encoder;
import net.microscraper.util.VectorUtils;

/**
 * {@link String} substitutions using {@link DatabaseView}.
 * This substitutes a key within {@link #openEncoded} and
 * {@link #closeEncoded} with a value from {@link DatabaseView}.
 * @author john
 *
 */
public final class StringTemplateDeprec implements StringTemplate {
/*
	private final String template;
	
	private final Pattern encodedPattern;
	
	private final Pattern notEncodedPattern;
	
	private final Encoder encoder;
	
	private final boolean isStatic;
	*/
	/**
	 * Compile an always-successful {@link StringTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link StringTemplate},
	 * which will always be returned as the success value of {@link StringSubstitution}.
	 */
	/*private StringTemplate(String template) {
		this.template = template;
		this.encodedPattern = null;
		this.notEncodedPattern = null;
		this.isStatic = true;
		this.encoder = null;
	}*/
	
	/**
	 * Compile a {@link StringTemplate} from a {@link String}.
	 * @param template The {@link String} to convert into a {@link StringTemplate}.
	 * @param encodedPattern The {@link Pattern} that identifies encoded tags whose values
	 * will be encoded.
	 * @param notEncodedPattern The {@link Pattern} that identifies tags whose values will not
	 * be encoded.
	 * @param encoder The {@link Encoder} to use when encoding.
	 * @throws TemplateCompilationException If <code>template</code> cannot be turned into a
	 * {@link StringTemplate}.
	 */
	/*
	public StringTemplate(String template, Pattern encodedPattern, Pattern notEncodedPattern,
						Encoder encoder) throws TemplateCompilationException {
		this.template = template;
		this.encodedPattern = encodedPattern;
		this.notEncodedPattern = notEncodedPattern;
		this.encoder = encoder;
		this.isStatic = false;
	}*/

	/* (non-Javadoc)
	 * @see net.microscraper.template.StringTemplate#sub(net.microscraper.database.DatabaseView)
	 */
	public StringSubstitution sub(DatabaseView input) throws DatabaseReadException {
		return null;
	}
	
	/**
	 * Substitute the values from a {@link HashtableDatabase} into the {@link StringTemplate},
	 * and encode each value upon inserting it.
	 * @param input The {@link DatabaseView} to use when substituting.
	 * @param encoder The {@link Encoder} to use when encoding values.
	 * @return A {@link StringSubstitution} with the results of the substitution.
	 * @throws DatabaseReadException if <code>input</code> could not be read from.
	 */
	/*public StringSubstitution subEncoded(DatabaseView input) throws DatabaseReadException {
		// cut out immediately for static templates -- TODO should be a separate class sharing an interface
		if(isStatic == true) {
			return StringSubstitution.success(template);
		}
		
		StringBuffer buf = new StringBuffer(template);		
		Vector missingTags = new Vector();
		int startPosEncoded, startPosNotEncoded;
		
		// loop while at least one of the start tokens is there.
		while((startPosEncoded = buf.indexOf(openEncoded)) != -1 ||
				(startPosNotEncoded = buf.indexOf(openNotEncoded)) != -1) {
			
			final boolean isEncoded;
			// is the earlier tag match the encoded or the not encoded?
			if(startPosEncoded == -1) { // did not find encoded
				isEncoded = false;
			} else if(startPosNotEncoded == -1) { // did not find not encoded
				isEncoded = true;
			} else { // found both
				if(startPosEncoded < startPosNotEncoded) {
					isEncoded = true;
				} else if(startPosEncoded == startPosNotEncoded) {
					// in case of ambiguity, go with the longer tag, of which
					// the shorter tag must be a substring (were this to happen)
					if(openEncoded.length() > openNotEncoded.length()) {
						isEncoded = true;
					} else {
						isEncoded = false;					
					}
				} else {
					isEncoded = false;
				}
			}
			
			int startPos;
			String starter;
			String ender;
			if(isEncoded) {
				startPos = startPosEncoded;
				starter = openEncoded;
				ender = closeEncoded;
			} else {
				startPos = startPosNotEncoded;
				starter = openNotEncoded;
				ender = closeNotEncoded;
			}
			
			int endPos = isEncoded ? buf.indexOf(closeEncoded) : buf.indexOf(closeNotEncoded);
			
			if(endPos == -1) {
				// this should not happen.
			}
			
			String tagName;
			if(isEncoded) {
				tagName = buf.substring(startPos + openEncoded.length(), endPos);
			} else {
				tagName = buf.substring(startPos + openNotEncoded.length(), endPos);				
			}
			
			String value = (String) input.get(tagName);
			if(value == null) {
				
			} else {
				missingTags.add(tagName);
			}
		}
		
		while((encodedStartPos = buf.indexOf(openEncoded)) != -1) {
			
			int closeTagStartPos = template.indexOf(closeEncoded, encodedStartPos);
			
			String tag = template.substring(encodedStartPos + openEncoded.length(), closeTagStartPos);
			
			encodedEndPos = closeTagStartPos + closeEncoded.length();
			String value = (String) input.get(tag);
			if(value != null) {
				if(encoder != null) {
					result += encoder.encode(value);	
				} else {
					result += value;
				}
			} else {
				missingTags.add(tag);
			}
		}
		
		if(missingTags.size() == 0) {
			return StringSubstitution.success(result + template.substring(encodedEndPos));
		} else {
			String[] missingTagsAry = new String[missingTags.size()];
			missingTags.copyInto(missingTagsAry);
			return StringSubstitution.missingTags(missingTagsAry);
		}
	}*/
	
	/**
	 * @return The raw, uncompiled {@link String} for this {@link StringTemplate}.
	 */
	/*public String toString() {
		return template;
	}
	*/
	/**
	 * Creates a {@link StringTemplate} that will run successfully no matter what
	 * {@link DatabaseView} is passed to {@link #sub}.
	 * @param alwaysSubbed The {@link String} that will always be returned from
	 * the returned object's {@link StringSubstitution}
	 * @return an always-successful {@link StringTemplate}.
	 */
	/*public static StringTemplate staticTemplate(String alwaysSubbed) {
		return new StringTemplate(alwaysSubbed);
	}*/
	
}
