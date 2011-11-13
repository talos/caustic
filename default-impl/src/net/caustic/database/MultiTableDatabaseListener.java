package net.caustic.database;

import java.util.HashMap;
import java.util.Map;

import net.caustic.scope.Scope;

class MultiTableDatabaseListener implements DatabaseListener {
	private final IOConnection connection;
	private final MultiTableDatabase db;
	
	MultiTableDatabaseListener(MultiTableDatabase db, IOConnection connection) {
		this.db = db;
		this.connection = connection;
	}
	
	@Override
	public void put(Scope scope, String key, String value)
			throws DatabaseListenerException {
		synchronized(connection) {
			// if the result table for this scope doesn't exist yet, create it.
			String columnName = db.cleanColumnName(key);
			Map<String, String> map = new HashMap<String, String>();
			if(value != null) {
				map.put(columnName, value);
			}
			
			try {
				String resultTableName = db.getResultTableName(scope);

				IOTable resultTable = connection.getIOTable(resultTableName);
				if(resultTable == null) { // have to create the table from scratch, insert new row
					resultTable = connection.newIOTable(resultTableName, new String[] { columnName },
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
			} catch(ConnectionException e) {
				throw new DatabaseListenerException("Could not create or read result table", e);
			} catch(TableManipulationException e) {
				throw new DatabaseListenerException("Could not add column for new value name.", e);
			} catch(DatabaseReadException e) {
				throw new DatabaseListenerException("Could not get result table name.", e);
			}
			
		}
	}

	@Override
	public void newScope(Scope scope) throws DatabaseListenerException { }

	@Override
	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		newScope(parent, key, null, child);
	}

	@Override
	public void newScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		synchronized(connection) {
			// add to links
			Map<String, String> insertMap = new HashMap<String, String>();
			insertMap.put(MultiTableDatabase.RESULT_TABLE, cleanTableName(key));
			if(parent != null) {
				insertMap.put(MultiTableDatabase.SOURCE_SCOPE, parent.asString());
			}
			if(value != null) {
				insertMap.put(MultiTableDatabase.VALUE, value);
			}
			try {
				db.insertLink(child, insertMap);				
			} catch(TableManipulationException e) {
				throw new DatabaseListenerException("Couldn't create link", e);
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
