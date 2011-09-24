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
class NonPersistedDatabaseViewHook implements DatabaseViewHook {
	private final WritableTable table;
	private final UUIDFactory idFactory;
	private final UUID scope;
	private final UUID parentScope;
	
	public NonPersistedDatabaseViewHook(WritableTable table, UUIDFactory idFactory, UUID parentScope) {
		this.table = table;
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.parentScope = parentScope;
	}
	
	@Override
	public void put(String name, String value) {
		SingleTable.insert(table, scope, parentScope, name, value);
	}

	@Override
	public void spawnChild(String name, DatabaseView child) {
		SingleTable.insert(table, scope, parentScope, name, null);		
		child.addHook(new NonPersistedDatabaseViewHook(table, idFactory, scope));
	}

	@Override
	public void spawnChild(String name, String value, DatabaseView child) {
		SingleTable.insert(table, scope, parentScope, name, value);		
		child.addHook(new NonPersistedDatabaseViewHook(table, idFactory, scope));		
	}
}
