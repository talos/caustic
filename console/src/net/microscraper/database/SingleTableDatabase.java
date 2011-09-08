package net.microscraper.database;

import java.io.IOException;
import java.util.Hashtable;

/**
 * An implementation of {@link Database} whose subclasses store
 * all results in a single table.
 * @author talos
 *
 */
public final class SingleTableDatabase implements Database {
	
	public static final String TABLE_NAME = "result";
	
	private static final String SCOPE_COLUMN_NAME = "scope";
	private static final String SOURCE_COLUMN_NAME = "source";
	private static final String NAME_COLUMN_NAME = "name";
	private static final String VALUE_COLUMN_NAME = "value";
	
	/**
	 * Names of columns in {@link Insertable}
	 */
	private static final String[] COLUMN_NAMES = new String[] {
		SCOPE_COLUMN_NAME, SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME
	};
	
	/**
	 * The {@link Insertable} used by this {@link SingleTableDatabase}.
	 */
	private Insertable table;

	/**
	 * Whether {@link #open()} has been called.
	 */
	private boolean isOpen = false;
	
	private final InsertableConnection connection;
	private final Database backingDatabase;
		
	/**
	 * Generate insert appropriate columns into {@link #table}.
	 * @param scope
	 * @param source
	 * @param name
	 * @param value {@link String} can be <code>null</code>
	 * @throws TableManipulationException
	 */
	private void insert(Scope scope, Scope source, String name, String value)
				throws TableManipulationException {
		ensureOpen();
		Hashtable map = new Hashtable();
		map.put(SCOPE_COLUMN_NAME, scope.getID().asString());
		map.put(SOURCE_COLUMN_NAME, source.getID().asString());
		map.put(NAME_COLUMN_NAME, name);
		if(value != null) {
			map.put(VALUE_COLUMN_NAME, value);
		}
		table.insert(map);
	}

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
	 * Create a {@link SingleTableDatabase} using another database for
	 * retrieving values.
	 * @param backingDatabase The {@link Database} to use when retrieving values.
	 * @param connection A {@link InsertableConnection} to use when inserting
	 * values.
	 * @throws IOException if there was a problem creating the table by
	 * <code>connection</code>.
	 */
	public SingleTableDatabase(Database backingDatabase, InsertableConnection connection) {
		this.connection = connection;
		this.backingDatabase = backingDatabase;
	}

	/**
	 * Open {@link #connection} and {@link #backingDatabase}, and create {@link #table}.
	 */
	public void open() throws IOException {
		connection.open();
		backingDatabase.open();
		table = connection.newInsertable(TABLE_NAME, COLUMN_NAMES);
		isOpen = true;
	}
	
	/**
	 * Closes the {@link #backingDatabase} and {@link #connection}.
	 */
	public void close() throws IOException {
		backingDatabase.close();
		connection.close();
	}
	
	public void storeOneToOne(Scope source, String name)
			throws TableManipulationException, IOException {
		backingDatabase.storeOneToOne(source, name);
	}
	
	public void storeOneToOne(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		backingDatabase.storeOneToOne(source, name, value);
		insert(source, source, name, value);
	}

	public Scope storeOneToMany(Scope source, String name)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name);
		insert(scope, source, name, null);
		return scope;
	}

	public Scope storeOneToMany(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name, value);
		insert(scope, source, name, value);
		return scope;
	}
	
	public String get(Scope scope, String key) {
		return backingDatabase.get(scope, key);
	}
	
	public Scope getDefaultScope() throws IOException {
		return backingDatabase.getDefaultScope();
	}
	
	public String toString(Scope scope) {
		return backingDatabase.toString(scope);
	}
}
