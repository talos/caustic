package net.microscraper.database;

import java.io.IOException;

public interface IOConnection extends WritableConnection {

	/**
	 * Obtain a new {@link IOTable} using this {@link IOConnection}.
	 * @param name The {@link String} name of the new {@link IOTable}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link IOTable}.
	 * @return A {@link IOTable}.
	 * @throws IOException if the {@link IOTable} cannot be created.
	 */
	public abstract IOTable getIOTable(String name, String[] textColumns)
			throws IOException;

	/**
	 * Open the {@link IOConnection}.
	 * @throws IOException If there is a problem opening the {@link IOConnection}.
	 */
	public abstract void open() throws IOException;

	/**
	 * Close the {@link IOConnection}.
	 * @throws IOException If there is a problem closing the {@link IOConnection}.
	 */
	public abstract void close() throws IOException;
}