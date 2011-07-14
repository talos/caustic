package net.microscraper.server.instruction;

import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Instruction;

/**
 * A regular expression {@link Instruction}.
 * @author john
 *
 */
public class Regexp extends Instruction {
	/**
	 * The {@link Regexp}'s pattern.  Mustache compiled before it is used.
	 */
	public final MustacheTemplate pattern;
	
	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean isCaseSensitive;
	

	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean isMultiline;

	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean doesDotMatchNewline;
	
	/**
	 * Deserialize a {@link Regexp} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Regexp} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a
	 * {@link Regexp},
	 * or the location is invalid.
	 */
	public Regexp (JSONInterfaceObject jsonObject) throws DeserializationException {
		super(jsonObject.getLocation());
		try {
			pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			doesDotMatchNewline = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
		
	private static final String PATTERN = "pattern";
	
	private static final String IS_CASE_SENSITIVE = "case_sensitive";
	private static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	private static final String IS_MULTILINE = "multiline";
	private static final boolean IS_MULTILINE_DEFAULT = false;

	private static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	private static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;
}
