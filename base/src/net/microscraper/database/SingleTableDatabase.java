package net.microscraper.database;

import java.io.IOException;

import net.microscraper.instruction.Result;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;

/**
 * An implementation of {@link Database} whose subclasses store
 * all {@link Result}s in a single table.
 * @author talos
 *
 */
public final class SingleTableDatabase implements Database {
		
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
	 * Names of columns in {@link IOTable}
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		SOURCE_ID_COLUMN, NAME_COLUMN, VALUE_COLUMN
	};
	
	/**
	 * The {@link WritableTable} used by this {@link SingleTableDatabase}.
	 */
	private WritableTable table;
			
	public SingleTableDatabase(WritableConnection connection) throws IOException {
		this.table = connection.getWritableTable(COLUMN_NAMES);
	}

	public final int store(String name, String value, int resultNum)
			throws TableManipulationException {
		return table.insert(
				new NameValuePair[] {
					new BasicNameValuePair(SOURCE_ID_COLUMN, null),
					new BasicNameValuePair(NAME_COLUMN, name),
					new BasicNameValuePair(VALUE_COLUMN, value)
				});
	}
	
	public final int store(String sourceName, int sourceId, String name, String value, int resultNum)
			throws TableManipulationException {
		return table.insert(
				new NameValuePair[] {
					new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(sourceId)),
					new BasicNameValuePair(NAME_COLUMN, name),
					new BasicNameValuePair(VALUE_COLUMN, value)
				});
	}
	
	public void close() throws IOException { }

	public void clean() throws TableManipulationException { }
}
