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
	private static final Map<String, String> emptyMap = Collections.emptyMap();
	
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
	 * Name of {@link #links}.
	 */
	public static final String LINK_TABLE = "links";

	/**
	 * Column name for the result table name of the scope, in {@link #links}.
	 * Cannot be null.
	 */
	public static final String RESULT_TABLE = "name";

	/**
	 * Column name for the result table name of the source, in {@link #links}.
	 * Only null for default scope.
	 */
	public static final String SOURCE_RESULT_TABLE = "source_name";
	
	/**
	 * Column name for the scope of the source, in {@link #links}.
	 * Only null for default scope.
	 */
	public static final String SOURCE_SCOPE = "source";
	
	/**
	 * Column name for the source join value in a {@link #links}.  Can be
	 * <code>null</code>.
	 */
	public static final String VALUE = "value";
	
	/**
	 * Default column names for {@link IOTable}s result table
	 * in {@link MultiTableDatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] { };
	
	/**
	 * Static columns for {@link #links} in {@link MultiTableDatabase}.
	 */
	public static final String[] LINK_TABLE_COLUMNS = new String[] {
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
	private IOTable links;
	
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
	 * Prevent a column name from overlapping with the scope column name.
	 * @param columnName A {@link String} column name to check.
	 * @return <code>columnName</code>, unless it overlaps with 
	 * {@link IOConnection#getScopeColumnName()}, in which case 
	 * it is prepended with {@link #PREPEND}.
	 */
	private String cleanColumnName(String columnName) {
		if(columnName.equals(connection.getScopeColumnName())) {
			return PREPEND + columnName;
		} else {
			return columnName;
		}
	}
	
	/**
	 * Make sure <code>tableName</code> doesn't overlap with the {@link #DEFAULT_TABLE}.
	 * @param tableName The {@link String} possible table name to check.
	 * @return A prepended version of <code>tableName</code> if necessary.
	 */
	private String cleanTableName(String tableName) {
		return tableName.equals(DEFAULT_TABLE) ||
				tableName.equals(LINK_TABLE)
				? PREPEND + tableName : tableName;
	}

	private Map<String, String> getLink(UUID scope) throws DatabaseReadException {
		List<Map<String, String>> results = links
				.select(scope, emptyMap, LINK_TABLE_COLUMNS);
		if(results.size() == 1) {
			return results.get(0);
		} else if(results.size() > 1) {
			throw new DatabaseReadException("Multiple entries for " + scope +
							" in links table.");
		} else {
			throw new DatabaseReadException("No entries for " + scope +
							" in links table.");
		}
	}
	
	/**
	 * Create the result table for a {@link UUID} scope if it doesn't already
	 * exist.  Should be called from somewhere already synchronized to
	 * {@link #connection}.
	 * @param scope
	 * @return
	 * @throws DatabasePersistException
	 */
	private IOTable newResultTable(UUID scope) throws DatabasePersistException {
		
		// find the name of the scope from links.
		try {
			String resultTableName = getLink(scope).get(RESULT_TABLE);
			try {
				return connection.newIOTable(resultTableName, RESULT_TABLE_COLUMNS);
			} catch(ConnectionException e) {
				throw new DatabasePersistException("Could not create result table " + StringUtils.quote(resultTableName), e);
			}
		} catch(DatabaseReadException e) {
			throw new DatabasePersistException("Could not read links table", e);
		}
	}
	
	/**
	 * Obtain the result table for a {@link UUID} scope if it already exists.
	 * Should be called from somewhere already synchronized to {@link #connection}.
	 * @param scope
	 * @return The {@link IOTable} results table, or <code>null</code>
	 * if there is none.
	 * @throws DatabaseReadException if there is a problem with {@link #connection},
	 * or if there are multiple result tables specified for <code>scope</code>.
	 */
	private IOTable getResultTable(UUID scope) throws DatabaseReadException {
		
		// we must have a results table
		try {
			String resultTableName = getLink(scope).get(RESULT_TABLE);
			return connection.getIOTable(resultTableName);
		} catch(ConnectionException e) {
			throw new DatabaseReadException("Error reading from connection", e);
		}
	}
	
	/**
	 * Insert a row into {@link #links}.
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
		
		System.out.println("adding link to " + scope + " : " + insertMap);
		links.insert(scope, insertMap);
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
		links = connection.newIOTable(LINK_TABLE, LINK_TABLE_COLUMNS);
		
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
		synchronized(connection) {
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
	}

	/**
	 * Retrieve a particular value from scope and name.
	 * @param id The {@link UUID} scope of the value to retrieve.
	 * @param columnName The {@link String} name of the value.
	 * @return The {@link String} value, or <code>null</code> if it does not exist.
	 */
	@Override
	public String get(UUID scope, String tagName) throws DatabaseReadException {
		synchronized(connection) {
			ensureOpen();
			
			String columnName = cleanColumnName(tagName);
			
			IOTable resultTable = getResultTable(scope);
			
			// if there is a result table, look through it.
			if(resultTable != null) {
				if(resultTable.hasColumn(columnName)) { // must have the column to have the value
					// there should be only one row per scope in results tables.
					List<Map<String, String>> rows =
							resultTable.select(scope, emptyMap, new String[] { columnName });
					if(rows.size() == 1) {
						return rows.get(0).get(columnName);
					} else if(rows.size() > 1) {
						throw new DatabaseReadException("Should not have stored multiple values for a scope ID");
					}
				}
			}
			
			// if the result table shares a name with the tag, should check the linking value.
			Map<String, String> link = getLink(scope);
			if(link.get(RESULT_TABLE).equals(columnName)) {
				String linkValue = link.get(VALUE);
				if(linkValue != null) {
					return link.get(VALUE);
				}
			}
			
			// not in the linking value, check the source table.
			String sourceScope = link.get(SOURCE_SCOPE);
			if(sourceScope != null) {
				// loop back using sourceScope
				return get(new DeserializedUUID(sourceScope), columnName);
			} else {
				// we're already at a default scope, this columnName doesn't exist.
				return null;
			}
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
		synchronized(connection) {
			try {
				System.out.println("insert one to one: " + scope);
				// if the result table for this scope doesn't exist yet, create it.
				IOTable resultTable = getResultTable(scope);
				if(resultTable == null) {
					System.out.println("new result table: " + name);
					resultTable = newResultTable(scope);
					resultTable.insert(scope, emptyMap); // create stub row, updated later
				}
				
				String columnName = cleanColumnName(name);
				
				// add column if it doesn't exist in table yet. luxury!
				if(!resultTable.hasColumn(columnName)) {
					resultTable.addColumn(columnName);
				}
	
				Map<String, String> updateMap = new HashMap<String, String>();
				updateMap.put(columnName, optValue);
				System.out.println("update " + scope + " with " + updateMap);
				
				// only one row in result tables per scope, don't need to filter by where.
				resultTable.update(scope, emptyMap, updateMap);
			} catch(TableManipulationException e) {
				throw new DatabasePersistException("Could not add column for new value name.", e);
			} catch(DatabaseReadException e) {
				throw new DatabasePersistException("Could not get result table.", e);
			}
		}
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name) throws DatabasePersistException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value)
			throws DatabasePersistException {
		synchronized(connection) {
			UUID scope = idFactory.get();
			String childName = cleanTableName(name);
			
			// add to links
			try {
				String sourceName = getLink(source).get(SOURCE_RESULT_TABLE);
				addLink(scope, source, sourceName, childName, value);
			} catch(DatabaseReadException e) {
				throw new DatabasePersistException("Error reading from links table", e);
			}
			
			return new PersistedDatabaseView(this, scope);
		}
	}
}
