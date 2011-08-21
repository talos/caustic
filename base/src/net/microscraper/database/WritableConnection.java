package net.microscraper.database;

import java.io.IOException;

public interface WritableConnection {
	/**
	 * Obtain a new {@link WritableTable} using this {@link WritableConnection}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link WritableTable}.
	 * @return A {@link WritableTable}.
	 * @throws IOException if the {@link WritableTable} cannot be created.
	 */
	public abstract WritableTable getWritableTable(String[] textColumns) throws IOException;

}
