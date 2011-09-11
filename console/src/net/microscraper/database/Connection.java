package net.microscraper.database;

import java.io.IOException;

/**
 * A basic interface for connections.
 * @author talos
 * @see #open()
 * @see #close()
 *
 */
public interface Connection {

	/**
	 * Open the {@link Connection}.
	 * @throws IOException If there is a problem opening the {@link Connection}.
	 */
	public abstract void open() throws IOException;

	/**
	 * Close the {@link Connection}.
	 * @throws IOException If there is a problem closing the {@link Connection}.
	 */
	public abstract void close() throws IOException;

}