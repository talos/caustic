package net.microscraper.database;

import java.io.IOException;

import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

public class SingleTableWritableDatabase extends SingleTableDatabase implements
		WritableDatabase {
	
	private final WritableConnection connection;
	private WritableTable table;
	private final UUIDFactory idFactory;
	
	public SingleTableWritableDatabase(WritableConnection connection, UUIDFactory idFactory) {
		this.connection = connection;
		this.idFactory = idFactory;
	}
	
	@Override
	public void open() throws IOException {
		table = connection.newWritable(TABLE_NAME, COLUMN_NAMES);
		connection.open();
	}

	@Override
	public DatabaseView newView() throws IOException {
		return new WritableDatabaseView(new HashtableDatabaseView(), this, idFactory.get());
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}

	@Override
	public void insertOneToOne(UUID id, String name)
			throws TableManipulationException {
		insertOneToOne(id, name, null);
	}

	@Override
	public void insertOneToOne(UUID id, String name, String value)
			throws TableManipulationException {
		insert(table, id, null, name, value);
	}

	@Override
	public UUID insertOneToMany(UUID source, String name)
			throws TableManipulationException {
		return insertOneToMany(source, name, null);
	}

	@Override
	public UUID insertOneToMany(UUID source, String name, String value)
			throws TableManipulationException {
		UUID childId = idFactory.get();
		insert(table, childId, source, name, value);
		return childId;
	}

}
