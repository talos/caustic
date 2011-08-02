package net.microscraper.impl.database;

import net.microscraper.BasicNameValuePair;
import net.microscraper.NameValuePair;
import net.microscraper.executable.Result;
import net.microscraper.interfaces.database.Connection;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.database.Table;

/**
 * An implementation of {@link Database} whose subclasses store
 * all {@link Result}s in a single table.
 * @author talos
 *
 */
public final class SingleTableDatabase implements Database {
	
	/**
	 * Name of the one {@link AllResultsTable} in {@link SingleTableDatabase}.
	 */
	private static final String TABLE_NAME = "results";
	
	/**
	 * Name of column to hold value for {@link Result#getId()}
	 * from {@link Result#getSource()}.
	 */
	private static final String SOURCE_ID_COLUMN = "source_id";
	
	/**
	 * Name of column to hold value for {@link Result#getName()}.
	 */
	private static final String NAME_COLUMN = "name";
	
	/**
	 * Name of column to hold value for {@link Result#getValue()}.
	 */
	private static final String VALUE_COLUMN = "value";
	
	/**
	 * Names of columns in {@link Table}
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		SOURCE_ID_COLUMN, NAME_COLUMN, VALUE_COLUMN
	};
	
	/**
	 * The {@link AllResultsTable} used by this {@link SingleTableDatabase}.
	 */
	private Table table;
	
	public SingleTableDatabase(Connection connection) throws DatabaseException {
		table = connection.getTable(TABLE_NAME, COLUMN_NAMES);
	}

	public final Result store(String name, String value)
			throws DatabaseException {
		return new Result(table.insert(
				new NameValuePair[] {
					new BasicNameValuePair(NAME_COLUMN, name),
					new BasicNameValuePair(VALUE_COLUMN, value)
				}),
			name, value );
	}
	
	public final Result store(Result source, String name, String value)
			throws DatabaseException {
		return new Result(table.insert(
				new NameValuePair[] {
					new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(source.getId())),
					new BasicNameValuePair(NAME_COLUMN, name),
					new BasicNameValuePair(VALUE_COLUMN, value)
				}),
			name, value );
	}
}
