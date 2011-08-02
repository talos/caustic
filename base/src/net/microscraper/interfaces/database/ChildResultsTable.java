package net.microscraper.interfaces.database;

import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;

/**
 * Interface for a table to hold the children of a single type of {@link Result}.
 * By default includes an ID column for the {@link Result}, a column for
 * the name of its own parent and the ID for it within its own {@link ChildResultsTable}.
 * Each child's value is added with a new column for the child's name, if it doesn't
 * exist already.
 * @author talos
 * @see Database
 *
 */
public interface ChildResultsTable {
	
	/**
	 * 
	 * @return The name of the {@link Result}s which all {@link Result}s in this
	 * table are children of.
	 */
	public String getResultName();
	
	/**
	 * 
	 * @param source The {@link Result} source of the new {@link Result}.
	 * @return The {@link int} ID of the new {@link Result}.
	 * @throws DatabaseException If the row cannot be inserted.
	 */
	public int insert(Result source) throws DatabaseException;
	
	/**
	 * Update a {@link Result} with a new child.
	 * @param result The {@link Result} reference of the row to be updated.
	 * @param childName The {@link String} name of the column to update.
	 * @param childValue The {@link String} value to update the column with.
	 * @throws DatabaseException If the row cannot be replaced.
	 * @see #insertRow(NameValuePair[])
	 */
	public void update(Result result, String childName, String childValue)
			throws DatabaseException;
	
}
