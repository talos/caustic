package net.microscraper.database;

import net.microscraper.uuid.UUIDFactory;

public class NonPersistedDatabase implements Database {
	private final WritableConnection connection;
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
		DatabaseView view = new InMemoryDatabaseView();
		view.addHook(new NonPersistedDatabaseViewHook(table, idFactory));
		return view;
		//return new NonPersistedDatabaseView(idFactory, table);
	}

	@Override
	public void close() throws ConnectionException {
		connection.close();
	}

	/**
	 * The <code>toString</code> method of {@link Connection}.
	 */
	@Override
	public String toString() {
		return connection.toString();
	}
}
