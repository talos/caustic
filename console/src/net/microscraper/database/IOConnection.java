package net.microscraper.database;

import java.io.IOException;

/**
 * @author talos
 *
 */
public interface IOConnection {

	/**
	 * Obtain a new {@link IOTable} using this {@link IOConnection}.
	 * @param name The {@link String} name of the new {@link IOTable}.
	 * @param textColumns An array of {@link String} columns to include in this 
	 * {@link IOTable}.
	 * @return A {@link IOTable}.
	 * @throws IOException if the {@link IOTable} cannot be created.
	 */
	public abstract IOTable newUpdateable(String name, String[] textColumns)
			throws IOException;

}