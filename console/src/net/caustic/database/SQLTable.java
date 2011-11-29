package net.caustic.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.caustic.database.Table;
import net.caustic.database.TableReadException;
import net.caustic.scope.Scope;
import net.caustic.util.StringUtils;

/**
 * A SQL implementation of {@link Updateable} using {@link SQLConnection}.
 * 
 * @author talos
 *
 */
class SQLTable implements Table {
	
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

	/**
	 * 
	 * @param whereMap a parameterized WHERE clause, where scope is the first WHERE.
	 * @return
	 */
	private String buildWhereClause(Set<String> whereColumns) {
		StringBuffer buf = new StringBuffer("WHERE `" + connection.getScopeColumnName() + "` = ?");
		for(String whereColumn : whereColumns) {
			buf.append(" AND `" + whereColumn + "` = ? ");
		}
		return buf.toString();
	}
	
	public SQLTable(SQLConnection connection, String name) throws SQLConnectionException {
		this.name = name;
		this.connection = connection;
	}
	
	@Override
	public void addColumn(String columnName) throws TableManipulationException {
		preventIllegalBacktick(columnName);
		
		try {
			connection.executeNow(
							"ALTER TABLE `" + name + "` " +
							" ADD COLUMN `" + columnName + "`" + 
							connection.textColumnType());
		} catch(SQLConnectionException e) {
			throw new TableManipulationException("Couldn't add column " + 
					StringUtils.quote(columnName) + " to " + StringUtils.quote(name), e);
		}
	}
	
	@Override
	public boolean hasColumn(String columnName) throws TableReadException {
		try {
			return connection.doesTableHaveColumn(name, columnName);
		} catch(SQLConnectionException e) {
			throw new TableReadException("Error determining whether table " +
					StringUtils.quote(name) + " has column " + StringUtils.quote(columnName), e);
		}
	}
	
	@Override
	public void insert(Scope scope, Map<String, String> map) throws TableManipulationException {
		
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
	public void update(Scope scope, Map<String, String> whereMap, Map<String, String> updateMap)
			throws TableManipulationException {
		if(updateMap.size() == 0) {
			throw new TableManipulationException("Must provide values to update.");
		}
		
		StringBuffer setBuf = new StringBuffer(" SET ");
		for(Map.Entry<String, String> entry : updateMap.entrySet()) {
			setBuf.append("`" + entry.getKey() + "` = ? ,");
		}
		String set = setBuf.substring(0, setBuf.length() -1); // clip trailing comma
		
		List<String> params = new ArrayList<String>();
		params.addAll(updateMap.values());
		params.add(scope.asString()); // this is an extra where clause before the others.
		params.addAll(whereMap.values());
		
		String sql = " UPDATE `" + name + "` " + set + " " + buildWhereClause(whereMap.keySet());
		
		try {
			connection.batchModify(sql,
					params.toArray(new String[params.size()]));
		} catch (SQLConnectionException e) {
			throw new TableManipulationException("Could not update with " + sql, e);
		}
	}
	
	@Override
	public List<Map<String, String>> select(Scope scope, Map<String, String> whereMap,
			String[] columnNames) throws TableReadException {
		try  {
			StringBuffer columnsClauseBuf = new StringBuffer();
			for(String columnName : columnNames) {
				columnsClauseBuf.append(", `"+ columnName + "` ");
			}
			// always select the scope column
			String sql = "SELECT `" + connection.getScopeColumnName() + "` "
					 + columnsClauseBuf + " FROM `" + name + "` " + buildWhereClause(whereMap.keySet());
			List<String> params = new ArrayList<String>();
			params.add(scope.asString());
			params.addAll(whereMap.values());
			
			return connection.select(sql, columnNames, params.toArray(new String[params.size()]));
		} catch (SQLConnectionException e) {
			throw new TableReadException("Error inserting " + Arrays.asList(columnNames) + " into " + name, e);
		}
	}
}
