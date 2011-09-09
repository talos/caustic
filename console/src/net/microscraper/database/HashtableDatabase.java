package net.microscraper.database;

public class HashtableDatabase implements Database {

	@Override
	public void open() { }

	@Override
	public DatabaseView newView() { 
		return new HashtableDatabaseView();
	}

	@Override
	public void close() { }

}
