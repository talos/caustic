package net.microscraper.database;

import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

public class SingleTableDatabase implements PersistedDatabase {
	private final UUIDFactory idFactory;
	private final IOConnection connection;
	private IOTable table;
	
	public SingleTableDatabase(IOConnection connection, UUIDFactory idFactory) {
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
		return new PersistedDatabaseView(this, idFactory.get());
	}
	
	@Override
	public String get(UUID id, String name) throws DatabaseReadException {
		return SingleTable.select(table, id, name);
	}
	
	@Override
	public void insertOneToOne(UUID id, String name) throws DatabasePersistException {
		SingleTable.update(table, id, null, name, null);
	}

	@Override
	public void insertOneToOne(UUID id, String name, String value) throws DatabasePersistException {
		SingleTable.update(table, id, null, name, value);
	}
	
	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name) throws DatabasePersistException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value)
			throws DatabasePersistException  {
		UUID scope = idFactory.get();
		SingleTable.update(table, scope, source, name, value);
		return new PersistedDatabaseView(this, scope);
	}
	
	@Override
	public void close() throws DatabaseException {
		connection.close();
	}
}
