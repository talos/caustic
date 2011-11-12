package net.caustic.database;

import net.caustic.scope.Scope;
import net.caustic.scope.ScopeFactory;

public class PersistedSingleTableDatabase implements PersistedDatabase {
	private final ScopeFactory idFactory;
	private final IOConnection connection;
	private IOTable table;
	
	public PersistedSingleTableDatabase(IOConnection connection, ScopeFactory idFactory) {
		this.idFactory = idFactory;
		this.connection = connection;
	}

	@Override
	public void open() throws DatabaseException {
		this.connection.open();
		this.table = SingleTable.get(connection);
	}
	
	@Override
	public DatabaseView newView() throws DatabaseException {
		synchronized(connection) {
			return new PersistedDatabaseView(this, idFactory.get());
		}
	}
	
	@Override
	public String get(Scope id, String name) throws DatabaseReadException {
		synchronized(connection) {
			return SingleTable.select(table, id, name);
		}
	}
	
	@Override
	public void insertOneToOne(Scope id, String name) throws DatabasePersistException {
		insertOneToOne(id, name, null);
	}

	@Override
	public void insertOneToOne(Scope id, String name, String value) throws DatabasePersistException {
		synchronized(connection) {
			SingleTable.replace(table, id, null, name, value);
		}
	}
	
	@Override
	public PersistedDatabaseView insertOneToMany(Scope source, String name) throws DatabasePersistException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(Scope source, String name, String value)
			throws DatabasePersistException  {
		synchronized(connection) {
			Scope scope = idFactory.get();
			// have to insert, otherwise call to 
			SingleTable.insert(table, scope, source, name, value);
			return new PersistedDatabaseView(this, scope);
		}
	}
	
	@Override
	public void close() throws DatabaseException {
		connection.close();
	}
	
	/**
	 * The <code>toString</code> method of {@link Connection}.
	 */
	@Override
	public String toString() {
		return connection.toString();
	}
}
