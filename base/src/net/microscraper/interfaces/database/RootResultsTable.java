package net.microscraper.interfaces.database;

import net.microscraper.executable.Result;

/**
 * Root table for {@link MultiTableDatabase}.  Should have one row in it.
 * @author talos
 *
 */
public interface RootResultsTable {

	/**
	 * Update {@link RootResultsTable} with a new child.
	 * @param childName The {@link String} name of the column to update.
	 * @param childValue The {@link String} value to update the column with.
	 * @throws DatabaseException If the row cannot be replaced.
	 * @see #insertRow(NameValuePair[])
	 */
	public void update(String childName, String childValue)
			throws DatabaseException;
	
	/**
	 * 
	 * @return The root {@link Result}.
	 */
	public Result getRootResult();
}
