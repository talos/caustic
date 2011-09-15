package net.microscraper.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	 * with {@link #DEFAULT_TABLE_NAME}, and to prepend before column
	 * names to prevent collision with {@link #SCOPE_COLUMN_NAME}.
	 */
	public static final char PREPEND = '_';
	
	/**
	 * Name of {@link #defaultTable}.
	 */
	public static final String DEFAULT_TABLE_NAME = "default";
	
	/**
	 * Name of {@link #relationshipTable}.
	 */
	public static final String RELATIONSHIP_TABLE_NAME = "relationships";

	/**
	 * Column name for the name of the scope, in relationship tables.
	 */
	public static final String NAME_COLUMN_NAME = "name";

	/**
	 * Column name for the name of the source, in relationship tables.
	 */
	public static final String SOURCE_NAME_COLUMN_NAME = "source_name";
	
	/**
	 * Column name for the scope of the source, in relationship tables.
	 */
	public static final String SOURCE_COLUMN_NAME = "source";
	
	/**
	 * Column name for the source join value in a relationship table.
	 */
	public static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Default column names for {@link IOTable}s result table
	 * in {@link MultiTableDatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] { };
	
	/**
	 * Fixed columns for {@link #relationshipTable} in {@link MultiTableDatabase}.
	 */
	public static final String[] RELATIONSHIP_TABLE_COLUMNS = new String[] {
		SOURCE_NAME_COLUMN_NAME,
		NAME_COLUMN_NAME,
		SOURCE_COLUMN_NAME,
		VALUE_COLUMN_NAME
	};
	
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
	 * Make sure <code>columnName</code> doesn't overlap with a predefined column in
	 * {@link RESULT_TABLE_COLUMNS}.
	 * @param tableName The {@link String} possible table name to check.
	 * @return A prepended version of <code>tableName</code> if necessary.
	 */
	private String cleanColumnName(String columnName) {
		for(int i = 0 ; i < RESULT_TABLE_COLUMNS.length ; i ++) {
			if(columnName.equals(RESULT_TABLE_COLUMNS[i])) {
				return PREPEND + columnName;
			}
		}
		return columnName;
	}
	
	/**
	 * Make sure <code>tableName</code> doesn't overlap with the {@link #DEFAULT_TABLE_NAME}.
	 * @param tableName The {@link String} possible table name to check.
	 * @return A prepended version of <code>tableName</code> if necessary.
	 */
	private String cleanTableName(String tableName) {
		return tableName.equals(DEFAULT_TABLE_NAME) ||
				tableName.equals(RELATIONSHIP_TABLE_NAME)
				? PREPEND + tableName : tableName;
	}
	
	/**
	 * Obtain the result table from a {@link UUID} scope.
	 * @param scope
	 * @return The {@link IOTable} results table, or <code>null</code>
	 * if there is none.
	 */
	private IOTable getResultTable(UUID scope) throws DatabaseException {
		// check in result table
		List<String> tableNames = relationshipTable.select(scope, NAME_COLUMN_NAME);
		// we must have a results table
		if(tableNames.size() == 1) {
			return connection.getIOTable(tableNames.get(0));
		} else if(tableNames.size() == 0) {
			return null;
		} else {
			throw new DatabaseException("More than one result table for scope " + scope);
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
		insertMap.put(NAME_COLUMN_NAME, name);
		if(optSourceName != null) {
			insertMap.put(SOURCE_NAME_COLUMN_NAME, optSourceName);
		}
		if(optSource != null) {
			insertMap.put(SOURCE_COLUMN_NAME, optSource.asString());
		}
		if(optValue != null) {
			insertMap.put(VALUE_COLUMN_NAME, optValue);
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
		relationshipTable = connection.newIOTable(RELATIONSHIP_TABLE_NAME, RELATIONSHIP_TABLE_COLUMNS);
		
		// create default table
		connection.newIOTable(DEFAULT_TABLE_NAME, RESULT_TABLE_COLUMNS);
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
		addLink(scope, null, null, DEFAULT_TABLE_NAME, null);
		
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
		
		try {
			IOTable resultTable = getResultTable(scope);
			
			if(resultTable != null) {
				if(resultTable.hasColumn(columnName)) { // has the column
					List<String> values = resultTable.select(scope, columnName);
					if(values.size() == 1 && values.get(0) != null) {
						return values.get(0);
					} else if(values.size() > 1) {
						throw new DatabaseReadException("Should not have stored multiple values for a scope ID");
					}
				}
			}
		} catch(DatabaseException e) {
			throw new DatabaseReadException(e.getMessage());
		}
		
		// not there, check source tables
		List<Map<String, String>> links = relationshipTable.select(scope,
				new String[] { NAME_COLUMN_NAME, SOURCE_COLUMN_NAME, VALUE_COLUMN_NAME } );
		if(links.size() == 1) {
			Map<String, String> link = links.get(0);
			// check the link itself for the value.
			
			if(link.get(NAME_COLUMN_NAME).equals(columnName)) {
				String linkValue = link.get(VALUE_COLUMN_NAME);
				if(linkValue != null) {
					return link.get(VALUE_COLUMN_NAME);
				}
			}
			
			// not in the link itself, check the source table.
			if(link.get(SOURCE_COLUMN_NAME) != null) {
				return get(new DeserializedUUID(link.get(SOURCE_COLUMN_NAME)), columnName);
			} else {
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
			
			table.update(scope, updateMap);
		} catch(DatabaseException e) {
			throw new DatabasePersistException(e.getMessage());
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
		
		try {
			// add to relationship table
			String sourceName = relationshipTable.select(source, NAME_COLUMN_NAME).get(0);
			addLink(scope, source, sourceName, tableName, value);
			
			// create new result table, insert row
			IOTable table = connection.getIOTable(tableName);
			if(table == null) {
				table = connection.newIOTable(tableName, RESULT_TABLE_COLUMNS);
			}
			Map<String, String> emptyMap = Collections.emptyMap();
			table.insert(scope, emptyMap);
			
			// update result table
			insertOneToOne(scope, name, value);
			
			return new PersistedDatabaseView(this, scope);
		} catch(ConnectionException e) {
			throw new DatabasePersistException(e.getMessage());
		} catch(DatabaseException e) {
			throw new DatabasePersistException(e.getMessage());
		}
	}
}
