package net.caustic.database;

import net.caustic.scope.Scope;

/**
 * This implementation of {@link DatabaseView} writes to a {@link SingleTable}.
 * @author talos
 *
 */
class SingleTableDatabaseListener implements DatabaseListener {

	private final SingleTableDatabase db;

	public SingleTableDatabaseListener(SingleTableDatabase db) {
		this.db = db;
	}

	public void onPut(Scope scope, String key, String value)
			throws DatabaseListenerException {
		try {
			db.insert(null, scope, key, value);
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Couldn't add row to single table.", e);
		}		
	}

	public void onNewDefaultScope(Scope scope) throws DatabaseListenerException { }

	public void onNewScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		onNewScope(parent, key, null, child);
	}

	public void onNewScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		try {
			db.insert(parent, child, key, value);		
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Couldn't add row to single table.", e);
		}
	}
}
