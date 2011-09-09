package net.microscraper.util;

import net.microscraper.template.MissingTags;

public abstract class Result extends MissingTags {
	private final Object success;
	private final String failureWhy;
	
	/**
	 * Protected constructor for a successful {@link Result}.
	 * @param success
	 */
	protected Result(Object success) {
		super();
		this.success = success;
		this.failureWhy = null;
	}

	/**
	 * Protected constructor for a {@link Result} that is missing tags.
	 * @param success
	 */
	protected Result(String[] missingTags) {
		super(missingTags);
		this.success = null;
		this.failureWhy = null;
	}
	
	/**
	 * Protected constructor for a failed {@link Result}.
	 * @param success
	 */
	public Result(String failureWhy) {
		this.success = null;
		this.failureWhy = failureWhy;
	}
	
	public final boolean isSuccess() {
		return success != null;
	}
	
	protected final Object getSuccess() {
		if(isSuccess()) {
			return success;
		} else {
			throw new IllegalStateException("Not a successful result.");
		}
	}
	
	public final String getFailedBecause() {
		if(failureWhy != null) {
			return failureWhy;
		} else {
			throw new IllegalStateException("Not a failed result.");
		}
	}
	
}
