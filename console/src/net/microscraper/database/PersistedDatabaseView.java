package net.microscraper.database;


import net.microscraper.uuid.UUID;

public class PersistedDatabaseView implements DatabaseView {
	
	private final UUID scope;
	private final PersistedDatabase database;
	
	protected PersistedDatabaseView(PersistedDatabase database, UUID scope) {
		this.database = database;
		this.scope = scope;
	}
	
	@Override
	public PersistedDatabaseView spawnChild(String name) throws DatabasePersistException {
		return database.insertOneToMany(scope, name);
	}

	@Override
	public PersistedDatabaseView spawnChild(String name, String value)
			throws DatabasePersistException {
		return database.insertOneToMany(scope, name, value);
	}
	
	@Override
	public String get(String key) throws DatabaseReadException {
		return database.get(scope, key);
	}

	@Override
	public void put(String key, String value) throws DatabasePersistException {
		database.insertOneToOne(scope, key, value);
	}
}
