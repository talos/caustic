package net.microscraper.database;

import java.io.IOException;

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
	 * @throws IOException if {@link Database} cannot be opened.
	 */
	public void open() throws IOException;
	
	/**
	 * 
	 * @return A {@link DatabaseView} to this {@link PersistedDatabase}.
	 * @throws IOException if a {@link DatabaseView} cannot be generated.
	 */
	public abstract DatabaseView newView() throws IOException;

	/**
	 * Open the {@link Database}, modifying tables to close out and closing
	 * any connections.
	 * @throws IOException if {@link Database} cannot be closed.
	 */
	public void close() throws IOException;
}