package net.caustic.template;

import java.util.Vector;

import net.caustic.util.VectorUtils;

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
	 * Does not include duplicates.
	 */
	public static String[] combine(DependsOnTemplate[] couldBeMissingTags) {
		Vector missingTags = new Vector();
		for(int i = 0 ; i < couldBeMissingTags.length ; i ++) {
			if(couldBeMissingTags[i].isMissingTags()) {
				String[] elemMissingTags = couldBeMissingTags[i].getMissingTags();
				// ensure we don't insert duplicate tags
				for(int j = 0 ; j < elemMissingTags.length ; j ++) {
					String missingTag = elemMissingTags[j];
					if(!missingTags.contains(missingTag)) {
						missingTags.addElement(missingTag);
					}
				}
			}
		}
		String[] missingTagsAry = new String[missingTags.size()];
		missingTags.copyInto(missingTagsAry);
		return missingTagsAry;
	}
	
	/**
	 * 
	 * @param couldBeMissingTags1
	 * @param couldBeMissingTags2
	 * @return <code>true</code> if both parameters are missing the same tag names (in any order),
	 * <code>false</code> otherwise.
	 */
	public static boolean isMissingSameTags(DependsOnTemplate couldBeMissingTags1,
					DependsOnTemplate couldBeMissingTags2) {
		if(couldBeMissingTags1.isMissingTags() && couldBeMissingTags2.isMissingTags()) {
			String[] missingTags1 = couldBeMissingTags1.getMissingTags();
			String[] missingTags2 = couldBeMissingTags2.getMissingTags();
			if(missingTags1.length == missingTags2.length) { // only bother testing if the same length
				Vector curVector = VectorUtils.arrayIntoVector(missingTags1, new Vector());
				Vector lastVector = VectorUtils.arrayIntoVector(missingTags2, new Vector());
				return VectorUtils.haveSameElements(curVector, lastVector);
			}
		}
		return false;
	}
}
