package net.microscraper.database;

import java.io.IOException;

import net.microscraper.uuid.UUID;

public class IODatabaseView implements DatabaseView {
	
	private final String viewName;
	private final UUID id;
	private final IODatabase database;
	
	protected IODatabaseView(IODatabase database,
			String viewName, UUID id) {
		this.database = database;
		this.viewName = viewName;
		this.id = id;
	}
	
	@Override
	public DatabaseView spawnChild(String name) throws IOException {
		return database.insertOneToMany(id, viewName, name);
	}

	@Override
	public DatabaseView spawnChild(String name, String value)
			throws IOException {
		return database.insertOneToMany(id, viewName, name, value);
	}

	@Override
	public String get(String key) {
		return database.get(viewName, id, key);
	}

	@Override
	public void put(String key, String value) throws IOException {
		database.insertOneToOne(id, viewName, key, value);
	}
}
