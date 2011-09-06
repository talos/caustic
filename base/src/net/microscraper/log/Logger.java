package net.microscraper.log;

import java.io.IOException;

/**
 * A basic interface for logging text and {@link Throwable}s.
 * @author talos
 * @see #open()
 * @see #close()
 * @see #i(String)
 * @see #w(Throwable)
 * @see #e(Throwable)
 *
 */
public interface Logger {
	
	/**
	 * Open the {@link Logger} for logging.
	 * @throws IOException if the {@link Logger} cannot be opened.
	 */
	public abstract void open() throws IOException;
	
	/**
	 * Close up the {@link Logger}.
	 * @throws IOException if the {@link Logger} cannot be closed.
	 */
	public abstract void close() throws IOException;
	
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