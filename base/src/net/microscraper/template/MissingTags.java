package net.microscraper.template;

import java.util.Vector;

import net.microscraper.util.VectorUtils;

/**
 * Convenenience abstract class for implementing {@link DependsOnTemplate}.
 * @author talos
 *
 */
public abstract class MissingTags implements DependsOnTemplate {
	final String[] missingTags;
	
	/**
	 * Constructor for a {@link MissingTags} that is not missing tags.
	 */
	protected MissingTags() {
		this.missingTags = new String[] {};
	}
	
	/**
	 * Constructor for a {@link MissingTags} that is missing tags.
	 * @param missingTags
	 */
	public MissingTags(String[] missingTags) {
		this.missingTags = missingTags;
	}
	
	public final boolean isMissingTags() {
		return missingTags.length > 0;
	}

	public final String[] getMissingTags() {
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
			VectorUtils.arrayIntoVector(couldBeMissingTags[i].getMissingTags(), missingTags);
		}
		String[] missingTagsAry = new String[missingTags.size()];
		missingTags.copyInto(missingTagsAry);
		return missingTagsAry;
	}
}
