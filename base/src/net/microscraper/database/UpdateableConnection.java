package net.microscraper.database;

import java.io.IOException;

public interface UpdateableConnection extends InsertableConnection {

	/**
	 * Obtain a new {@link Updateable} using this {@link UpdateableConnection}.
	 * @param name The {@link String} name of the new {@link Updateable}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link Updateable}.
	 * @return A {@link Updateable}.
	 * @throws IOException if the {@link Updateable} cannot be created.
	 */
	public abstract Updateable getIOTable(String name, String[] textColumns)
			throws IOException;

}