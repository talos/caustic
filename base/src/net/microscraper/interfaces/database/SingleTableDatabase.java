package net.microscraper.interfaces.database;

import net.microscraper.executable.Result;

/**
 * An abstract implementation of {@link Database} whose subclasses store
 * all {@link Result}s in a single table.
 * @author talos
 *
 */
public abstract class SingleTableDatabase implements Database {
	
	/**
	 * Name of the one {@link AllResultsTable} in {@link SingleTableDatabase}.
	 */
	private static final String TABLE_NAME = "results";
	
	/**
	 * Name of column to hold value for {@link Result#getId()}
	 * from {@link Result#getSource()}.
	 */
	private static final String SOURCE_ID = "source_id";
	
	/**
	 * Name of column to hold value for {@link Result#getName()}.
	 */
	private static final String NAME = "name";
	
	/**
	 * Name of column to hold value for {@link Result#getValue()}.
	 */
	private static final String VALUE = "value";
	
	/**
	 * The {@link AllResultsTable} used by this {@link SingleTableDatabase}.
	 */
	private AllResultsTable table;
	/*
	 * Sensical indexes:
	 * CREATE UNIQUE INDEX uri_number ON results (uri, number);
	 * CREATE INDEX source_uri_source_number ON results (source_uri, source_number);
	 * CREATE INDEX name ON results (name);
	 * 
	 */
	
	/**
	 * Create {@link #table} and generate its columns.
	 * @throws DatabaseException If the {@link #table} cannot be created.
	 */
	public final void open() throws DatabaseException {
		table = getAllResultsTable();
	}

	public final Result store(String name, String value)
			throws DatabaseException {
		return table.insert(name, value);
	}
	
	public final Result store(Result source, String name, String value)
			throws DatabaseException {
		return table.insert(source, name, value);
	}
	
	protected abstract AllResultsTable getAllResultsTable();
}
