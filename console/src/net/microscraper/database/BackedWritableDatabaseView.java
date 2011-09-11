package net.microscraper.database;

import java.io.IOException;

import net.microscraper.console.UUID;

/**
 * A {@link DatabaseView} backed by another {@link DatabaseView}, most likely an
 * in-memory {@link HashtableDatabaseView}.
 * @author talos
 *
 */
public class BackedWritableDatabaseView implements DatabaseView {
	private final WritableDatabase db;
	private final UUID id;
	private final DatabaseView view;
	
	public BackedWritableDatabaseView(DatabaseView view, WritableDatabase db, UUID id) {
		this.db = db;
		this.id = id;
		this.view = view;
	}
	
	@Override
	public DatabaseView spawnChild(String name) throws TableManipulationException {
		UUID childId = db.insertOneToMany(id, name);
		return new BackedWritableDatabaseView(view, db, childId);
	}

	@Override
	public DatabaseView spawnChild(String name, String value) throws TableManipulationException {
		UUID childId = db.insertOneToMany(id, name, value);
		return new BackedWritableDatabaseView(view, db, childId);
	}
	
	@Override
	public String get(String key) {
		return view.get(key);
	}
	
	@Override
	public void put(String key, String value) throws IOException {
		view.put(key, value);
		db.insertOneToOne(id, key, value);
	}
	
	@Override
	public String toString() {
		return view.toString();
	}
}
