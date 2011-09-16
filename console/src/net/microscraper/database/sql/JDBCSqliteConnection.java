package net.microscraper.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.microscraper.database.ConnectionException;
import net.microscraper.database.IOTable;

/**
 * An implementation of {@link SQLConnection} for org.sqlite.JDBC
 * @see SQLConnection
 * @author realest
 *
 */
public class JDBCSqliteConnection implements SQLConnection {
	public static final int CACHE_SIZE = 100;
	
	private Connection connection;
	private final String connectionPath;
	
	/**
	 * Cache to keep track of Prepared SQL statements by their raw SQL string.  Implements
	 * pruning by least-recent access.
	 * @author realest
	 *
	 */
	private static final class StatementCache<T extends Statement>
					extends LinkedHashMap<String, T> {
		private static final long serialVersionUID = 2660881239439328558L;
		public StatementCache() {
			super(CACHE_SIZE, 1, true);
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
			T stmt = eldest.getValue();
			if(size() > CACHE_SIZE) {
				try {
					stmt.executeBatch();
					stmt.close();
					return true;
				} catch(SQLException e) {
					e.printStackTrace(); // TODO
					return false;
				}
			} else {
				return false;
			}
		}
	}
	
	private final Map<String, PreparedStatement> prepSelects =
			new StatementCache<PreparedStatement>();
	private final Map<String, PreparedStatement> prepMods =
			new StatementCache<PreparedStatement>();
	private PreparedStatement tableExistsStmt;
	
	private final String scopeColumnName;
	
