package net.microscraper.database;

import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

/**
 * This implementation of {@link DatabaseView} writes to a {@link SingleTable}
 * but does not read from it.  Instead, it wraps around an {@link InMemoryDatabaseView}
 * for read operations.
 * @author talos
 *
 */
class NonPersistedDatabaseView implements DatabaseView {
	
	private final InMemoryDatabaseView view;
	private final WritableTable table;
	private final UUIDFactory idFactory;
	private final UUID scope;
	
	private NonPersistedDatabaseView(NonPersistedDatabaseView parent, String name, String value)
			throws DatabasePersistException {
		this.idFactory = parent.idFactory;
		this.scope = idFactory.get();
		this.table = parent.table;
		if(value == null) {
			this.view = (InMemoryDatabaseView) parent.view.spawnChild(name);
		} else {
			this.view = (InMemoryDatabaseView) parent.view.spawnChild(name, value);
		}
		SingleTable.insert(table, scope, parent.scope, name, value);
	}
	
	public NonPersistedDatabaseView(UUIDFactory idFactory, WritableTable table)
			throws DatabasePersistException {
		this.view = new InMemoryDatabaseView();
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.table = table;
	}
	
	@Override
	public NonPersistedDatabaseView spawnChild(String name) throws DatabasePersistException {
		return spawnChild(name, null);
	}

	@Override
	public NonPersistedDatabaseView spawnChild(String name, String value)
			throws DatabasePersistException {
		return new NonPersistedDatabaseView(this, name, value);
	}

	@Override
	public String get(String key) throws DatabaseReadException {
		return view.get(key);
	}

	@Override
	public void put(String key, String value) throws DatabasePersistException {
		SingleTable.insert(table, scope, null, key, value);
		view.put(key, value);
	}
	
	@Override
	public String toString() {
		return view.toString();
	}
}
