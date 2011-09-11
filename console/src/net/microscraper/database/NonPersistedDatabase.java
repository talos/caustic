package net.microscraper.database;

import java.io.IOException;

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
	public void open() throws IOException {
		connection.open();
		table = SingleTable.get(connection);
	}

	@Override
	public DatabaseView newView() throws IOException {
		return new NonPersistedDatabaseView(idFactory, table);
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}

}
