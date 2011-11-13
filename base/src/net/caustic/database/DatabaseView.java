package net.caustic.database;

import net.caustic.scope.Scope;

/**
 * {@link DatabaseView} binds a {@link Database} to a particular {@link Scope}.
 */
public final class DatabaseView {

	private final Database database;
	private final Scope scope;
	
	public DatabaseView(Database database) throws DatabaseException {
		this.scope = database.newScope();
		this.database = database;
	}
	
	private DatabaseView(Database database, Scope scope) throws DatabaseException {
		this.scope = scope;
		this.database = database;
	}
		
	public String get(String key) throws DatabaseException {
		return database.get(scope, key);
	}
	
	public void put(String key, String value) throws DatabaseException {
		database.put(scope, key, value);
	}
	
	public DatabaseView spawnChild(String key) throws DatabaseException {
		return new DatabaseView(database, database.newScope(scope, key));
	}
	
	public DatabaseView spawnChild(String key, String value) throws DatabaseException {
		return new DatabaseView(database, database.newScope(scope, key, value));
	}
}