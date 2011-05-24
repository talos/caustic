package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.MustacheTemplate;
import net.microscraper.server.Resource;

/**
 * A regular expression {@link Resource}.
 * @author john
 *
 */
public class Regexp extends Resource {
	/**
	 * The {@link Regexp}'s pattern.  Mustache compiled before it is used.
	 */
	public final MustacheTemplate pattern;
	
	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean isCaseInsensitive;
	

	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean isMultiline;

	/**
	 * @see net.microscraper.client.interfaces.JSONInterface#compile
	 */
	public final boolean doesDotMatchNewline;
	
	public Regexp (URIInterface location, MustacheTemplate pattern,
					boolean isCaseInsensitive, boolean isMultiline,
					boolean doesDotMatchNewline) throws URIMustBeAbsoluteException {
		super(location);
		this.pattern = pattern;
		this.isCaseInsensitive = isCaseInsensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
		
	private static final String PATTERN = "pattern";
	
	private static final String IS_CASE_SENSITIVE = "case_sensitive";
	private static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	private static final String IS_MULTILINE = "multiline";
	private static final boolean IS_MULTILINE_DEFAULT = false;

	private static final String DOES_DOT_MATCH_ALL = "dot_matches_all";
	private static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;

	/**
	 * Deserialize a {@link Regexp} from a {@link JSONInterfaceObject}.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return A {@link Regexp} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a Pattern,
	 * or the location is invalid.
	 */
	public static Resource deserialize(JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {			
			MustacheTemplate pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			boolean isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			boolean isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			boolean doesDotMatchAll = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
			
			return new Regexp(jsonObject.getLocation(), pattern, isCaseSensitive, isMultiline,
					doesDotMatchAll);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		} catch (URIMustBeAbsoluteException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link Regexp}s from a {@link JSONInterfaceArray}.
	 * @param jsonArray Input {@link JSONInterfaceArray} array.
	 * @return An array of {@link Regexp} instances.
	 * @throws DeserializationException If this is not a valid JSON serialization of an array of {@link Regexp}s.
	 */
	public static Regexp[] deserializeArray(JSONInterfaceArray jsonArray)
			throws DeserializationException {
		Regexp[] patterns = new Regexp[jsonArray.length()];
		for(int i = 0 ; i < patterns.length ; i++) {
			try {
				patterns[i] = (Regexp) Regexp.deserialize(jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e ) {
					throw new DeserializationException(e, jsonArray, i);
			}
		}
		return patterns;
	}
	
}
