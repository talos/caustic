package net.microscraper.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.microscraper.console.UUID;

/**
 * An implementation of {@link Database} whose subclasses store
 * all results in a single table.
 * @author talos
 *
 */
public abstract class SingleTableDatabase implements Database {
	
	private final WritableConnection connection;
	
	
	private static final String TABLE_NAME = "result";
	
	private static final String SOURCE_COLUMN_NAME = "source";
	private static final String NAME_COLUMN_NAME = "name";
	private static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Names of columns in the table.
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
	
	/**
	 * The {@link WritableTable} used by this {@link SingleTableDatabase}.
	 */
	private WritableTable table;

	/**
	 * Whether {@link #open()} has been called.
	 */
	private boolean isOpen = false;
	
	/**
	 * 
	 * @throws IllegalStateException If {@link MultiTableDatabase} has not been opened.
	 */
	protected void ensureOpen() throws IllegalStateException {
		if(isOpen == false) {
			throw new IllegalStateException("Must open database before using it.");
		}
	}

	/**
	 * Generate insert appropriate columns into {@link #table}.
	 * @param scope
	 * @param source
	 * @param name
	 * @param value {@link String} can be <code>null</code>
	 * @throws TableManipulationException
	 */
	protected void insert(UUID id, UUID source, String name, String value)
				throws TableManipulationException {
		ensureOpen();
		Map<String, String> map = new HashMap<String, String>();
		if(source != null) {
			map.put(SOURCE_COLUMN_NAME, source.asString());
		}
		map.put(NAME_COLUMN_NAME, name);
		if(value != null) {
			map.put(VALUE_COLUMN_NAME, value);
		}
		table.insert(id, map);
	}
	
	/**
	 * Create a {@link SingleTableDatabase} using another database for
	 * retrieving values.
	 * @param connection A {@link WritableConnection} to use when inserting
	 * values.
	 * @throws IOException if there was a problem creating the table by
	 * <code>connection</code>.
	 */
	public SingleTableDatabase(WritableConnection connection) {
		this.connection = connection;
	}

	/**
	 * Open {@link #connection} and {@link #backingDatabase}, and create {@link #table}.
	 */
	public void open() throws IOException {
		connection.open();
		table = connection.newWritable(TABLE_NAME, COLUMN_NAMES);
		isOpen = true;
	}
	
	/**
	 * Closes the {@link #backingDatabase} and {@link #connection}.
	 */
	public void close() throws IOException {
		connection.close();
	}
}
