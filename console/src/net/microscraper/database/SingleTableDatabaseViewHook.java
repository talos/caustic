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
class SingleTableDatabaseViewHook implements DatabaseViewListener {
	private final WritableTable table;
	private final UUIDFactory idFactory;
	private final UUID scope;
	private final UUID parentScope;

	public SingleTableDatabaseViewHook(WritableTable table, UUIDFactory idFactory) {
		this.table = table;
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.parentScope = null;
	}
	
	public SingleTableDatabaseViewHook(WritableTable table, UUIDFactory idFactory, UUID parentScope) {
		this.table = table;
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.parentScope = parentScope;
	}
	
	@Override
	public void put(String name, String value) throws DatabaseViewHookException {
		try {
			SingleTable.insert(table, scope, parentScope, name, value);
		} catch(TableManipulationException e) {
			throw new DatabaseViewHookException("Couldn't add row to single table.", e);
		}
	}

	@Override
	public void spawnChild(String name, DatabaseView child) throws DatabaseViewHookException {
		try {
			SingleTable.insert(table, scope, parentScope, name, null);
			child.addListener(new SingleTableDatabaseViewHook(table, idFactory, scope));
		} catch(TableManipulationException e) {
			throw new DatabaseViewHookException("Couldn't add row to single table.", e);
		}
	}

	@Override
	public void spawnChild(String name, String value, DatabaseView child) throws DatabaseViewHookException {
		try {
			SingleTable.insert(table, scope, parentScope, name, value);		
			child.addListener(new SingleTableDatabaseViewHook(table, idFactory, scope));		
		} catch(TableManipulationException e) {
			throw new DatabaseViewHookException("Couldn't add row to single table.", e);
		}
	}
}
