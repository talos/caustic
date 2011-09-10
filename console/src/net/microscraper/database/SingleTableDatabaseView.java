package net.microscraper.database;

import net.microscraper.console.UUID;
import net.microscraper.console.UUIDFactory;

public class SingleTableDatabaseView implements DatabaseView {
	private final SingleTableDatabase db;
	private final UUID id;
	private final UUIDFactory idFactory;
	private final DatabaseView view;
	
	public SingleTableDatabaseView(DatabaseView view, UUIDFactory idFactory, SingleTableDatabase db) {
		this.db = db;
		this.id = idFactory.get();
		this.idFactory = idFactory;
		this.view = view;
	}
	
	@Override
	public DatabaseView spawnChild(String name) throws TableManipulationException {
		SingleTableDatabaseView child = new SingleTableDatabaseView(view, idFactory, db);
		db.insert(child.id, id, name, null);
		return child;
	}

	@Override
	public DatabaseView spawnChild(String name, String value) throws TableManipulationException {
		SingleTableDatabaseView child = new SingleTableDatabaseView(view, idFactory, db);
		db.insert(child.id, id, name, value);
		return child;
	}
	
	@Override
	public String get(String key) {
		return view.get(key);
	}
	
	@Override
	public void put(String key, String value) throws TableManipulationException {
		db.insert(id, null, key, value);
	}
	
	@Override
	public String toString() {
		return view.toString();
	}
}
