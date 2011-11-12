package net.caustic.database;

import net.caustic.scope.Scope;

/**
 * 
 * @author talos
 *
 */
interface PersistedDatabase extends Database {
	
	/**
	 * 
	 * @param scope The {@link Scope} scope to look within.
	 * @param tagName The {@link String} name of the tag value to get.
	 * @return The {@link String} value associated with <code>tagName</code>.
	 * @throws DatabaseReadException 
	 */
	public String get(Scope scope, String tagName) throws DatabaseReadException;
	
	/**
	 * Insert a one-to-one result without a value.
	 * @param scope The {@link Scope} scope of the result.
	 * @param resultTableName The name of the table to insert into.
	 * @param name the {@link String} name of the result.
	 * @throws DatabasePersistException if the operation could not be completed
	 */
	public void insertOneToOne(Scope scope, String name)
			throws DatabasePersistException;
	
	/**
	 * Insert a one-to-one result with a value.
	 * @param scope The {@link Scope} scope of the result.
	 * @param resultTableName The name of the table to insert into.
	 * @param name the {@link String} name of the result.
	 * @param value the {@link String} value of the result.
	 * @throws DatabasePersistException if the operation could not be completed
	 */
	public void insertOneToOne(Scope scope, String name, String value)
			throws DatabasePersistException;
	
	/**
	 * Insert a one-to-many result without a value.
	 * @param source The {@link Scope} scope of the source of the one-to-many.
	 * @param resultTableName The name of the 
	 * @param name
	 * @return
	 * @throws DatabasePersistException if the operation could not be completed
	 */
	public PersistedDatabaseView insertOneToMany(Scope source, String name)
			throws DatabasePersistException;
	
	/**
	 * 
	 * @param source
	 * @param resultTableName
	 * @param name
	 * @param value
	 * @return
	 * @throws DatabasePersistException if the operation could not be completed
	 */
	public PersistedDatabaseView insertOneToMany(Scope source, String name, String value)
			throws DatabasePersistException;

	
}
