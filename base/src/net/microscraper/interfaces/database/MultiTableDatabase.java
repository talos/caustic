package net.microscraper.interfaces.database;

import java.util.Hashtable;

import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;

/**
 * An abstract implementation of {@link Database} whose subclasses store
 * {@link Result}s into separate tables, based off of their source's name.
 * @author talos
 *
 */
public abstract class MultiTableDatabase implements Database {
	
	/**
	 * String to prepend before table names to prevent collision
	 * with {@link #ROOT_TABLE_NAME}.
	 */
	private static final char PREPEND = '_';
	
	/**
	 * Name of {@link #rootTable}.
	 */
	private static final String ROOT_TABLE_NAME = "root";
	
	/**
	 * The {@link ChildResultsTable} that holds {@link Result}s that don't have
	 * a source.  These would be from the first layer of
	 * {@link Instruction}s.  This table has only one row.
	 * @see #rootTableRowId
	 */
	private RootResultsTable rootTable;
	
	/**
	 * The {@link Result} for the only row of {@link #rootTable}.
	 */
	private Result rootResult;
	
	/**
	 * A {@link Hashtable} of all the {@link ChildResultsTable}s in this 
	 * {@link MultiTableDatabase}, excepting {@link #rootTable}.
	 * Keyed by {@link String} {@link ChildResultsTable} names.
	 */
	private final Hashtable tables = new Hashtable();
	
	/**
	 * Create the root table with no values in it.
	 * @throws DatabaseException If the root table cannot be created.
	 */
	public final void open() throws DatabaseException {
		rootTable = getRootResultsTable();
		rootResult = rootTable.getRootResult();
	}
	
	public Result store(String name, String value) throws DatabaseException {
		rootTable.update(name, value);
		return generateResult(rootResult, name, value);
	}
	
	public Result store(Result source, String name, String value) throws DatabaseException {
		String sourceTableName = PREPEND + source.getName();
		ChildResultsTable sourceTable;
		if(tables.containsKey(sourceTableName)) {
			sourceTable = (ChildResultsTable) tables.get(sourceTableName);
		} else {
			sourceTable = getChildResultsTable(sourceTableName);
			tables.put(sourceTableName, sourceTable);
		}
		sourceTable.update(source, name, value);
		return generateResult(source, sourceTableName, value);
	}
	
	private Result generateResult(Result sourceResult,
			String name, String value) throws DatabaseException {
		String tableName = PREPEND + name;
		ChildResultsTable table;
		if(tables.containsKey(tableName)) {
			table = (ChildResultsTable) tables.get(tableName);
		} else {
			table = getChildResultsTable(tableName);
			tables.put(tableName, table);
		}
		
		return new BasicResult(table.insert(sourceResult), name, value);
	}
	
	private static final class BasicResult implements Result {
		private final int id;
		private final String name;
		private final String value;
		public BasicResult(int id, String name, String value) {
			this.id = id;
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public int getId() {
			return id;
		}
	}
	
	/**
	 * Obtain {@link Table} for the {@link MultiTableDatabase}.
	 * @param name The {@link String} name of the {@link Result} parent of
	 * {@link ChildResultsTable}.
	 * @return A {@link ChildResultsTable}.
	 */
	private final Table getChildResultsTable(String name) {
		return getTable(name, textColumns)
	}
}
