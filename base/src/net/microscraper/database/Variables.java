package net.microscraper.database;

import java.io.IOException;

/**
 * {@link Variables} is a binding of a {@link Database} to an integer ID
 * that scopes out which parts of the {@link Database} are visible to the
 * receiver of a {@link Variables}.  With {@link Variables}, {@link Database}
 * operations that return this ID instead return new {@link Variables} instances,
 * and it is never necessary to keep track of this ID.
 * @author realest
 *
 */
public class Variables {
	private final Database database;
	private final int id;
	

	/**
	 * Initialize a new {@link Variables} with a {@link Database} linked to a particular ID.
	 * Should be accessed through {@link Database#getVariables}.
	 */
	public Variables(Database database, int id) {
		this.database = database;
		this.id = id;
	}

	/**
	 * Store a name without a value, and return a new {@link Variables} instance that will
	 * share with its sources.
	 * @param name The {@link String} name of the value to store.
	 * @return A new {@link Variables} instance.
	 * @throws IOException If there was a problem writing to {@link #database}.
	 */
	public Variables storeOneToOne(String name) throws IOException {
		return new Variables(database, database.storeOneToOne(id, name));
	}

	/**
	 * Store a new value, and return a new {@link Variables} instance that will
	 * share with its sources.
	 * @param name The {@link String} name of the value to store.
	 * @param value The {@link String} value to store.
	 * @return A new {@link Variables} instance.
	 * @throws IOException If there was a problem writing to {@link #database}.
	 */
	public Variables storeOneToOne(String name, String value) throws IOException {
		return new Variables(database, database.storeOneToOne(id, name, value));
	}

	/**
	 * Store a name without a value, and return a new {@link Variables} instance that will
	 * read from its sources but not contribute to them.
	 * @param name The {@link String} name of the value to store.
	 * @return A new {@link Variables} instance.
	 * @throws IOException If there was a problem writing to {@link #database}.
	 */
	public Variables storeOneToMany(String name) throws IOException {
		return new Variables(database, database.storeOneToMany(id, name));
	}
	
	/**
	 * Store a new value, and return a new {@link Variables} instance that will
	 * read from its sources but not contribute to them.
	 * @param name The {@link String} name of the value to store.
	 * @param value The {@link String} value to store.
	 * @return A new {@link Variables} instance.
	 * @throws IOException If there was a problem writing to {@link #database}.
	 */
	public Variables storeOneToMany(String name, String value) throws IOException {
		return new Variables(database, database.storeOneToMany(id, name, value));
	}
	
	/**
	 * Get the {@link String} value visible for this <code>name</code> within this
	 * {@link Variables}.
	 * @param name The {@link String} name whose value should be pulled.
	 * @return The {@link String} value if it is available, <code>null</code> otherwise.
	 */
	public String get(String name) {
		return database.get(id, name);
	}
	
	public String toString() {
		return database.toString(id);
	}
}
