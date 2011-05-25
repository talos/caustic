package net.microscraper.client.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import net.microscraper.client.Utils;
import net.microscraper.client.executable.Executable;
import net.microscraper.client.impl.SQLInterface.PreparedStatement;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;

public class SQLPublisher implements Publisher {
	public static final String TABLE_NAME = "executions";
	
	private final SQLInterface sql;
	private final PreparedStatement createTable;
	private final PreparedStatement checkTable;
	private final PreparedStatement insertExecution;
	private final PreparedStatement deleteExecution;
	
	private final int batchSize;
	private int executionsSinceLastCommit = 0;
	private final Hashtable<Integer, String[]> batchParameters = new Hashtable<Integer, String[]>();
	
	public SQLPublisher(SQLInterface sql, int batchSize) throws SQLInterfaceException {
		this.sql = sql;
		this.batchSize = batchSize;
		try {
			createTable =
				sql.prepareStatement(
						"CREATE TABLE `" + TABLE_NAME + "` (" +
					"`" + RESOURCE_LOCATION + "` " + sql.varcharColumnType() + ", " +
					"`" + SOURCE_ID + "` " + sql.intColumnType() + ", " +
					"`" + ID + "` " + sql.idColumnType() + " " + sql.keyColumnDefinition() + ", " +
					"`" + STUCK_ON + "` " + sql.varcharColumnType() + ", " +
					"`" + FAILURE_BECAUSE + "` " + sql.varcharColumnType() + ", " +
					"`" + NAME + "` " + sql.varcharColumnType() + ", " + 
					"`" + VALUE + "` " + sql.textColumnType() + " )");
			
			createTable.execute();
			checkTable = 
				sql.prepareStatement(
						"SELECT `"+ RESOURCE_LOCATION + "`, " +
								"`" + SOURCE_ID + "`, " +
								"`" + ID + "`, " +
								"`" + STUCK_ON + "`, " +
								"`" + FAILURE_BECAUSE + "`, " +
								"`" + NAME + "`, " +
								"`" + VALUE + "` " +
								"FROM " + TABLE_NAME);
			deleteExecution = 
				sql.prepareStatement(
						"DELETE FROM `" + TABLE_NAME + "` " +
						"WHERE `" + ID + "` = ?");
			insertExecution =
				sql.prepareStatement(
						"INSERT INTO `" + TABLE_NAME + "` (" +
								"`" + RESOURCE_LOCATION + "`," +
								"`" + SOURCE_ID + "`," +
								"`" + ID + "`," +
								"`" + STUCK_ON + "`," +
								"`" + FAILURE_BECAUSE + "`," +
								"`" + NAME + "`," +
								"`" + VALUE + "`) " +
							"VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			try {
				checkTable.executeQuery();
			} catch (SQLInterfaceException e2) {
				// Something is weird -- wrong schema in the specified SQL file?  Abort.
				throw new SQLInterfaceException("Error creating or using results table from the" +
						" specified SQL interface.", e2);
			}
			sql.disableAutoCommit();
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			throw new SQLInterfaceException(e);
		}
	}
	
	private void addEntry(Executable executable) throws SQLInterfaceException {
		String[] parameters = new String[] {
				getResourceLocationString(executable),
				executable.hasParent() ? Integer.toString(executable.getParent().getId()) : sql.nullValue(),
				Integer.toString(executable.getId()),
				executable.isStuck()    ? truncateToVarchar(executable.stuckOn()) : sql.nullValue(),
				executable.hasFailed()  ? truncateToVarchar(executable.failedBecause().toString()) : sql.nullValue(),
				executable.hasName() ? truncateToVarchar(executable.getName()) : sql.nullValue(),
				executable.hasValue() ? executable.getValue() : sql.nullValue()
			};
		batchParameters.put(executable.getId(), parameters);
		//insertExecution.bindStrings();
		//insertExecution.addBatch();
	}
	/*
	private void deleteEntry(Execution execution) throws SQLInterfaceException {
		deleteExecution.bindStrings(new String[] { Integer.toString(execution.getId()) });
		deleteExecution.addBatch();
	}
*/
	@Override
	public void publish(Executable execution) throws PublisherException {
		try {
			// delete existing entry
			//deleteEntry(execution);
			// Add new entry
			addEntry(execution);
			executionsSinceLastCommit++;
			
			if(executionsSinceLastCommit > batchSize) {
				forceCommit();
			}
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}
	}
	
	public void forceCommit() throws SQLInterfaceException {
		// Iterate through our batch of ids, update the PreparedStatements.
		Enumeration<Integer> ids = batchParameters.keys();
		while(ids.hasMoreElements()) {
			Integer id = ids.nextElement();
			
			deleteExecution.bindStrings(new String[] { Integer.toString(id) } );
			deleteExecution.addBatch();
			
			insertExecution.bindStrings(batchParameters.get(id));
			insertExecution.addBatch();
		}
		// Delete, then insert.
		deleteExecution.executeBatch();
		insertExecution.executeBatch();
		sql.commit();
		
		// Clear out our batch parameters
		batchParameters.clear();
		executionsSinceLastCommit = 0;
	}
	
	private String truncateToVarchar(String stringToTruncate) {
		return Utils.truncate(stringToTruncate, sql.defaultVarcharLength());
	}
	
	private static String getResourceLocationString(Executable execution) {
		return execution.getResource().location.toString();
	}
}
