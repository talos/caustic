package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

/**
 * An implementation of {@link Database} whose subclasses store
 * {@link Result}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public final class MultiTableDatabase implements Database {
	
	/**
	 * String to prepend before table names to prevent collision
	 * with {@link #ROOT_TABLE_NAME}, and to prepend before column
	 * names to prevent collision with anything in {@link #COLUMN_NAMES}.
	 */
	public static final char PREPEND = '_';
	
	/**
	 * Name of {@link #rootTable}.
	 */
	public static final String ROOT_TABLE_NAME = "root";
	
	/**
	 * Column name for the scope of the source row.
	 */
	public static final String SOURCE_COLUMN_NAME = "_source";
	
	/**
	 * Column name for the scope of this row.
	 */
	public static final String SCOPE_COLUMN_NAME = "_scope";
	
	/**
	 * Column name for the name of the table where the result row's source can be
	 * found.
	 */
	public static final String SOURCE_TABLE_COLUMN = "_source_table";
	
	public static final String VALUE_COLUMN_NAME = "_value";
	
	public static final String[] ROOT_TABLE_COLUMNS = new String[] {
		SCOPE_COLUMN_NAME
	};
	
	/**
	 * Default column names for {@link Updateable}s in {@link MultiTableDatabase}.
	 */
	public static final String[] RESULT_TABLE_COLUMNS = new String[] {
		SCOPE_COLUMN_NAME,
		SOURCE_COLUMN_NAME,
		SOURCE_TABLE_COLUMN,
		VALUE_COLUMN_NAME
	};
	
	private final Hashtable nameTables = new Hashtable();
	
	/**
	 * A {@link Hashtable} keying {@link Scope}s to {@link String} names.
	 */
	private final Hashtable scopeNames = new Hashtable();
	
	/**
	 * A {@link UpdateableConnection} to use when generating tables.
	 */
	private final UpdateableConnection connection;
	
	private final Database backingDatabase;
	
	private String cleanColumnName(String columnName) {
		for(int i = 0 ; i < RESULT_TABLE_COLUMNS.length ; i ++) {
			if(columnName.equals(RESULT_TABLE_COLUMNS[i])) {
				return PREPEND + columnName;
			}
		}
		return columnName;
	}
	
	private String cleanTableName(String tableName) {
		return tableName.equals(ROOT_TABLE_NAME) ? PREPEND + tableName : tableName;
	}	
	
	/**
	 * Create a {@link MultiTableDatabase} using another database for
	 * retrieving values.
	 */
	public MultiTableDatabase(Database backingDatabase,
			UpdateableConnection connection) throws IOException {
		this.backingDatabase = backingDatabase;
		this.connection = connection;
		nameTables.put(ROOT_TABLE_NAME, connection.getIOTable(ROOT_TABLE_NAME, ROOT_TABLE_COLUMNS));
	}
	
	public void storeOneToOne(Scope source, String name)
			throws TableManipulationException, IOException {
		backingDatabase.storeOneToOne(source, name);
	}
	
	/**
	 * Add this name & value to the table of sourceId.  Create the column for the name
	 * if it doesn't already exist.
	 */
	public void storeOneToOne(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		String tableName = (String) scopeNames.get(source);
		Updateable table = (Updateable) nameTables.get(tableName);
		if(!table.hasColumn(cleanColumnName(name))) {
			table.addColumn(cleanColumnName(name));
		}
		Hashtable map = new Hashtable();
		map.put(cleanColumnName(name), value);
		
		table.update(SCOPE_COLUMN_NAME, source.getID(), map);
		
		backingDatabase.storeOneToOne(source, name, value);
	}

	public Scope storeOneToMany(Scope source, String name)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name);
		
		String sourceTableName = (String) scopeNames.get(source);
		
		Updateable table;
		scopeNames.put(scope, cleanTableName(name));
		if(nameTables.containsKey(cleanTableName(name))) {
			table = (Updateable) nameTables.get(cleanTableName(name));
		} else {
			table = connection.getIOTable(cleanTableName(name), RESULT_TABLE_COLUMNS);
			nameTables.put(cleanTableName(name), table);
		}
		
		Hashtable map = new Hashtable();
		map.put(SCOPE_COLUMN_NAME, scope.getID().asString());
		map.put(SOURCE_COLUMN_NAME, source.getID().asString());
		map.put(SOURCE_TABLE_COLUMN, sourceTableName);
		//map.put(VALUE_COLUMN_NAME, "");
		table.insert(map);
		
		return scope;
	}

	/**
	 * Insert a new row into a table for this result.
	 */
	public Scope storeOneToMany(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name, value);
		
		String sourceTableName = (String) scopeNames.get(source);
		
		Updateable table;
		scopeNames.put(scope, cleanTableName(name));
		if(nameTables.containsKey(cleanTableName(name))) {
			table = (Updateable) nameTables.get(cleanTableName(name));
		} else {
			table = connection.getIOTable(cleanTableName(name), RESULT_TABLE_COLUMNS);
			nameTables.put(cleanTableName(name), table);
		}
		
		Hashtable map = new Hashtable();
		map.put(SCOPE_COLUMN_NAME, scope.getID().asString());
		map.put(SOURCE_COLUMN_NAME, source.getID().asString());
		map.put(SOURCE_TABLE_COLUMN, sourceTableName);
		map.put(VALUE_COLUMN_NAME, value);
		table.insert(map);
		
		return scope;
	}
	
	public void close() throws IOException {
		connection.close();
	}

	public String get(Scope scope, String key) {
		return backingDatabase.get(scope, key);
	}

	public Scope getScope() throws IOException {
		Scope scope = backingDatabase.getScope();
		scopeNames.put(scope, ROOT_TABLE_NAME);
		return scope;
	}

	public String toString(Scope scope) {
		return backingDatabase.toString(scope);
	}
}
