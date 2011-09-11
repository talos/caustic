package net.microscraper.database;

import java.io.IOException;

/**
 * A {@link Connection} that can obtain {@link IOTable}s.
 * @author talos
 * @see #newIOTable(String, String[])
 *
 */
public interface IOConnection extends Connection {

	/**
	 * Obtain a new {@link IOTable} using this {@link IOConnection}.
	 * @param name The {@link String} name of the new {@link IOTable}.
	 * @param columnNames An array of {@link String} columns to include in this 
	 * {@link IOTable}.
	 * @return A {@link IOTable}.
	 * @throws IOException if the {@link IOTable} cannot be created.
	 */
	public abstract IOTable newIOTable(String name, String[] columnNames)
			throws IOException;
	
	public abstract IOTable getIOTable(String name) throws IOException;
}