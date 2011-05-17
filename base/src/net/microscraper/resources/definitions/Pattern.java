package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;

public class Pattern {
	public final MustacheTemplate pattern;
	public final boolean isCaseInsensitive;
	public final boolean isMultiline;
	public final boolean doesDotMatchNewline;
	public Pattern (MustacheTemplate pattern,
					boolean isCaseInsensitive, boolean isMultiline,
					boolean doesDotMatchNewline) {
		this.pattern = pattern;
		this.isCaseInsensitive = isCaseInsensitive;
		this.isMultiline = isMultiline;
		this.doesDotMatchNewline = doesDotMatchNewline;
	}
	
	private static final String PATTERN = "pattern";
	
	private static final String IS_CASE_SENSITIVE = "pattern";
	private static final boolean IS_CASE_SENSITIVE_DEFAULT = false;
	
	private static final String IS_MULTILINE = "pattern";
	private static final boolean IS_MULTILINE_DEFAULT = false;

	private static final String DOES_DOT_MATCH_ALL = "pattern";
	private static final boolean DOES_DOT_MATCH_ALL_DEFAULT = true;

	/**
	 * Deserialize a {@link Pattern} from a {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return A {@link Pattern} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of a Pattern.
	 */
	public static Pattern deserialize(Interfaces.JSON jsonInterface,
					Interfaces.JSON.Object jsonObject)
				throws DeserializationException {
		try {
			MustacheTemplate pattern = new MustacheTemplate(jsonObject.getString(PATTERN));
			boolean isCaseSensitive = jsonObject.has(IS_CASE_SENSITIVE) ? jsonObject.getBoolean(IS_CASE_SENSITIVE) : IS_CASE_SENSITIVE_DEFAULT;
			boolean isMultiline = jsonObject.has(IS_MULTILINE) ? jsonObject.getBoolean(IS_MULTILINE) : IS_MULTILINE_DEFAULT;
			boolean doesDotMatchAll = jsonObject.has(DOES_DOT_MATCH_ALL) ? jsonObject.getBoolean(DOES_DOT_MATCH_ALL) : DOES_DOT_MATCH_ALL_DEFAULT;
			
			return new Pattern(pattern, isCaseSensitive, isMultiline,
					doesDotMatchAll);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	
	/**
	 * Deserialize an array of {@link Pattern}s from a {@link Interfaces.JSON.Array}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonArray Input {@link Interfaces.JSON.Array} array.
	 * @return An array of {@link Pattern} instances.
	 * @throws DeserializationException If this is not a valid JSON serialization of an array of Patterns.
	 */
	public static Pattern[] deserializeArray(Interfaces.JSON jsonInterface,
				Interfaces.JSON.Array jsonArray)
			throws DeserializationException {
		Pattern[] patterns = new Pattern[jsonArray.length()];
		for(int i = 0 ; i < patterns.length ; i++) {
			try {
				patterns[i] = Pattern.deserialize(jsonInterface, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e ) {
					throw new DeserializationException(e, jsonArray, i);
			}
		}
		return patterns;
	}
}
