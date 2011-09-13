package net.microscraper.database;

import net.microscraper.uuid.UUIDFactory;

public class NonPersistedDatabase implements Database {
	private final WritableConnection connection;
	private final UUIDFactory idFactory;
	private WritableTable table;
	
	public NonPersistedDatabase(WritableConnection connection, UUIDFactory idFactory) {
		this.connection = connection;
		this.idFactory = idFactory;
	}
	
	@Override
	public void open() throws ConnectionException {
		connection.open();
		table = SingleTable.get(connection);
	}

	@Override
	public DatabaseView newView() throws DatabasePersistException {
		return new NonPersistedDatabaseView(idFactory, table);
	}

	@Override
	public void close() throws ConnectionException {
		connection.close();
	}

}
