package net.microscraper.database;

import java.io.IOException;

import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;

/**
 * An implementation of {@link Database} whose subclasses store
 * all results in a single table.
 * @author talos
 *
 */
public final class SingleTableDatabase implements Database {
	
	private static final String ID_COLUMN = "id";
	private static final String SOURCE_ID_COLUMN = "source_id";
	private static final String NAME_COLUMN = "name";
	private static final String VALUE_COLUMN = "value";
	
	/**
	 * Names of columns in {@link IOTable}
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		ID_COLUMN, SOURCE_ID_COLUMN, NAME_COLUMN, VALUE_COLUMN
	};
	
	/**
	 * The {@link WritableTable} used by this {@link SingleTableDatabase}.
	 */
	private final WritableTable table;
	
	private final int firstId = 0;
	private int curId = firstId;
	
	public SingleTableDatabase(WritableConnection connection) throws IOException {
		this.table = connection.getWritableTable(COLUMN_NAMES);
	}
	
	public void close() throws IOException { }

	public void clean() throws TableManipulationException { }

	public int store(int sourceId, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(
				new NameValuePair[] {
						new BasicNameValuePair(ID_COLUMN, Integer.toString(curId)),
						new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(sourceId)),
						new BasicNameValuePair(NAME_COLUMN, name),
						new BasicNameValuePair(VALUE_COLUMN, value)
					});
		return sourceId;
	}

	public int store(int sourceId, int resultNum)
			throws TableManipulationException, IOException {
		return sourceId;
	}

	public int store(int sourceId, int resultNum, String name, String value)
			throws TableManipulationException, IOException {
		curId++;
		table.insert(
				new NameValuePair[] {
						new BasicNameValuePair(ID_COLUMN, Integer.toString(curId)),
						new BasicNameValuePair(SOURCE_ID_COLUMN, Integer.toString(sourceId)),
						new BasicNameValuePair(NAME_COLUMN, name),
						new BasicNameValuePair(VALUE_COLUMN, value)
					});
		return curId;
	}
	
	public int getFirstId() {
		return firstId;
	}
}
