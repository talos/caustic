package net.microscraper.database;

import java.io.IOException;

import net.microscraper.uuid.UUID;

interface PersistedDatabase extends Database {
	
	/**
	 * 
	 * @param scope The {@link UUID} scope to look within.
	 * @param tagName The {@link String} name of the tag value to get.
	 * @return The {@link String} value associated with <code>tagName</code>.
	 * @throws IOException 
	 */
	public String get(UUID scope, String tagName) throws IOException;
	
	/**
	 * Insert a one-to-one result without a value.
	 * @param scope The {@link UUID} scope of the result.
	 * @param resultTableName The name of the table to insert into.
	 * @param name the {@link String} name of the result.
	 * @throws IOException if the operation could not be completed
	 */
	public void insertOneToOne(UUID scope, String name)
			throws IOException;
	
	/**
	 * Insert a one-to-one result with a value.
	 * @param scope The {@link UUID} scope of the result.
	 * @param resultTableName The name of the table to insert into.
	 * @param name the {@link String} name of the result.
	 * @param value the {@link String} value of the result.
	 * @throws IOException if the operation could not be completed
	 */
	public void insertOneToOne(UUID scope, String name, String value)
			throws IOException;
	
	/**
	 * Insert a one-to-many result without a value.
	 * @param source The {@link UUID} scope of the source of the one-to-many.
	 * @param resultTableName The name of the 
	 * @param name
	 * @return
	 * @throws IOException if the operation could not be completed
	 */
	public PersistedDatabaseView insertOneToMany(UUID source, String name)
			throws IOException;
	
	/**
	 * 
	 * @param source
	 * @param resultTableName
	 * @param name
	 * @param value
	 * @return
	 * @throws IOException if the operation could not be completed
	 */
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value)
			throws IOException;

	
}
