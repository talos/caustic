package net.microscraper.interfaces.sql;

import net.microscraper.executable.Result;
import net.microscraper.interfaces.database.DatabaseException;

public final class SQLSingleTable extends SQLTable {
	
	public SQLSingleTable(SQLConnection connection) {
		super(connection);
	}
	
	@Override
	public Result insertRow(String name, String value) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateRow(Result result, String name, String value)
			throws DatabaseException {
		// TODO Auto-generated method stub

	}

}
