package net.microscraper.database;


import java.util.ArrayList;
import java.util.List;

import net.microscraper.uuid.UUID;

class PersistedDatabaseView implements DatabaseView {
	
	private final UUID scope;
	private final PersistedDatabase database;
	private final List<DatabaseViewListener> hooks = new ArrayList<DatabaseViewListener>();
	
	protected PersistedDatabaseView(PersistedDatabase database, UUID scope) {
		this.database = database;
		this.scope = scope;
	}
	
	@Override
	public PersistedDatabaseView spawnChild(String name)
			throws DatabasePersistException, DatabaseViewHookException {
		PersistedDatabaseView child = database.insertOneToMany(scope, name);
		for(DatabaseViewListener hook : hooks) {
			hook.spawnChild(name, child);
		}
		return child;
	}

	@Override
	public PersistedDatabaseView spawnChild(String name, String value)
			throws DatabasePersistException, DatabaseViewHookException {
		PersistedDatabaseView child = database.insertOneToMany(scope, name, value);
		for(DatabaseViewListener hook : hooks) {
			hook.spawnChild(name, value, child);
		}
		return child;
	}
	
	@Override
	public String get(String key) throws DatabaseReadException {
		return database.get(scope, key);
	}

	@Override
	public void put(String key, String value)
			throws DatabasePersistException, DatabaseViewHookException {
		database.insertOneToOne(scope, key, value);
		for(DatabaseViewListener hook : hooks) {
			hook.put(key, value);
		}
	}

	@Override
	public void addListener(DatabaseViewListener viewHook) {
		hooks.add(viewHook);
	}
}
