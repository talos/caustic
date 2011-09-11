package net.microscraper.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

/**
 * An implementation of {@link Database} whose subclasses store
 * {@link ScraperResult}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public final class MultiTableIODatabase implements IODatabase {
	
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
	 * Column name for the scope of the source, in join tables.
	 */
	public static final String SOURCE_COLUMN_NAME = "source";
	
	/**
	 * Column name for the source join value in a join table.
	 */
	public static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Default column names for {@link IOTable}s result table
	 * in {@link MultiTableIODatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] { };
	
	/**
	 * Fixed columns for {@link WritableTable} join table in {@link MultiTableIODatabase}.
	 */
	public static final String[] JOIN_TABLE_COLUMNS = new String[] {
		SOURCE_COLUMN_NAME,
		VALUE_COLUMN_NAME
	};

	/**
	 * Mapping of names to result tables.
	 */
	private final Map<String, IOTable> resultTables = new HashMap<String, IOTable>();

	/**
	 * Mapping of names to join tables.
	 */
	private final Map<String, IOTable> joinTables = new HashMap<String, IOTable>();

	/**
	 * Mapping of names to source table names.
	 */
	private final Map<String, String> sourceTableNames = new HashMap<String, String>();
	
	/**
	 * A {@link IOConnection} to use when generating tables.
	 */
	private final IOConnection connection;
	
	/**
	 * The default result table.
	 */
	private IOTable defaultTable;
	
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
		return tableName.equals(DEFAULT_TABLE_NAME) ? PREPEND + tableName : tableName;
	}
	
	/**
	 * 
	 * @throws IllegalStateException If {@link MultiTableIODatabase} has not been opened.
	 */
	private void ensureOpen() throws IllegalStateException {
		if(isOpen == false) {
			throw new IllegalStateException("Must open database before using it.");
		}
	}
	
	
	public MultiTableIODatabase(IOConnection connection, UUIDFactory idFactory) {
		this.idFactory = idFactory;
		this.connection = connection;
	}
	
	/**
	 * Opens {@link #connection} and {@link #backingDatabase}, and creates the {@link #defaultTable}.
	 */
	public void open() throws IOException {
		connection.open();
		defaultTable = connection.newIOTable(DEFAULT_TABLE_NAME, RESULT_TABLE_COLUMNS);
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
		UUID id = idFactory.get();
		Map<String, String> map = new HashMap<String, String>();
		defaultTable.insert(id, map);

		return new IODatabaseView(this, DEFAULT_TABLE_NAME, idFactory.get());
	}

	private String get(String viewName, String id, String name) {
		ensureOpen();
		List<String> result;
		
		IOTable table = resultTables.get(cleanTableName(viewName));
		result = table.select(id, name);
		if(result.size() == 0) {
			IOTable joinTable = joinTables.get(cleanTableName(viewName));
			if(joinTable == null) {
				result = null;
			} else {
				String sourceTableName = sourceTableNames.get(cleanTableName(viewName));
				String sourceID = joinTable.select(id, VALUE_COLUMN_NAME).get(0);
				return get(sourceTableName, sourceID, name);
			}
		}
		return result.get(0);
	}
	
	@Override
	public String get(String viewName, UUID id, String name) {
		ensureOpen();
		return get(viewName, id.asString(), name);
	}

	@Override
	public void insertOneToOne(UUID id, String viewName, String name)
			throws TableManipulationException {
		// no-op
	}

	@Override
	public void insertOneToOne(UUID id, String viewName, String name,
			String value) throws TableManipulationException {
		IOTable table = resultTables.get(viewName);
		Map<String, String> updateMap = new HashMap<String, String>();
		updateMap.put(cleanColumnName(name), value);
		table.update(id, updateMap);
	}

	@Override
	public DatabaseView insertOneToMany(UUID source, String viewName,
			String name) throws IOException {
		return insertOneToMany(source, viewName, name, null);
	}

	@Override
	public DatabaseView insertOneToMany(UUID source, String viewName,
			String name, String value) throws IOException {
		UUID id = idFactory.get();
		
		String resultName = cleanTableName(name);
		String joinName = cleanTableName(viewName + PREPEND + resultName);
		
		// Link to source table name in sourceTableNames
		sourceTableNames.put(resultName, resultName);
		
		// Obtain the table for results
		IOTable resultTable;
		if(!resultTables.containsKey(resultName)) {
			resultTable = connection.newIOTable(resultName, RESULT_TABLE_COLUMNS);
			resultTables.put(resultName, resultTable);
		} else {
			resultTable = resultTables.get(resultName);
		}
		
		// Obtain the table for joins
		IOTable joinTable;
		if(!joinTables.containsKey(joinName)) {
			joinTable = connection.newIOTable(joinName, JOIN_TABLE_COLUMNS);
			joinTables.put(joinName, joinTable);
		} else {
			joinTable = joinTables.get(joinName);
		}
		
		// Insert new value into joinTable
		Map<String, String> joinInsert = new HashMap<String, String>();
		joinInsert.put(SOURCE_COLUMN_NAME, source.asString());
		if(value != null) {
			joinInsert.put(VALUE_COLUMN_NAME, value);
		}
		joinTable.insert(id, joinInsert);
		
		return new IODatabaseView(this, name, id);
	}

}
