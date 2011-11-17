package net.caustic.template;

import java.util.Hashtable;

/**
 * Implementation of {@link DependsOnTemplate} where a successful substitution
 * is a {@link Hashtable}.
 * @author talos
 *
 */
public class HashtableSubstitution implements DependsOnTemplate {
	private Hashtable substituted;
	private String[] missingTags;
	
	private HashtableSubstitution(String[] missingTags) {
		this.missingTags = missingTags;
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

	public boolean isMissingTags() {
		return missingTags != null;
	}

	public String[] getMissingTags() {
		return missingTags;
	}

}
