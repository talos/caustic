package net.microscraper.instruction;

import net.microscraper.template.MissingTags;

/**
 * An {@link ActionResult} is created by {@link Action#execute(String, net.microscraper.database.Scope).
 * If successful, it holds an array of {@link String}s accessible through {@link #getResults()}.
 * @author talos
 *
 */
public class ActionResult extends MissingTags {
	
	private final String[] results;
	private final String failureWhy;
	
	private ActionResult(String[] missingTags) {
		super(missingTags);
		this.results = null;
		this.failureWhy = null;
	}
	
	private ActionResult(String[] results, String failureWhy) {
		this.results = results;
		this.failureWhy = failureWhy;
	}
	
	/**
	 * 
	 * @param success The {@link String} array of successful {@link Action} results.
	 * @return A {@link ActionResult} with a successful result.
	 */
	public static ActionResult newSuccess(String[] success) {
		return new ActionResult(success, null);
	}
	
	/**
	 * 
	 * @param missingTags An array of {@link String} missing tags.
	 * @return A {@link ActionResult} with missing tags.
	 */
	public static ActionResult newMissingTags(String[] missingTags) {
		return new ActionResult(missingTags);
	}
	
	/**
	 * 
	 * @param why A {@link String} explaining why an {@link Action} failed.
	 * @return An {@link ActionResult} with a failure explanation.
	 */
	public static ActionResult newFailure(String why) {
		return new ActionResult(null, why);
	}
	
	/**
	 * 
	 * @return The {@link String} array resulting from a successful {@link Action}.
	 */
	public String[] getResults() {
		if(results == null) {
			throw new IllegalStateException("This was not a successful action.");
		} else {
			return results;
		}
	}
	
	/**
	 * 
	 * @return <code>true</code> if the {@link Action} failed, <code>false</code> otherwise.
	 */
	public boolean isFailure() {
		return failureWhy != null;
	}
	
	/**
	 * 
	 * @return The {@link String} explaining why an {@link Action} failed.
	 */
	public String failedBecause() {
		if(failureWhy == null) {
			throw new IllegalStateException("This was not a failed action.");
		} else {
			return failureWhy;
		}
	}
}
