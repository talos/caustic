package net.microscraper.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.util.StringUtils;
import net.microscraper.uuid.DeserializedUUID;
import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

/**
 * An implementation of {@link PersistedDatabase} whose subclasses store
 * {@link ScraperResult}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public final class MultiTableDatabase implements PersistedDatabase {
	
	/**
	 * String to prepend before table names to prevent collision
	 * with {@link #DEFAULT_TABLE}, and to prepend before column
	 * names to prevent collision with {@link #SCOPE_COLUMN_NAME}.
	 */
	public static final char PREPEND = '_';
	
	/**
	 * Name of {@link #defaultTable}.
	 */
	public static final String DEFAULT_TABLE = "default";
	
	/**
	 * Name of {@link #relationshipTable}.
	 */
	public static final String RELATIONSHIP_TABLE = "relationships";

	/**
	 * Column name for the result table name of the scope, in relationship tables.
	 * Cannot be null.
	 */
	public static final String RESULT_TABLE = "name";

	/**
	 * Column name for the result table name of the source, in relationship tables.
	 * Only null for default scope.
	 */
	public static final String SOURCE_RESULT_TABLE = "source_name";
	
	/**
	 * Column name for the scope of the source, in relationship tables.
	 * Only null for default scope.
	 */
	public static final String SOURCE_SCOPE = "source";
	
	/**
	 * Column name for the source join value in a relationship table.  Can be
	 * <code>null</code>.
	 */
	public static final String VALUE = "value";
	
	/**
	 * Default column names for {@link IOTable}s result table
	 * in {@link MultiTableDatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] { };
	
	/**
	 * Fixed columns for {@link #relationshipTable} in {@link MultiTableDatabase}.
	 */
	public static final String[] RELATIONSHIP_TABLE_COLUMNS = new String[] {
		SOURCE_SCOPE,
		SOURCE_RESULT_TABLE,
		RESULT_TABLE,
		VALUE
	};
	
	/**
	 * A table mapping scopes to their result table names.  For non default scopes,
	 * this also includes their source scope, scope source name, and (optionally) the
	 * value that joins them.
	 */
	private IOTable relationshipTable;
	
	/**
	 * A {@link IOConnection} to use when generating tables.
	 */
	private final IOConnection connection;
	
	private final UUIDFactory idFactory;
	
	/**
	 * Whether {@link #open()} has been called.
	 */
	private boolean isOpen = false;

	/**
	 */
	private String cleanColumnName(String columnName) {
		// TODO
		return columnName;
	}
	
	/**
	 * Make sure <code>tableName</code> doesn't overlap with the {@link #DEFAULT_TABLE}.
	 * @param tableName The {@link String} possible table name to check.
	 * @return A prepended version of <code>tableName</code> if necessary.
	 */
	private String cleanTableName(String tableName) {
		return tableName.equals(DEFAULT_TABLE) ||
				tableName.equals(RELATIONSHIP_TABLE)
				? PREPEND + tableName : tableName;
	}
	
	/**
	 * Obtain the result table from a {@link UUID} scope.
	 * @param scope
	 * @return The {@link IOTable} results table, or <code>null</code>
	 * if there is none.
	 */
	private IOTable getResultTable(UUID scope) throws DatabaseReadException {
		
		// select only based off of scope
		Map<String, String> whereMap = Collections.emptyMap();
		List<Map<String, String>> tableNames = relationshipTable.select(scope,
				whereMap, new String[] { RESULT_TABLE });
		
		// we must have a results table
		if(tableNames.size() == 1) {
			try {
				return connection.getIOTable(tableNames.get(0).get(RESULT_TABLE));
			} catch(ConnectionException e) {
				throw new DatabaseReadException("Error reading from connection", e);
			}
		} else if(tableNames.size() == 0) {
			throw new DatabaseReadException("Missing result table for " + scope);
		} else {
			throw new DatabaseReadException("More than one result table for scope " + scope);
		}
	}
	
	/**
	 * Insert a row into {@link #relationshipTable}.
	 * @param scope
	 * @param optSource
	 * @param name
	 * @param optValue
	 */
	private void addLink(UUID scope, UUID optSource, String optSourceName, String name, String optValue)
				throws TableManipulationException {
		Map<String, String> insertMap = new HashMap<String, String>();
		insertMap.put(RESULT_TABLE, name);
		if(optSourceName != null) {
			insertMap.put(SOURCE_RESULT_TABLE, optSourceName);
		}
		if(optSource != null) {
			insertMap.put(SOURCE_SCOPE, optSource.asString());
		}
		if(optValue != null) {
			insertMap.put(VALUE, optValue);
		}
		relationshipTable.insert(scope, insertMap);
	}
	
	/**
	 * 
	 * @throws IllegalStateException If {@link MultiTableDatabase} has not been opened.
	 */
	private void ensureOpen() throws IllegalStateException {
		if(isOpen == false) {
			throw new IllegalStateException("Must open database before using it.");
		}
	}
	
	
	public MultiTableDatabase(IOConnection connection, UUIDFactory idFactory) {
		this.idFactory = idFactory;
		this.connection = connection;
	}
	
	/**
	 * Opens {@link #connection} and {@link #backingDatabase}, and creates the {@link #defaultTable}.
	 */
	public void open() throws DatabaseException {
		connection.open();
		relationshipTable = connection.newIOTable(RELATIONSHIP_TABLE, RELATIONSHIP_TABLE_COLUMNS);
		
		// create default table
		connection.newIOTable(DEFAULT_TABLE, RESULT_TABLE_COLUMNS);
		isOpen = true;
	}

	/**
	 * Closes {@link #connection} and {@link #backingDatabase}.
	 */
	public void close() throws DatabaseException {
		connection.close();
	}
	
	@Override
	public DatabaseView newView() throws DatabaseException {
		ensureOpen();
		UUID scope = idFactory.get();
		// insert the fact that this is a default scope into joinTable
		addLink(scope, null, null, DEFAULT_TABLE, null);
		
		// insert blank row into default table
		IOTable defaultTable = getResultTable(scope);
		Map<String, String> emptyMap = Collections.emptyMap();
		defaultTable.insert(scope, emptyMap);
		
		return new PersistedDatabaseView(this, scope);
	}

	/**
	 * Retrieve a particular value from scope and name.
	 * @param id The {@link UUID} scope of the value to retrieve.
	 * @param columnName The {@link String} name of the value.
	 * @return The {@link String} value, or <code>null</code> if it does not exist.
	 */
	@Override
	public String get(UUID scope, String tagName) throws DatabaseReadException {
		ensureOpen();
		
		String columnName = cleanColumnName(tagName);
		
		IOTable resultTable = getResultTable(scope);
		
		if(resultTable == null) {
			throw new DatabaseReadException("No result table created for " + tagName + " at " + scope);
		}
		
		if(resultTable.hasColumn(columnName)) { // has the column
				
			// there should be only one row per scope in results tables.
			Map<String, String> whereMap = Collections.emptyMap();
			List<Map<String, String>> values =
					resultTable.select(scope, whereMap, new String[] { columnName });
			if(values.size() == 1 && values.get(0) != null) {
				return values.get(0).get(columnName);
			} else if(values.size() > 1) {
				throw new DatabaseReadException("Should not have stored multiple values for a scope ID");
			}
		}
		
		// not there, check source tables
		Map<String, String> whereMap = Collections.emptyMap();
		List<Map<String, String>> links = relationshipTable.select(scope,
				whereMap, new String[] { RESULT_TABLE, SOURCE_SCOPE, VALUE } );
		if(links.size() == 1) { // should only be one entry in relationships table per scope
			Map<String, String> link = links.get(0);
			// check the link itself for the value.
			if(link.get(RESULT_TABLE).equals(columnName)) {
				String linkValue = link.get(VALUE);
				if(linkValue != null) {
					return link.get(VALUE);
				}
			}
			
			// not in the link itself, check the source table.
			String sourceScope = link.get(SOURCE_SCOPE);
			if(sourceScope != null) {
				// loop back using sourceScope
				return get(new DeserializedUUID(sourceScope), columnName);
			} else {
				// we're already at a default scope, this columnName doesn't exist.
				return null;
			}
		} else if(links.size() == 0) {
			throw new DatabaseReadException("Missing source in relationships table for scope " + scope);
		} else {
			throw new DatabaseReadException("Multiple sources in relationships table for scope " + scope);
		}
	}
	
	@Override
	public void insertOneToOne(UUID id, String name)
			throws DatabasePersistException {
		insertOneToOne(id, name, null);
	}

	@Override
	public void insertOneToOne(UUID scope, String name, String optValue)
			throws DatabasePersistException {
		try {
			IOTable table = getResultTable(scope);
			String columnName = cleanColumnName(name);
			
			// add column if it doesn't exist in table yet. luxury!
			if(!table.hasColumn(columnName)) {
				table.addColumn(columnName);
			}

			Map<String, String> updateMap = new HashMap<String, String>();
			updateMap.put(columnName, optValue);
			
			// only one row in result tables per scope, don't need to filter by where.
			Map<String, String> whereMap = Collections.emptyMap();
			table.update(scope, whereMap, updateMap);
		} catch(TableManipulationException e) {
			throw new DatabasePersistException("Could not add column for new value name.", e);
		} catch(DatabaseReadException e) {
			throw new DatabasePersistException("Could not get result table.", e);
		}
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name) throws DatabasePersistException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value)
			throws DatabasePersistException {
		UUID scope = idFactory.get();
		String tableName = cleanTableName(name);
		
		// add to relationship table
		try {
			Map<String, String> whereMap = Collections.emptyMap();
			List<Map<String, String>> results = relationshipTable
					.select(source, whereMap, new String[] { RESULT_TABLE });
			if(results.size() == 1) {
				String sourceName = results.get(0).get(RESULT_TABLE);
				addLink(scope, source, sourceName, tableName, value);
			} else if(results.size() > 1) {
				throw new DatabasePersistException("Multiple entries for " +
								StringUtils.quote(name) + ": " + source +
								" in relationships table.");
			} else {
				throw new DatabasePersistException("No entries for " +
								StringUtils.quote(name) + ": " + source +
								" in relationships table.");				
			}
		} catch(DatabaseReadException e) {
			throw new DatabasePersistException("Error reading from relationships table", e);
		}
		
		// create new result table, insert row
		try {
			IOTable table = connection.getIOTable(tableName);
			if(table == null) {
				table = connection.newIOTable(tableName, RESULT_TABLE_COLUMNS);
			}
			
			Map<String, String> emptyMap = Collections.emptyMap();
			table.insert(scope, emptyMap);
		} catch(ConnectionException e) {
			throw new DatabasePersistException("Could not create result table " +
					StringUtils.quote(tableName), e);
		}
		
		// update result table
		insertOneToOne(scope, name, value);
		
		return new PersistedDatabaseView(this, scope);

	}
}
