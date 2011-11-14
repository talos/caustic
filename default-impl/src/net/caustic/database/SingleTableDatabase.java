package net.caustic.database;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.caustic.scope.Scope;
import net.caustic.scope.SerializedScope;

public class SingleTableDatabase extends Database {
	public static final String TABLE_NAME = "result";
	
	public static final String SOURCE_COLUMN_NAME = "source";
	public static final String NAME_COLUMN_NAME = "name";
	public static final String VALUE_COLUMN_NAME = "value";
		
	/**
	 * Names of columns in the table.
	 */
	public static final String[] COLUMN_NAMES = new String[] {
		SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
	
	private final Connection connection;
	private Table table;
	
	public SingleTableDatabase(Connection connection) {
		this.connection = connection;
	}

	public String get(Scope scope, String name) throws DatabaseException {
		synchronized(connection) {
			open();
			Map<String, String> whereMap = Collections.emptyMap();
			
			// return all name-values in this scope
			List<Map<String, String>> rows = table.select(scope, whereMap, 
					new String[] { SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME } );
			
			while(scope != null) {
				for(Map<String, String> row : rows) {
					if(row.get(NAME_COLUMN_NAME).equals(name)) {
						return row.get(VALUE_COLUMN_NAME);
					} else if(row.get(SOURCE_COLUMN_NAME) != null) {
						String scopeString = row.get(SOURCE_COLUMN_NAME);
						if(scopeString == null) {
							scope = null; // break loop
						} else {
							scope = new SerializedScope(scopeString);
						}
					}
				}
			}
			return null;
		}
	}
	
	protected void insert(Scope scope, Scope child, String key, String value) 
				throws DatabaseException {
		open();
		Map<String, String> insertMap = new HashMap<String, String>();
		if(scope != null) {
			insertMap.put(SOURCE_COLUMN_NAME, scope.asString());
		}
		insertMap.put(NAME_COLUMN_NAME, key);
		if(value != null) {
			insertMap.put(VALUE_COLUMN_NAME, value);
		}
		
		table.insert(scope, insertMap);
	}
	

	private void open() throws DatabaseException {
		if(table == null) {
			table = connection.newTable(TABLE_NAME, COLUMN_NAMES,
					new String[] { Database.DEFAULT_SCOPE_NAME, SOURCE_COLUMN_NAME } );
		}
	}
}
