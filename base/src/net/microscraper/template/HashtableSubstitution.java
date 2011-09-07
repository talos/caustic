package net.microscraper.template;

import java.util.Hashtable;

/**
 * Implementation of {@link DependsOnTemplate} where a successful substitution
 * is a {@link Hashtable}.
 * @author talos
 *
 */
public class HashtableSubstitution extends MissingTags {
	private final Hashtable substituted;
	
	private HashtableSubstitution(String[] missingTags) {
		super(missingTags);
		this.substituted = null;
	}

	private HashtableSubstitution(Hashtable substituted) {
		this.substituted = substituted;
	}
	
	/**
	 * 
	 * @param substitutedString The successful substituted {@link Hashtable}.
	 * @return A {@link HashtableSubstitution} with a successful substitution.
	 */
	public static HashtableSubstitution newSuccess(Hashtable substitutedHashtable) {
		return new HashtableSubstitution(substitutedHashtable);
	}

	/**
	 * 
	 * @param missingTags An array of {@link String} missing tags.
	 * @return A {@link HashtableSubstitution} with missing tags.
	 */
	public static HashtableSubstitution newMissingTags(String[] missingTags) {
		return new HashtableSubstitution(missingTags);
	}
	
	/**
	 * 
	 * @return The {@link Hashtable} successful substitution.  Should only be called
	 * if {@link #isMissingTags()} is <code>false</code>.
	 */
	public Hashtable getSubstituted() {
		if(substituted == null) {
			throw new IllegalStateException("Missing tags.");
		} else {
			return substituted;
		}
	}

}
