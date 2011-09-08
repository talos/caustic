package net.microscraper.database;

import java.io.IOException;

/**
 * A connection interface to create {@link Insertable}s.
 * @author talos
 *
 */
public interface InsertableConnection {
	/**
	 * Obtain a new {@link Insertable} using this {@link InsertableConnection}.
	 * @param name The {@link String} name of the {@link Insertable}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link Insertable}.
	 * @return A {@link Insertable}.
	 * @throws IOException if the {@link Insertable} cannot be created.
	 */
	public abstract Insertable newInsertable(String name, String[] textColumns) throws IOException;
	
	/**
	 * Open the {@link UpdateableConnection}.
	 * @throws IOException If there is a problem opening the {@link UpdateableConnection}.
	 */
	public abstract void open() throws IOException;

	/**
	 * Close the {@link UpdateableConnection}.
	 * @throws IOException If there is a problem closing the {@link UpdateableConnection}.
	 */
	public abstract void close() throws IOException;

}
