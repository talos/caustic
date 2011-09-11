package net.microscraper.database;

import java.io.IOException;
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
	private IOTable getResultTable(UUID scope) throws IOException {
		// check in result table
		List<String> tableNames = relationshipTable.select(scope, NAME_COLUMN_NAME);
		// we must have a results table
		if(tableNames.size() == 1) {
			return connection.getIOTable(tableNames.get(0));
		} else if(tableNames.size() == 0) {
			return null;
		} else {
			throw new IOException("More than one result table for scope " + scope);
		}
	}
	
	/**
	 * Insert a row into {@link #relationshipTable}.
	 * @param scope
	 * @param optSource
	 * @param name
	 * @param optValue
	 */
	private void addLink(UUID scope, UUID optSource, String name, String optValue)
				throws TableManipulationException{
		Map<String, String> insertMap = new HashMap<String, String>();
		insertMap.put(NAME_COLUMN_NAME, DEFAULT_TABLE_NAME);
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
	public void open() throws IOException {
		connection.open();
		relationshipTable = connection.newIOTable(RELATIONSHIP_TABLE_NAME, RELATIONSHIP_TABLE_COLUMNS);
		
		// create default table
		connection.newIOTable(DEFAULT_TABLE_NAME, RESULT_TABLE_COLUMNS);
		isOpen = true;
	}

	/**
	 * Closes {@link #connection} and {@link #backingDatabase}.
	 */
	public void close() throws IOException {
		connection.close();
	}
	
	@Override
	public DatabaseView newView() throws TableManipulationException {
		ensureOpen();
		UUID scope = idFactory.get();
		// insert the fact that this is a default scope into joinTable
		addLink(scope, null, DEFAULT_TABLE_NAME, null);
		
		return new PersistedDatabaseView(this, scope);
	}

	/**
	 * Retrieve a particular value from scope and name.
	 * @param id The {@link UUID} scope of the value to retrieve.
	 * @param name The {@link String} name of the value.
	 * @return The {@link String} value, or <code>null</code> if it does not exist.
	 */
	@Override
	public String get(UUID scope, String name) throws IOException {
		ensureOpen();
		
		IOTable resultTable = getResultTable(scope);
		
		if(resultTable != null) {
			if(resultTable.hasColumn(name)) { // has the column
				List<String> values = resultTable.select(scope, name);
				if(values.size() == 1 && values.get(0) != null) {
					return values.get(0);
				} else if(values.size() > 1) {
					throw new IOException("Should not store multiple values for a scope ID");
				}
			}
		}
		
		// not there, check source tables
		List<String> sources = relationshipTable.select(scope, SOURCE_COLUMN_NAME);
		if(sources.size() == 1) {
			if(sources.get(0) == null) { // already at highest level
				return null;
			} else {
				return get(new DeserializedUUID(sources.get(0)), name);
			}
		} else if(sources.size() == 0) {
			throw new IOException("Missing entry in join table for scope " + scope);
		} else {
			throw new IOException("Multiple entries in join table for scope " + scope);
		}
		
	}
	
	@Override
	public void insertOneToOne(UUID id, String name)
			throws TableManipulationException {
		// no-op
	}

	@Override
	public void insertOneToOne(UUID id, String name, String value)
			throws IOException {
		IOTable table = getResultTable(id);
		
		Map<String, String> updateMap = new HashMap<String, String>();
		updateMap.put(cleanColumnName(name), value);
		table.update(id, updateMap);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name) throws IOException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value) throws IOException {
		UUID scope = idFactory.get();
		
		// add to relationship table
		addLink(scope, source, name, value);
		
		// create new result table
		connection.newIOTable(name, RESULT_TABLE_COLUMNS);
		
		// insert into result table
		insertOneToOne(scope, name, value);
		
		return new PersistedDatabaseView(this, scope);
	}

}
