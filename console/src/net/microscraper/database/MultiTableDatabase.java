package net.microscraper.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	 * Static columns for {@link #links} in {@link MultiTableDatabase}.
	 */
	public static final String[] LINK_TABLE_COLUMNS = new String[] {
		SOURCE_SCOPE,
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
	
	//private Map<UUID, Boolean> lockedScopes = Collections.synchronizedMap(new HashMap<UUID, Boolean>());
	
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
	/*
	private Boolean getLock(UUID scope) {
		synchronized(lockedScopes) {
			return lockedScopes.get(scope);
		}
	}*/
	
	private String getResultTableName(UUID scope)  throws DatabaseReadException {
		return getLink(scope).get(RESULT_TABLE);
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
		//connection.newIOTable(DEFAULT_TABLE, RESULT_TABLE_COLUMNS);
		//insertOneToOne(id, name)
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
		return insertOneToMany(null, DEFAULT_TABLE);
	}
	
	/**
	 * Retrieve a particular value from scope and name.
	 * @param scope The {@link UUID} scope of the value to retrieve.
	 * @param tagName The {@link String} name of the value.
	 * @return The {@link String} value, or <code>null</code> if it does not exist in
	 * this <code>scope</code>.
	 * @throws
	 */
	@Override
	public String get(UUID scope, String tagName) throws DatabaseReadException {
		synchronized(connection) {
			ensureOpen();
			
			String columnName = cleanColumnName(tagName);
			
			try {
				IOTable resultTable = connection.getIOTable(getResultTableName(scope));
				
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
			} catch(ConnectionException e) {
				throw new DatabaseReadException("Could not get result table");
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
			// if the result table for this scope doesn't exist yet, create it.
			String columnName = cleanColumnName(name);
			Map<String, String> map = new HashMap<String, String>();
			if(optValue != null) {
				map.put(columnName, optValue);
			}
			
			try {
				String resultTableName = getResultTableName(scope);

				IOTable resultTable = connection.getIOTable(resultTableName);
				if(resultTable == null) { // have to create the table from scratch, insert new row
					resultTable = connection.newIOTable(resultTableName, new String[] { columnName });
					resultTable.insert(scope, map);	

				} else { // have to update existing row, perhaps after alteration.
					// add column if it doesn't exist in table yet. luxury!
					if(!resultTable.hasColumn(columnName)) {
						resultTable.addColumn(columnName);

					}
					if(resultTable.select(scope, emptyMap, new String[] {} ).size() == 1) {
						resultTable.update(scope, emptyMap, map);						
					} else {
						resultTable.insert(scope, map);
					}
				}
			} catch(ConnectionException e) {
				throw new DatabasePersistException("Could not create or read result table", e);
			} catch(TableManipulationException e) {
				throw new DatabasePersistException("Could not add column for new value name.", e);
			} catch(DatabaseReadException e) {
				throw new DatabasePersistException("Could not get result table name.", e);
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
		/*Boolean lock;
		if(source == null) {
			lock = new Boolean(true);
		} else  {
			lock = lockedScopes.get(source);
		}*/
		synchronized(connection) {
			UUID scope = idFactory.get();
			/*synchronized(lockedScopes) {
				lockedScopes.put(scope, lock);
			}*/
			// add to links
			Map<String, String> insertMap = new HashMap<String, String>();
			insertMap.put(RESULT_TABLE, cleanTableName(name));
			if(source != null) {
				insertMap.put(SOURCE_SCOPE, source.asString());
			}
			if(value != null) {
				insertMap.put(VALUE, value);
			}
			
			links.insert(scope, insertMap);
			
			return new PersistedDatabaseView(this, scope);
		}
	}
}
