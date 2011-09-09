package net.microscraper.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.microscraper.console.UUID;
import net.microscraper.console.UUIDFactory;

/**
 * An implementation of {@link Database} whose subclasses store
 * all results in a single table.
 * @author talos
 *
 */
public class SingleTableDatabase implements Database {
	
	private static final String TABLE_NAME = "result";
	
	private static final String SCOPE_COLUMN_NAME = "scope";
	private static final String SOURCE_COLUMN_NAME = "source";
	private static final String NAME_COLUMN_NAME = "name";
	private static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Names of columns in the table.
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		SCOPE_COLUMN_NAME, SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
	
	/**
	 * The {@link WritableTable} used by this {@link SingleTableDatabase}.
	 */
	private WritableTable table;

	/**
	 * Whether {@link #open()} has been called.
	 */
	private boolean isOpen = false;
	
	private final WritableConnection connection;
	private final Database backingDatabase;
	private final UUIDFactory idFactory;
		
	/**
	 * 
	 * @throws IllegalStateException If {@link MultiTableDatabase} has not been opened.
	 */
	private void ensureOpen() throws IllegalStateException {
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
	protected void insert(UUID scope, UUID source, String name, String value)
				throws TableManipulationException {
		ensureOpen();
		Map<String, String> map = new HashMap<String, String>();
		map.put(SCOPE_COLUMN_NAME, scope.asString());
		if(source != null) {
			map.put(SOURCE_COLUMN_NAME, source.asString());
		}
		map.put(NAME_COLUMN_NAME, name);
		if(value != null) {
			map.put(VALUE_COLUMN_NAME, value);
		}
		table.insert(map);
	}
	
	/**
	 * Create a {@link SingleTableDatabase} using another database for
	 * retrieving values.
	 * @param backingDatabase The {@link Database} to use when retrieving values.
	 * @param connection A {@link WritableConnection} to use when inserting
	 * values.
	 * @throws IOException if there was a problem creating the table by
	 * <code>connection</code>.
	 */
	public SingleTableDatabase(Database backingDatabase, WritableConnection connection,
			UUIDFactory idFactory) {
		this.connection = connection;
		this.backingDatabase = backingDatabase;
		this.idFactory = idFactory;
	}

	/**
	 * Open {@link #connection} and {@link #backingDatabase}, and create {@link #table}.
	 */
	public void open() throws IOException {
		connection.open();
		backingDatabase.open();
		table = connection.newWritable(TABLE_NAME, COLUMN_NAMES);
		isOpen = true;
	}
	
	/**
	 * Closes the {@link #backingDatabase} and {@link #connection}.
	 */
	public void close() throws IOException {
		backingDatabase.close();
		connection.close();
	}
	
	@Override
	public DatabaseView newView() {
		return new SingleTableDatabaseView(
				backingDatabase.newView(),
				idFactory,
				this);
	}
}
