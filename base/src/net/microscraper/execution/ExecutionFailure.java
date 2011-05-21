package net.microscraper.execution;

/**
 * An {@link Exception} that indicates an {@link Execution}
 * has failed.  Its {@link #getCause} is the reason
 * for the failure.
 * @author john
 *
 */
public class ExecutionFailure extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9180369308508105666L;
	private final Exception failure;
	
	/**
	 * 
	 * @param failure The reason for the {@link Execution}'s failure.
	 */
	public ExecutionFailure(Exception failure) {
		super(failure);
		this.failure = failure;
	}
	
	public Throwable getCause() {
		return failure;
	}
}
