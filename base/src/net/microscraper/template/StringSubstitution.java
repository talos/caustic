package net.microscraper.template;

/**
 * Implementation of {@link DependsOnTemplate} where a successful substitution
 * is a {@link String}.
 * @author talos
 *
 */
public class StringSubstitution extends MissingTags {
	private final String substituted;
	
	private StringSubstitution(String[] missingTags) {
		super(missingTags);
		substituted = null;
	}

	private StringSubstitution(String substituted) {
		this.substituted = substituted;
	}
	
	/**
	 * 
	 * @param substitutedString The successful substituted {@link String}.
	 * @return A {@link StringSubstitution} with a successful substitution.
	 */
	public static StringSubstitution success(String substitutedString) {
		return new StringSubstitution(substitutedString);
	}

	/**
	 * 
	 * @param missingTags An array of {@link String} missing tags.
	 * @return A {@link StringSubstitution} with missing tags.
	 */
	public static StringSubstitution missingTags(String[] missingTags) {
		return new StringSubstitution(missingTags);
	}
	
	/**
	 * 
	 * @return The {@link String} successful substitution.  Should only be called
	 * if {@link #isMissingTags()} is <code>false</code>.
	 */
	public String getSubstituted() {
		if(substituted == null) {
			throw new IllegalStateException("Missing tags.");
		} else {
			return substituted;
		}
	}
}
