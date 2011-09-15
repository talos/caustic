package net.microscraper.util;

import net.microscraper.template.DependsOnTemplate;

/**
 * Generic interface for tasks that can be missing tags from
 * a template, successful, or have failed for some reason.
 * @author talos
 *
 */
public interface Result extends DependsOnTemplate {
	
	/**
	 * 
	 * @return A {@link String} explaining why the {@link Result}
	 * failed.  <code>null</code> if it did not fail.
	 */
	public String getFailedBecause();
}
