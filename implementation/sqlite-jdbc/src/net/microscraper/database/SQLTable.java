package net.microscraper.database;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import net.microscraper.util.UUID;

import net.microscraper.database.Updateable;
import net.microscraper.util.StringUtils;

/**
 * A SQL implementation of {@link Updateable} using {@link SQLConnection}.
 * 
 * @author talos
 *
 */
public class SQLTable implements Updateable {
	
	/**
	 * {@link SQLConnection} used in this {@link SQLTable}.
	 */
	private final SQLConnection connection;
	
	/**
	 * {@link String} name of the table in SQL.
	 */
	private final String name;

	/**
	 * The names of all this table's columns.
	 */
	private final List<String> columns = new ArrayList<String>();
	
	public SQLTable(SQLConnection connection, String name,
			String[] columns) throws SQLConnectionException {
				
		preventIllegalBacktick(name);
		this.connection = connection;
		this.name = name;
		
		String[] columnDefinitions = new String[columns.length];
		for(int i = 0 ; i < columns.length ; i ++) {
			columnDefinitions[i] = columns[i] + " " + connection.textColumnType();
			this.columns.add(columns[i]);
		}
		String columnDefinition = StringUtils.join(columnDefinitions, " , ");
		
		if(connection.tableExists(name)) {
			
		} else {
			SQLPreparedStatement createTable = 
					this.connection.prepareStatement("CREATE TABLE `" + name + "` (" +
		/*			+ " (`" + ID_COLUMN_NAME + "` " + connection.intColumnType() +
					" " + connection.keyColumnDefinition() + ", " + */
					columnDefinition + ")");
			createTable.execute();
			connection.runBatch();
		}
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
	
	@Override
	public boolean hasColumn(String columnName) {
		return columns.contains(columnName);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void insert(Hashtable map) throws TableManipulationException {
		String[] columnNames = new String[map.size()];
		String[] parameters = new String[map.size()];
		String[] columnValues = new String[map.size()];
		
		int i = 0;
		Enumeration enumeration = map.keys();
		while(enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			String value =(String) map.get(name);
			columnNames[i] = "`" + name + "`";
			parameters[i] = "?";
			columnValues[i] = value;
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

	@SuppressWarnings("rawtypes")
	@Override
	public void update(String idColumnName, UUID id, Hashtable map)
			throws TableManipulationException {
		String[] setStatements = new String[map.size()];
		String[] values = new String[map.size() + 1];
		
		Enumeration enumeration = map.keys();
		int i = 0;
		while(enumeration.hasMoreElements()) {
			String name = (String) enumeration.nextElement();
			setStatements[i] = "`" + name + "` = ? ";
			values[i] = (String) map.get(name);
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
		return columns.toArray(new String[0]);
	}

	@Override
	public void drop() throws TableManipulationException {
		try {
			SQLPreparedStatement drop = connection.prepareStatement(
					"DROP TABLE `" + name + "`");
			drop.execute();
			connection.runBatch();
		} catch(SQLConnectionException e) {
			throw new TableManipulationException(e);
		}
	}
}