	/**
	 * Check to see whether a table exists using the reused {@link #tableExistsStmt}
	 * statement.  Commits before, in case a table was created.
	 * @param tableName {@link String} name of table to check.
	 * @return <code>True</code> if the table exists, <code>false</code> otherwise.
	 * @throws SQLConnectionException
	 */
	private boolean tableExists(String tableName) throws SQLConnectionException {
		try {
			commit();
			tableExistsStmt.setString(1, tableName);
			ResultSet result = tableExistsStmt.executeQuery();
			boolean tableExists = result.next();
			result.close();
			return tableExists;
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	/**
	 * Remember to close the returned result set! Don't have to worry about closing
	 * the statement because it's part of the cache.
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	private ResultSet getResultSet(String sql, String[] parameters) throws SQLException {
		PreparedStatement stmt;
		synchronized(prepSelects) {
			if(prepSelects.containsKey(sql)) {
				stmt = prepSelects.get(sql);
			} else {
				stmt = connection.prepareStatement(sql);
				prepSelects.put(sql, stmt);
			}
		}
		setParams(stmt, parameters);
		//stmt.addBatch();
		System.out.println("About to retrieve resultSet");
		return stmt.executeQuery();
	}
	
	private JDBCSqliteConnection(String connectionPath, String scopeColumnName) {
		this.scopeColumnName = scopeColumnName;
		this.connectionPath = connectionPath;
	}

	@Override
	public void commit() throws SQLConnectionException {
		try {
			/*for(PreparedStatement stmt : prepMods.values()) {
				stmt.executeBatch();
			}*/
			synchronized(prepMods) {
				for(Map.Entry<String, PreparedStatement> entry : prepMods.entrySet()) {
					int[] updateCounts = entry.getValue().executeBatch();
					for(int updateCount : updateCounts) {
						System.out.println(entry.getKey() + " modified " + updateCount + " rows." );
					}
				}
			}
			synchronized(connection) {
				System.out.println("committing...");
				connection.commit();
				System.out.println("finished committing.");
			}
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public String keyColumnDefinition() {
		return "PRIMARY KEY";
	}
	
	@Override
	public String varcharColumnType() {
		return "VARCHAR";
	}

	@Override
	public String textColumnType() {
		return "TEXT";
	}
	
	@Override
	public String intColumnType() {
		return "INTEGER";
	}

	@Override
	public String nullValue() {
		return "NULL";
	}

	@Override
	public int defaultVarcharLength() {
		return (int) Math.pow(10, 9);
	}
	
	/**
	 * Open {@link JDBCSqliteConnection} using org.sqlite.JDBC.
	 */
	@Override
	public void open() throws ConnectionException {
		try {
			Class.forName("org.sqlite.JDBC"); // Make sure we have this class.
			connection = DriverManager.getConnection(connectionPath);
			connection.setAutoCommit(false);
			tableExistsStmt =
					connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?;");
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		} catch(ClassNotFoundException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public IOTable newIOTable(String name, String[] columnNames)
			throws ConnectionException {
		String definitionStr = "`" + scopeColumnName + "` " + textColumnType();
		for(String columnName : columnNames) {
			definitionStr += ", `" + columnName + "` " + textColumnType();
		}
		
		executeNow("CREATE TABLE `" + name + "` (" + definitionStr + ")");
		
		return new SQLTable(this, name);
	}

	@Override
	public void close() throws ConnectionException {
		try {
			commit();
			connection.close();
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public IOTable getIOTable(String name) throws ConnectionException {
		if(tableExists(name)) {
			return new SQLTable(this, name);
		} else {
			return null;
		}
	}

	@Override
	public String getScopeColumnName() {
		return scopeColumnName;
	}
	
	@Override
	public boolean doesTableHaveColumn(String tableName, String columnName)
				throws SQLConnectionException{
		synchronized(connection) {
			commit();
			try {
				boolean result = false;
				ResultSet rs = getResultSet("SELECT * FROM `" + tableName + "`",
						new String[] { } ); 
				ResultSetMetaData meta = rs.getMetaData();
				
				int numCol = meta.getColumnCount();
				
				for (int i = 1; i < numCol+1; i++) {
				    if(meta.getColumnName(i).equals(columnName)) {
				    	result = true;
				    }
				}
				rs.close();
				return result;
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
	}
	
	@Override
	public List<Map<String, String>> select(String sql, String[] columnNames) throws SQLConnectionException {
		return select(sql, new String[] { }, columnNames );
	}

	@Override
	public List<Map<String, String>> select(String sql, String[] columnNames, String[] parameters)
			throws SQLConnectionException {
		synchronized(connection) { // hold the connection for the whole process.
			
			
			
			System.out.println("before select commit");
			commit(); // commit any lingering changes before selecting
			System.out.println("after select commit");
			try {
				System.out.println("Selecting " + sql + " with params " + Arrays.asList(parameters));
				ResultSet rs = getResultSet(sql, parameters);
				List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
				
				while(rs.next()) {
					System.out.println("advanced through resultset.");
					Map<String, String> row = new HashMap<String, String>();
					for(String columnName : columnNames) {
						row.put(columnName, rs.getString(columnName));
					}
					rows.add(row);
				}
				rs.close();
				
				System.out.println("row results: " + rows);
				return rows;
			} catch(SQLException e) {
				throw new SQLConnectionException(e);
			}
		}
	}

	@Override
	public void executeNow(String sql) throws SQLConnectionException {
		try {
			synchronized(connection) {
				System.out.println(sql);
				commit();
				Statement stmt = connection.createStatement();
				stmt.execute(sql);
				connection.commit();
				stmt.close();
			}
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public void batchModify(String sql, String[] parameters)
			throws SQLConnectionException {
		try {
			PreparedStatement stmt;
			synchronized(prepMods) {
				if(prepMods.containsKey(sql)) {
					stmt = prepMods.get(sql);
				} else {
					stmt = connection.prepareStatement(sql);
					prepMods.put(sql, stmt);
				}
			}
			setParams(stmt, parameters);
			stmt.addBatch();	
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	/**
	 * Convenience method to bind an array of {@link String} params to
	 * a {@link PreparedStatement}.
	 * @param stmt The {@link PreparedStatement} on which to set the parameters.
	 * @param params A {@link String} list of parameters to set.
	 * @throws SQLException If the parameters cannot be set on <code>stmt</code>
	 */
	private static void setParams(PreparedStatement stmt, String[] params) throws SQLException {
		for(int i = 0 ; i < params.length ; i ++) {
			System.out.println("Setting param " + (i+1) + " to " + params[i]);
			stmt.setString(i + 1, params[i]);
		}
	}
	
	/**
	 * Produce a {@link JDBCSqliteConnection} using a path to a database.
	 * @param pathToDB {@link String} path to database.
	 * @param scopeColumnName The name of the scope column in tables.
	 */
	public static JDBCSqliteConnection toFile(String pathToDB, String scopeColumnName) {
		return new JDBCSqliteConnection("jdbc:sqlite:" + pathToDB, scopeColumnName);
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} in-memory.
	 * @param scopeColumnName The name of the scope column in tables.
	 */
	public static JDBCSqliteConnection inMemory(String scopeColumnName) {
		return new JDBCSqliteConnection("jdbc:sqlite::memory:", scopeColumnName);
	}
}
