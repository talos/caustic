package net.microscraper.database.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import net.microscraper.database.IOTable;
import net.microscraper.database.TableManipulationException;
import net.microscraper.util.StringUtils;
import net.microscraper.uuid.UUID;

/**
 * A SQL implementation of {@link Updateable} using {@link SQLConnection}.
 * 
 * @author talos
 *
 */
public class SQLTable implements IOTable {
	
	/**
	 * {@link SQLConnection} used in this {@link SQLTable}.
	 */
	private final SQLConnection connection;
	
	/**
	 * {@link String} name of the table in SQL.
	 */
	private final String name;

	private final String idColumnName;
	
	/**
	 * The names of all this table's columns.
	 */
	private final List<String> columns = Collections.synchronizedList(new ArrayList<String>());

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

	public SQLResultSet getResultSet(String id, String[] columnNames) {
		try {
			SQLPreparedStatement select = connection.prepareStatement(
					"SELECT `" + StringUtils.join(columnNames, "`, `") + "` " +
					"WHERE `" + idColumnName + "` = ?");
			select.bindStrings(new String[] { id });
			return select.executeQuery();
		} catch(SQLConnectionException e) {
			throw new RuntimeException(); // TODO
		}
	}
	
	public SQLTable(SQLConnection connection, String name, String idColumnName,
			String[] columns) throws SQLConnectionException {
		
		preventIllegalBacktick(name);
		this.idColumnName = idColumnName;
		this.connection = connection;
		this.name = name;
		
		String[] columnDefinitions = new String[columns.length + 1];
		for(int i = 1 ; i < columns.length + 1; i ++) {
			columnDefinitions[i] = columns[i] + " " + connection.textColumnType();
			this.columns.add(columns[i]);
		}
		this.columns.add(idColumnName);
		columnDefinitions[0] = idColumnName + " " + connection.textColumnType();
		String columnDefinition = StringUtils.join(columnDefinitions, " , ");
		
		SQLPreparedStatement createTable = 
				this.connection.prepareStatement("CREATE TABLE `" + name + "` (" +
				columnDefinition + ")");
		createTable.execute();
		connection.runBatch();
	}
	
	@Override
	public void addColumn(String columnName) throws TableManipulationException {
		preventIllegalBacktick(columnName);
		
		try {
			SQLPreparedStatement alterTable = 
					connection.prepareStatement("" +
							"ALTER TABLE `" + name + "` " +
							" ADD COLUMN `" + columnName + "`" + 
							connection.textColumnType());
			alterTable.execute();
			connection.runBatch();
			columns.add(columnName);
		} catch(SQLConnectionException e) {
			throw new TableManipulationException(e);
		}
	}
	
	@Override
	public boolean hasColumn(String columnName) {
		return columns.contains(columnName);
	}
	
	@Override
	public void insert(UUID id, Map<String, String> map) throws TableManipulationException {
		String[] columnNames = new String[map.size()];
		String[] parameters = new String[map.size()];
		String[] columnValues = new String[map.size()];
		
		int i = 0;
		for(Map.Entry<String, String> entry : map.entrySet()) {
			columnNames[i] = "`" + entry.getKey() + "`";
			parameters[i] = "?";
			columnValues[i] = entry.getValue();
			i++;
		}
		
		try {
			SQLPreparedStatement insert = connection.prepareStatement(
					"INSERT INTO `" + name + "` " +
							"(" + StringUtils.join(columnNames, ", ") + ") " +
							"VALUES (" + StringUtils.join(parameters, ", ") + ")");
			insert.bindStrings(columnValues);
			insert.execute();
		} catch(SQLConnectionException e) {
			throw new TableManipulationException(e);
		}
	}

	@Override
	public void update(UUID id, Map<String, String> map)
			throws TableManipulationException {
		String[] setStatements = new String[map.size()];
		String[] values = new String[map.size() + 1];
		
		int i = 0;
		for(Map.Entry<String, String> entry : map.entrySet()) {
			setStatements[i] = "`" + entry.getKey() + "` = ? ";
			values[i] = entry.getValue();
		}
		values[values.length - 1] = id.asString();
		
		String set = " SET " + StringUtils.join(setStatements, ", ");
		
		try {
			SQLPreparedStatement update = connection.prepareStatement(
					"UPDATE `" + name + "` " + set +
					"WHERE `" + idColumnName + "` = ?");
			update.bindStrings(values);
			update.execute();
		} catch (SQLConnectionException e) {
			throw new TableManipulationException(e);
		}
	}

	@Override
	public String[] getColumnNames() {
		synchronized(columns) {
			return columns.toArray(new String[0]);
		}
	}

	@Override
	public List<Map<String, String>> select(String id, String[] columnNames) {
		try  {
			SQLResultSet rs = getResultSet(id, columnNames);
			
			List<Map<String, String>> results = new ArrayList<Map<String, String>>();
			while(rs.next()) {
				Map<String, String> map = new HashMap<String, String>();
				for(String columnName : columnNames) {
					map.put(columnName, rs.getString(columnName));
				}
				results.add(map);
			}
			return results;
		} catch(SQLConnectionException e) {
			throw new RuntimeException(e); //TODO
		}
	}

	@Override
	public List<String> select(String id, String columnName) {
		try  {
			SQLResultSet rs = getResultSet(id, new String[] { columnName} );
			
			List<String> results = new ArrayList<String>();
			while(rs.next()) {
				results.add(rs.getString(columnName));
			}
			return results;
		} catch(SQLConnectionException e) {
			throw new RuntimeException(e); //TODO
		}
		
	}
}
