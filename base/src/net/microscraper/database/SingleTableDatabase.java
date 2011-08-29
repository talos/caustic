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
	private final Insertable table;
	
	private final Database backingDatabase;
		
	private Hashtable generateMap(Scope scope, Scope source, String name, String value) {
		Hashtable map = new Hashtable();
		map.put(SCOPE_COLUMN_NAME, scope.getID().asString());
		map.put(SOURCE_COLUMN_NAME, source.getID().asString());
		map.put(NAME_COLUMN_NAME, name);
		map.put(VALUE_COLUMN_NAME, value);
		return map;
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
	public SingleTableDatabase(Database backingDatabase,
			InsertableConnection connection) throws IOException {
		this.table = connection.getInsertable(COLUMN_NAMES);
		this.backingDatabase = backingDatabase;
	}
	
	public void close() throws IOException { }

	public void storeOneToOne(Scope source, String name)
			throws TableManipulationException, IOException {
		backingDatabase.storeOneToOne(source, name);
	}
	
	public void storeOneToOne(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		backingDatabase.storeOneToOne(source, name, value);
		table.insert(generateMap(source, source, name, value));
	}

	public Scope storeOneToMany(Scope source, String name)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name);
		table.insert(generateMap(scope, source, name, ""));
		return scope;
	}

	public Scope storeOneToMany(Scope source, String name, String value)
			throws TableManipulationException, IOException {
		Scope scope = backingDatabase.storeOneToMany(source, name, value);
		table.insert(generateMap(scope, source, name, value));
		return scope;
	}
	
	public String get(Scope scope, String key) {
		return backingDatabase.get(scope, key);
	}
	
	public Scope getScope() throws IOException {
		return backingDatabase.getScope();
	}
	
	public String toString(Scope scope) {
		return backingDatabase.toString(scope);
	}
}
