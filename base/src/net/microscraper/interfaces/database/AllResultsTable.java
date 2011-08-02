package net.microscraper.interfaces.database;

import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;

/**
 * Interface for a table to hold all {@link Result}s.
 * @author talos
 * @see Database
 *
 */
public interface AllResultsTable {
	
	/**
	 * 
	 * @param name The {@link String} name of the {@link Result} to insert.
	 * @param value The {@link String} value of the {@link Result} to insert.
	 * @return The {@link Result} reference to the newly inserted row.
	 * @throws DatabaseException If the row cannot be inserted.
	 */
	public Result insert(String name, String value) throws DatabaseException;
	
	/**
	 * 
	 * @param source The {@link Result} source of the {@link Result} to insert.
	 * @param name The {@link String} name of the {@link Result} to insert.
	 * @param value The {@link String} value of the {@link Result} to insert.
	 * @return The {@link Result} reference to the newly inserted row.
	 * @throws DatabaseException If the row cannot be inserted.
	 */
	public Result insert(Result source, String name, String value) throws DatabaseException;
}
