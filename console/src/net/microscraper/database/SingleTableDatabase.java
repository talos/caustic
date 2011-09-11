package net.microscraper.database;

import java.io.IOException;

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
	public void open() throws IOException {
		this.connection.open();
		this.table = SingleTable.get(connection);
	}
	
	@Override
	public DatabaseView newView() throws IOException {
		return new PersistedDatabaseView(this, idFactory.get());
	}
	
	@Override
	public String get(UUID id, String name) throws IOException {
		return SingleTable.select(table, id, name);
	}
	
	@Override
	public void insertOneToOne(UUID id, String name)
			throws TableManipulationException {
		SingleTable.insert(table, id, null, name, null);
	}

	@Override
	public void insertOneToOne(UUID id, String name, String value) throws TableManipulationException {
		SingleTable.insert(table, id, null, name, value);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name) throws TableManipulationException, IOException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public PersistedDatabaseView insertOneToMany(UUID source, String name, String value) throws TableManipulationException,
			IOException {
		UUID scope = idFactory.get();
		SingleTable.insert(table, scope, source, name, value);
		return new PersistedDatabaseView(this, scope);
	}


	@Override
	public void close() throws IOException {
		connection.close();
	}
}
