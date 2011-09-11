package net.microscraper.database;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.microscraper.uuid.UUID;
import net.microscraper.uuid.UUIDFactory;

public class SingleTableIODatabase extends SingleTableDatabase implements
		IODatabase {
	private final UUIDFactory idFactory;
	private final IOConnection connection;
	private IOTable table;	
	
	
	private String get(String tableName, String id, String columnName) {
		List<Map<String, String>> rows = table.select(id,
				new String[] { SOURCE_COLUMN_NAME, NAME_COLUMN_NAME, VALUE_COLUMN_NAME } );
		
		String sourceId = null;
		for(Map<String, String> row : rows) {
			if(row.get(NAME_COLUMN_NAME).equals(columnName)) {
				return row.get(VALUE_COLUMN_NAME);
			} else if(row.get(SOURCE_COLUMN_NAME) != null) {
				sourceId = row.get(SOURCE_COLUMN_NAME);
			}
		}
		if(sourceId != null) {
			return get(tableName, sourceId, columnName);
		} else {
			return null;
		}
	}
	
	public SingleTableIODatabase(IOConnection connection, UUIDFactory idFactory) {
		this.idFactory = idFactory;
		this.connection = connection;
	}

	@Override
	public void open() throws IOException {
		this.connection.open();
		this.table = connection.newIOTable(TABLE_NAME, COLUMN_NAMES);
	}

	@Override
	public DatabaseView newView() throws IOException {
		return new IODatabaseView(this, TABLE_NAME, idFactory.get());
	}
	
	@Override
	public String get(String tableName, UUID id, String columnName) {
		return get(tableName, id.asString(), columnName);
	}
	
	@Override
	public void insertOneToOne(UUID id, String resultTableName, String name)
			throws TableManipulationException {
		insert(table, id, null, name, null);
	}

	@Override
	public void insertOneToOne(UUID id, String resultTableName, String name,
			String value) throws TableManipulationException {
		insert(table, id, null, name, value);

	}

	@Override
	public DatabaseView insertOneToMany(UUID source, String resultTableName,
			String name) throws TableManipulationException, IOException {
		return insertOneToMany(source, resultTableName, name, null);
	}

	@Override
	public DatabaseView insertOneToMany(UUID source, String resultTableName,
			String name, String value) throws TableManipulationException,
			IOException {
		UUID childId = idFactory.get();
		insert(table, childId, source, name, value);
		return new IODatabaseView(this, name, childId);
	}


	@Override
	public void close() throws IOException {
		connection.close();
	}
}
