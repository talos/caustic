package net.caustic.database;


import java.util.ArrayList;
import java.util.List;

import net.caustic.scope.Scope;

class PersistedDatabaseView implements DatabaseView {
	
	private final Scope scope;
	private final PersistedDatabase database;
	private final List<DatabaseListener> hooks = new ArrayList<DatabaseListener>();
	
	protected PersistedDatabaseView(PersistedDatabase database, Scope scope) {
		this.database = database;
		this.scope = scope;
	}
	
	@Override
	public PersistedDatabaseView spawnChild(String name)
			throws DatabasePersistException, DatabaseListenerException {
		PersistedDatabaseView child = database.insertOneToMany(scope, name);
		for(DatabaseListener hook : hooks) {
			hook.spawnChild(name, child);
		}
		return child;
	}

	@Override
	public PersistedDatabaseView spawnChild(String name, String value)
			throws DatabasePersistException, DatabaseListenerException {
		PersistedDatabaseView child = database.insertOneToMany(scope, name, value);
		for(DatabaseListener hook : hooks) {
			hook.newScope(name, value, child);
		}
		return child;
	}
	
	@Override
	public String get(String key) throws DatabaseReadException {
		return database.get(scope, key);
	}

	@Override
	public void put(String key, String value)
			throws DatabasePersistException, DatabaseListenerException {
		database.insertOneToOne(scope, key, value);
		for(DatabaseListener hook : hooks) {
			hook.put(key, value);
		}
	}

	@Override
	public void addListener(DatabaseListener viewHook) {
		hooks.add(viewHook);
	}
}
