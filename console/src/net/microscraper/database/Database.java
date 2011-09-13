package net.microscraper.database;

/**
 * Implementations of the {@link Database} interface provide a method
 * to get new {@link DatabaseView}.
 * @author talos
 * @see #newView()
 *
 */
public interface Database {

	/**
	 * Open the {@link Database}, creating any necessary tables and opening
	 * any connections.
	 * @throws DatabaseException if {@link Database} cannot be opened.
	 * @throws ConnectionException if {@link Database}'s {@link Connection}
	 * cannot be opened.
	 */
	public void open() throws DatabaseException, ConnectionException;
	
	/**
	 * 
	 * @return A {@link DatabaseView} to this {@link PersistedDatabase}.
	 * @throws DatabaseException if a {@link DatabaseView} cannot be generated.
	 */
	public abstract DatabaseView newView() throws DatabaseException;

	/**
	 * Open the {@link Database}, modifying tables to close out and closing
	 * any connections.
	 * @throws DatabaseException if {@link Database} cannot be closed.
	 * @throws ConnectionException if {@link Database}'s {@link Connection}
	 * cannot be closed.
	 */
	public void close() throws DatabaseException, ConnectionException;
}