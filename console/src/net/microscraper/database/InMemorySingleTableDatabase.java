package net.microscraper.database;

import net.microscraper.uuid.UUIDFactory;

/**
 * A {@link Database} implementation that creates a new {@link InMemoryDatabaseView}
 * for each call of {@link #newView()}.
 * @author talos
 *
 */
public class InMemorySingleTableDatabase implements Database {
	private final WritableConnection connection;
	private WritableTable table;
	private UUIDFactory idFactory;
	
	public InMemorySingleTableDatabase(WritableConnection connection,
			UUIDFactory idFactory) {
		this.connection = connection;
		this.idFactory = idFactory;
	}
	
	@Override
	public void open() throws DatabaseException, ConnectionException { 
		connection.open();
		table = SingleTable.get(connection);
	}

	@Override
	public DatabaseView newView() throws DatabaseException {
		DatabaseView view = new InMemoryDatabaseView();
		view.addListener(new SingleTableDatabaseViewHook(table, idFactory));
		return view;
	}

	@Override
	public void close() throws DatabaseException, ConnectionException {
		connection.close();
	}

}
