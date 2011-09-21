package net.microscraper.template;

import java.util.Vector;

import net.microscraper.util.VectorUtils;

/**
 * Implementation of {@link DependsOnTemplate} where a successful substitution
 * is a {@link String}.
 * @author talos
 *
 */
public class StringSubstitution implements DependsOnTemplate {
	private String substituted;
	private String[] missingTags;
	
	private StringSubstitution(String[] missingTags) {
		this.missingTags = missingTags;
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
	
	public boolean isMissingTags() {
		return missingTags != null;
	}

	public String[] getMissingTags() {
		return missingTags;
	}

	/**
	 * 
	 * @param couldBeMissingTags An array of {@link DependsOnTemplate} whose {@link #missingTags}
	 * should be combined.
	 * @return A {@link String} array of all the missing tags.  Zero-length if there are none.
	 */
	public static String[] combine(DependsOnTemplate[] couldBeMissingTags) {
		Vector missingTags = new Vector();
		for(int i = 0 ; i < couldBeMissingTags.length ; i ++) {
			if(couldBeMissingTags[i].isMissingTags()) {
				VectorUtils.arrayIntoVector(couldBeMissingTags[i].getMissingTags(), missingTags);
			}
		}
		String[] missingTagsAry = new String[missingTags.size()];
		missingTags.copyInto(missingTagsAry);
		return missingTagsAry;
	}
}
