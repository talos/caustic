package net.microscraper.database;

import java.util.Map;

import net.microscraper.console.UUID;

/**
 * An interface for writing to a table via {@link #insert(Map)}.
 * @author talos
 *
 */
public interface WritableTable {

	/**
	 * Insert a new row into the {@link WritableTable}.
	 * @param id
	 * @param map A {@link Hashtable} mapping columns names to values to insert
	 * into {@link WritableTable}.
	 * @throws TableManipulationException if the row could not be inserted.
	 */
	public abstract void insert(UUID id, Map<String, String> map) throws TableManipulationException;
}