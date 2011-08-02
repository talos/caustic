package net.microscraper.interfaces.database;

import java.util.Hashtable;

import net.microscraper.BasicNameValuePair;
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
	 * Column name for the ID of the source of a result row.
	 */
	private static final String SOURCE_ID = "source_id";
	
	/**
	 * Column name for the name of the table where the result row's source can be
	 * found.
	 */
	private static final String SOURCE_NAME = "source_name";
	
	/**
	 * Default column names for {@link Table}s in {@link MultiTableDatabase}.
	 */
	private static final String[] COLUMN_NAMES = new String[] { SOURCE_ID, SOURCE_NAME };
	
	/**
	 * The {@link Table} that holds {@link Result}s that don't have
	 * a source.  These would be from the first layer of
	 * {@link Instruction}s.  This table has only one row.
	 */
	private Table rootTable;
	
	/**
	 * The {@link int} ID for the only row of {@link #rootTable}.
	 */
	private int rootResultId;
	
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
		rootTable = getTable(ROOT_TABLE_NAME);
		rootResultId = rootTable.insert(new NameValuePair[] {});
	}
	
	public Result store(String name, String value) throws DatabaseException {
		rootTable.update(name, value);
		return generateResult(rootResultId, name, value);
	}
	
	public Result store(Result source, String name, String value) throws DatabaseException {
		String sourceTableName = PREPEND + source.getName();
		Table sourceTable;
		if(tables.containsKey(sourceTableName)) {
			sourceTable = (Table) tables.get(sourceTableName);
		} else {
			sourceTable = getTable(sourceTableName);
			tables.put(sourceTableName, sourceTable);
		}
		sourceTable.update(source.getId(), new NameValuePair[] { new BasicNameValuePair(name, value) });
		return generateResult(source, sourceTableName, value);
	}
	
	private Result generateResult(Result sourceResult,
			String name, String value) throws DatabaseException {
		String tableName = PREPEND + name;
		Table table;
		if(tables.containsKey(tableName)) {
			table = (Table) tables.get(tableName);
		} else {
			table = getTable(tableName);
			tables.put(tableName, table);
		}
		
		return new BasicResult(table.insert(new NameValuePair[] {}), name, value);
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
	 * @throws DatabaseException if the {@link Table} cannot be made.
	 */
	private final Table getTable(String name) throws DatabaseException {
		return getTable(name, COLUMN_NAMES );
	}
}
