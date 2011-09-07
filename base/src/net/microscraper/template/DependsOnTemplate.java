package net.microscraper.template;

/**
 * Interface for classes dependent upon a template.
 * @author talos
 *
 */
public interface DependsOnTemplate {
	
	/**
	 * 
	 * @return <code>True</code> if this {@link DependsOnTemplate} is missing
	 * tags, <code>false</code> otherwise.
	 */
	public boolean isMissingTags();
	
	/**
	 * 
	 * @return A {@link String} array of missing tag names.
	 */
	public String[] getMissingTags();
	
}
