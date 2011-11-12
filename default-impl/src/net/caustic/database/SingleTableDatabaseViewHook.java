package net.caustic.database;

import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;

/**
 * This implementation of {@link DatabaseView} writes to a {@link SingleTable}
 * but does not read from it.  Instead, it wraps around an {@link InMemoryDatabaseView}
 * for read operations.
 * @author talos
 *
 */
class SingleTableDatabaseViewHook implements DatabaseListener {
	private final WritableTable table;
	private final ScopeFactory idFactory;
	private final Scope scope;
	private final Scope parentScope;

	public SingleTableDatabaseViewHook(WritableTable table, ScopeFactory idFactory) {
		this.table = table;
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.parentScope = null;
	}
	
	public SingleTableDatabaseViewHook(WritableTable table, ScopeFactory idFactory, Scope parentScope) {
		this.table = table;
		this.idFactory = idFactory;
		this.scope = idFactory.get();
		this.parentScope = parentScope;
	}
	
	@Override
	public void put(String name, String value) throws DatabaseListenerException {
		try {
			SingleTable.insert(table, scope, parentScope, name, value);
		} catch(TableManipulationException e) {
			throw new DatabaseListenerException("Couldn't add row to single table.", e);
		}
	}

	@Override
	public void spawnChild(String name, DatabaseView child) throws DatabaseListenerException {
		try {
			SingleTable.insert(table, scope, parentScope, name, null);
			child.addListener(new SingleTableDatabaseViewHook(table, idFactory, scope));
		} catch(TableManipulationException e) {
			throw new DatabaseListenerException("Couldn't add row to single table.", e);
		}
	}

	@Override
	public void spawnChild(String name, String value, DatabaseView child) throws DatabaseListenerException {
		try {
			SingleTable.insert(table, scope, parentScope, name, value);		
			child.addListener(new SingleTableDatabaseViewHook(table, idFactory, scope));		
		} catch(TableManipulationException e) {
			throw new DatabaseListenerException("Couldn't add row to single table.", e);
		}
	}
}
