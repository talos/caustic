package net.microscraper.database;

import java.io.IOException;

public interface InsertableConnection {
	/**
	 * Obtain a new {@link Insertable} using this {@link InsertableConnection}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link Insertable}.
	 * @return A {@link Insertable}.
	 * @throws IOException if the {@link Insertable} cannot be created.
	 */
	public abstract Insertable getInsertable(String[] textColumns) throws IOException;

}
