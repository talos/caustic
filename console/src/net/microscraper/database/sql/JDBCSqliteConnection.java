package net.microscraper.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
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
	final int maxStatementsBeforeCommit;
	private final String connectionPath;
	
	/**
	 * Cache to keep track of Prepared SQL statements by their raw SQL string.  Implements
	 * pruning by least-recent access.
	 * @author realest
	 *
	 */
	private static final class StatementCache extends LinkedHashMap<String, PreparedStatement> {
		private static final long serialVersionUID = 2660881239439328558L;
		public StatementCache() {
			super(CACHE_SIZE, 1, true);
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, PreparedStatement> eldest) {
			PreparedStatement stmt = eldest.getValue();
			try {
				stmt.executeBatch();
				stmt.close();
				return true;
			} catch(SQLException e) {
				e.printStackTrace(); // TODO
				return false;
			}
		}
	}
	
	private final StatementCache selects = new StatementCache();
	private final StatementCache modifications = new StatementCache();
	
	private PreparedStatement tableExistsStmt;
	
	private final String scopeColumnName;
	
	private PreparedStatement getSelect(String sql) throws SQLException {
		synchronized(selects) {
			if(selects.containsKey(sql)) {
				return selects.get(sql);
			} else {
				PreparedStatement stmt = connection.prepareStatement(sql);
				selects.put(sql, stmt);
				return stmt;
			}
		}
	}
	
	private PreparedStatement getModification(String sql) throws SQLException {
		synchronized(modifications) {
			if(modifications.containsKey(sql)) {
				// modification statement is cached, increment counter and retrieve it.
				return modifications.get(sql);
			} else {
				PreparedStatement stmt = connection.prepareStatement(sql);
				modifications.put(sql, stmt);
				return stmt;
			}
		}
	}
	
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

	private JDBCSqliteConnection(String connectionPath, String scopeColumnName,
			int maxStatementsBeforeCommit) {
		this.scopeColumnName = scopeColumnName;
		this.maxStatementsBeforeCommit = maxStatementsBeforeCommit;
		this.connectionPath = connectionPath;
	}

	@Override
	public void commit() throws SQLConnectionException {
		try {
			synchronized(modifications) {
				for(PreparedStatement stmt : modifications.values()) {
					stmt.executeBatch();
				}
			}
			connection.commit();
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
			definitionStr += ", `" + columnName + "`" + textColumnType();
		}
		
		executeModification("CREATE TABLE `" + name + "` (" +
						definitionStr + ")");
		
		commit();
		return new SQLTable(this, name);
	}

	@Override
	public void close() throws ConnectionException {
		try {
			commit();
			connection.commit();
			
		} catch(SQLConnectionException e) {
			throw new SQLConnectionException(e);
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
	public SQLResultSet executeSelect(String sql) throws SQLConnectionException {
		commit(); // commit any lingering changes before selecting
		try {
			return new JDBCSQLiteResultSet(getSelect(sql).executeQuery());
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public SQLResultSet executeSelect(String sql, String[] parameters)
			throws SQLConnectionException {
		commit(); // commit any lingering changes before selecting
		try {
			PreparedStatement stmt = getSelect(sql);
			setParams(stmt, parameters);
			return new JDBCSQLiteResultSet(stmt.executeQuery());
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}

	@Override
	public void executeModification(String sql) throws SQLConnectionException {
		try {
			getModification(sql).addBatch();
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	@Override
	public void executeModification(String sql, String[] parameters)
			throws SQLConnectionException {
		try {
			PreparedStatement stmt = getModification(sql);
			setParams(stmt, parameters);
		} catch(SQLException e) {
			throw new SQLConnectionException(e);
		}
	}
	
	/**
	 * Static convenience method to bind an array of {@link String} params to
	 * a {@link PreparedStatement}.
	 * @param stmt The {@link PreparedStatement} on which to set the parameters.
	 * @param params A {@link String} list of parameters to set.
	 * @throws SQLException If the parameters cannot be set on <code>stmt</code>
	 */
	private static void setParams(PreparedStatement stmt, String[] params) throws SQLException {
		for(int i = 0 ; i < params.length ; i ++) {
			stmt.setString(i + 1, params[i]);
		}
	}
	
	/**
	 * Produce a {@link JDBCSqliteConnection} using a path to a database.
	 * @param pathToDB {@link String} path to database.
	 * @param scopeColumnName The name of the scope column in tables.
	 * @param maxStatementsBeforeCommit How many statements to make before committing.
	 */
	public static JDBCSqliteConnection toFile(String pathToDB, String scopeColumnName,
			int maxStatementsBeforeCommit) {
		return new JDBCSqliteConnection("jdbc:sqlite:" + pathToDB, scopeColumnName,
				maxStatementsBeforeCommit);
	}

	/**
	 * Produce a {@link JDBCSqliteConnection} in-memory.
	 * @param scopeColumnName The name of the scope column in tables.
	 * @param maxStatementsBeforeCommit How many statements to make before committing.
	 */
	public static JDBCSqliteConnection inMemory(String scopeColumnName, int maxStatementsBeforeCommit) {
		return new JDBCSqliteConnection("jdbc:sqlite::memory:", scopeColumnName,
				maxStatementsBeforeCommit);
	}
}
