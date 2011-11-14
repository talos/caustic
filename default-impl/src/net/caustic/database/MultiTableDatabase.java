package net.caustic.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.caustic.scope.SerializedScope;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * An implementation of {@link PersistedDatabase} whose subclasses store
 * {@link ScraperResult}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public final class MultiTableDatabase extends Database {
	public static final Map<String, String> EMPTY_MAP = Collections.emptyMap();

	private final Connection connection;
	
	/**
	 * String to prepend before table names to prevent collision
	 * with {@link #DEFAULT_TABLE}, and to prepend before column
	 * names to prevent collision with {@link #DEFAULT_SCOPE_NAME}.
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
	 * Static columns for {@link #links} in {@link PersistedMultiTableDatabase}.
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
	private Table links;

	/**
	 * Whether {@link #open()} has been called.
	 */
	private boolean isOpen = false;
	
	public MultiTableDatabase(Connection connection) {
		this.connection = connection;
		addListener(new MultiTableDatabaseListener(this));
	}
	
	/**
	 * Retrieve a particular value from scope and name.
	 * @param scope The {@link Scope} scope of the value to retrieve.
	 * @param tagName The {@link String} name of the value.
	 * @return The {@link String} value, or <code>null</code> if it does not exist in
	 * this <code>scope</code>.
	 * @throws
	 */
	@Override
	public String get(Scope scope, String tagName) throws DatabaseException {
		synchronized(connection) {
			open();
			
			String columnName = cleanColumnName(tagName);
			
			try {
				Table resultTable = connection.getTable(getResultTableName(scope));
				
				// if there is a result table, look through it.
				if(resultTable != null) {
					if(resultTable.hasColumn(columnName)) { // must have the column to have the value
						// there should be only one row per scope in results tables.
						List<Map<String, String>> rows =
								resultTable.select(scope, EMPTY_MAP, new String[] { columnName });
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
				return get(new SerializedScope(sourceScope), columnName);
			} else {
				// we're already at a default scope, this columnName doesn't exist.
				return null;
			}
		}
	}
	
	protected void insert(Scope scope, String key, String value) throws DatabaseException {
		synchronized(connection) {
			// if the result table for this scope doesn't exist yet, create it.
			String columnName = cleanColumnName(key);
			Map<String, String> map = new HashMap<String, String>();
			if(value != null) {
				map.put(columnName, value);
			}
		
			String resultTableName = getResultTableName(scope);

			Table resultTable = connection.getTable(resultTableName);
			if(resultTable == null) { // have to create the table from scratch, insert new row
				resultTable = connection.newTable(resultTableName, new String[] { columnName },
						new String[] { MultiTableDatabase.DEFAULT_SCOPE_NAME });
				resultTable.insert(scope, map);	

			} else { // have to update existing row, perhaps after alteration.
				// add column if it doesn't exist in table yet. luxury!
				if(!resultTable.hasColumn(columnName)) {
					resultTable.addColumn(columnName);

				}
				if(resultTable.select(scope, MultiTableDatabase.EMPTY_MAP, new String[] {} ).size() == 1) {
					resultTable.update(scope, MultiTableDatabase.EMPTY_MAP, map);						
				} else {
					resultTable.insert(scope, map);
				}
			}
		}
	}
	
	protected void insertLink(Scope parent, Scope child, String key, String value) throws DatabaseException {
		synchronized(connection) {
			open();
			Map<String, String> insertMap = new HashMap<String, String>();
			insertMap.put(MultiTableDatabase.RESULT_TABLE, cleanTableName(key));
			if(parent != null) {
				insertMap.put(MultiTableDatabase.SOURCE_SCOPE, parent.asString());
			}
			if(value != null) {
				insertMap.put(MultiTableDatabase.VALUE, value);
			}
			links.insert(child, insertMap);
		}
	}
	
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
	
	private Map<String, String> getLink(Scope scope) throws DatabaseReadException {
		synchronized(connection) {
			List<Map<String, String>> results = links
					.select(scope, EMPTY_MAP, LINK_TABLE_COLUMNS);
			if(results.size() == 1) {
				return results.get(0);
			} else if(results.size() > 1) {
				throw new DatabaseReadException("Multiple entries for scope " + StringUtils.quote(scope) +
								" in links table.");
			} else {
				throw new DatabaseReadException("No entries for scope " + StringUtils.quote(scope) +
								" in links table.");
			}
		}
	}
	
	private String getResultTableName(Scope scope) throws DatabaseException {
		return getLink(scope).get(RESULT_TABLE);
	}

	/**
	 * Opens {@link #connection} and {@link #backingDatabase}, and creates
	 * the {@link #defaultTable} if {@Link Database} is not already open.
	 */
	private void open() throws DatabaseException {
		if(isOpen == false) {
			synchronized(connection) {
				connection.open();
				links = connection.newTable(LINK_TABLE, LINK_TABLE_COLUMNS, new String[] { DEFAULT_SCOPE_NAME });
				
				// create default table
				//connection.newIOTable(DEFAULT_TABLE, RESULT_TABLE_COLUMNS);
				//insertOneToOne(id, name)
				isOpen = true;
			}
		}
	}
	
	/**
	 * Make sure <code>tableName</code> doesn't overlap with the {@link #DEFAULT_TABLE}.
	 * @param tableName The {@link String} possible table name to check.
	 * @return A prepended version of <code>tableName</code> if necessary.
	 */
	private String cleanTableName(String tableName) {
		return tableName.equals(MultiTableDatabase.DEFAULT_TABLE) ||
				tableName.equals(MultiTableDatabase.LINK_TABLE)
				? MultiTableDatabase.PREPEND + tableName : tableName;
	}
}
