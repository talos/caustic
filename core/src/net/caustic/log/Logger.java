package net.caustic.log;

/**
 * A basic interface for logging text and {@link Throwable}s.
 * @author talos
 * @see #i(String)
 * @see #w(Throwable)
 * @see #e(Throwable)
 *
 */
public interface Logger {
	
	/**
	 * 
	 * Log a {@link Throwable} as an error.
	 * @param e The {@link Throwable} to log.
	 * @throws IllegalStateException if the {@link Logger} is not open,
	 * or has already been closed.
	 * @see #open()
	 * @see #close()
	 */
	public abstract void e(Throwable e) throws IllegalStateException;
	
	/**
	 * 
	 * Log a {@link String}.
	 * @param infoText The {@link String} to log.
	 * @throws IllegalStateException if the {@link Logger} is not open,
	 * or has already been closed.
	 * @see #open()
	 * @see #close()
	 */
	public abstract void i(String infoText) throws IllegalStateException;
	
}