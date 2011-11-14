package net.caustic.database;

import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

class MultiTableDatabaseListener implements DatabaseListener {
	private final MultiTableDatabase db;
	
	MultiTableDatabaseListener(MultiTableDatabase db) {
		this.db = db;
	}
	
	public void put(Scope scope, String key, String value)
			throws DatabaseListenerException {
		try {
			db.insert(scope, key, value);
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Couldn't store in table", e);
		}
	}

	public void newScope(Scope scope) throws DatabaseListenerException { 
		try {
			db.insertLink(null, scope, MultiTableDatabase.DEFAULT_TABLE, null);
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Could not initialize blank scope " + StringUtils.quote(scope), e);
		}
	}

	public void newScope(Scope parent, String key, Scope child)
			throws DatabaseListenerException {
		newScope(parent, key, null, child);
	}

	public void newScope(Scope parent, String key, String value, Scope child)
			throws DatabaseListenerException {
		// add to links
		try {
			db.insertLink(parent, child, key, value);				
		} catch(DatabaseException e) {
			throw new DatabaseListenerException("Couldn't create link", e);
		}
	}
}
