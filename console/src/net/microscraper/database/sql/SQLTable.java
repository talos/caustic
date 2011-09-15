package net.microscraper.database.sql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.database.IOTable;
import net.microscraper.database.IOTableReadException;
import net.microscraper.database.TableManipulationException;
import net.microscraper.util.StringUtils;
import net.microscraper.uuid.UUID;

/**
 * A SQL implementation of {@link Updateable} using {@link SQLConnection}.
 * 
 * @author talos
 *
 */
class SQLTable implements IOTable {
	
	/**
	 * {@link SQLConnection} used in this {@link SQLTable}.
	 */
	private final SQLConnection connection;
	
	/**
	 * {@link String} name of the table in SQL.
	 */
	private final String name;
	
	/**
	 * Check a {@link String} for backticks, which cause problems in column or
	 * table names.
	 * @param stringToCheck The {@link String} to check.
	 * @throws IllegalArgumentException If <code>stringToCheck</code>
	 * contains a backtick.
	 */
	private void preventIllegalBacktick(String stringToCheck) throws IllegalArgumentException {
		if(stringToCheck.indexOf('`') != -1 ) {
			throw new IllegalArgumentException("Illegal name for SQL " +
					StringUtils.quote(stringToCheck) +
					" to  because it contains a backtick at index " +
				Integer.toString(stringToCheck.indexOf('`')));
		}
	}
	
	private String getSelectQuery(UUID scope, String[] columnNames)  throws IOException {
		return "SELECT `" + StringUtils.join(columnNames, "`, `") + "` " +
					"FROM `" + name + "` " +
					"WHERE `" + connection.getScopeColumnName() + "` = ?";
	}
	
	public SQLTable(SQLConnection connection, String name) throws SQLConnectionException {
		this.name = name;
		this.connection = connection;
	}
	
	@Override
	public void addColumn(String columnName) throws TableManipulationException {
		preventIllegalBacktick(columnName);
		
		try {
			String type = connection.textColumnType();
			connection.executeNow(
							"ALTER TABLE `" + name + "` " +
							" ADD COLUMN `" + columnName + "`" + 
							type);
		} catch(SQLConnectionException e) {
			throw new TableManipulationException(e.getMessage());
		}
	}
	
	@Override
	public boolean hasColumn(String columnName) throws IOTableReadException {
		try {
			return connection.doesTableHaveColumn(name, columnName);
		} catch(SQLConnectionException e) {
			throw new IOTableReadException(e);
		}
	}
	
	@Override
	public void insert(UUID scope, Map<String, String> map) throws TableManipulationException {
		
		String[] columnNames = new String[map.size() + 1];
		String[] parameters = new String[map.size() + 1];
		String[] columnValues = new String[map.size() + 1];
		
		int i = 1;
		for(Map.Entry<String, String> entry : map.entrySet()) {
			columnNames[i] = "`" + entry.getKey() + "`";
			parameters[i] = "?";
			columnValues[i] = entry.getValue();
			i++;
		}
		columnNames[0] = connection.getScopeColumnName();
		parameters[0] = "?";
		columnValues[0] = scope.asString();
		
		try {
			connection.batchModify("INSERT INTO `" + name + "` " +
							"(" + StringUtils.join(columnNames, ", ") + ") " +
							"VALUES (" + StringUtils.join(parameters, ", ") + ")",
							columnValues);
		} catch(SQLConnectionException e) {
			throw new TableManipulationException(e.getMessage());
		}
	}

	@Override
	public void update(UUID scope, Map<String, String> map)
			throws TableManipulationException {
		if(map.size() == 0) {
			throw new TableManipulationException("Must provide values to update.");
		}
		
		String[] setStatements = new String[map.size()];
		String[] values = new String[map.size() + 1]; // extra value for scope
		
		int i = 0;
		for(Map.Entry<String, String> entry : map.entrySet()) {
			setStatements[i] = "`" + entry.getKey() + "` = ? ";
			values[i] = entry.getValue();
			i++;
		}
		
		// bind the very last parameter to scope
		values[values.length - 1] = scope.asString();
		
		String set = " SET " + StringUtils.join(setStatements, ", ");
		
		try {
			connection.batchModify(" UPDATE `" + name + "` " + set +
					" WHERE `" + connection.getScopeColumnName() + "` = ?", values);
		} catch (SQLConnectionException e) {
			throw new TableManipulationException(e.getMessage());
		}
	}

	@Override
	public List<Map<String, String>> select(UUID scope, String[] columnNames) throws IOTableReadException {
		try  {
			return connection.select(getSelectQuery(scope, columnNames), columnNames);
		} catch(IOException e) {
			throw new IOTableReadException(e);
		} catch (SQLConnectionException e) {
			throw new IOTableReadException(e);
		}
	}
}
