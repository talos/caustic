package net.caustic.database;


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
	 * @throws ConnectionException If there is a problem closing the {@link Connection}.
	 */
	public abstract void open() throws ConnectionException;

	/**
	 * Close the {@link Connection}.
	 * @throws ConnectionException If there is a problem closing the {@link Connection}.
	 */
	public abstract void close() throws ConnectionException;

}