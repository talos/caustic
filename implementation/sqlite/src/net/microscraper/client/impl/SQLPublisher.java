package net.microscraper.client.impl;

import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.SQLInterface.PreparedStatement;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.execution.Execution;

public class SQLPublisher implements Publisher {
	public static final String TABLE_NAME = "executions";
	
	private final SQLInterface sql;
	private final PreparedStatement createTable;
	private final PreparedStatement checkTable;
	private final PreparedStatement insertExecution;
	private final PreparedStatement deleteExecution;
	
	private final int batchSize;
	private int executionsSinceLastCommit = 0;
	
	public SQLPublisher(SQLInterface sql, int batchSize) throws SQLInterfaceException {
		this.sql = sql;
		this.batchSize = batchSize;
		try {
			createTable =
				sql.prepareStatement(
						"CREATE TABLE `" + TABLE_NAME + "` (" +
					"`" + RESOURCE_LOCATION + "` " + sql.varcharColumnType() + ", " +
					"`" + SOURCE_ID + "` " + sql.intColumnType() + ", " +
					"`" + ID + "` " + sql.idColumnType() +", " + //+ " " + sql.keyColumnDefinition() + ", " +
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
	
	private void addEntry(Execution execution) throws SQLInterfaceException {
		insertExecution.bindStrings(new String[] {
				getResourceLocationString(execution),
				execution.hasCaller() ? Integer.toString(execution.getCaller().getId()) : sql.nullValue(),
				Integer.toString(execution.getId()),
				execution.isStuck()    ? truncateToVarchar(execution.stuckOn()) : sql.nullValue(),
				execution.hasFailed()  ? truncateToVarchar(execution.failedBecause().toString()) : sql.nullValue(),
				execution.hasPublishName() ? truncateToVarchar(execution.getPublishName()) : sql.nullValue(),
				execution.hasPublishValue() ? execution.getPublishValue() : sql.nullValue()
			});
		insertExecution.addBatch();
	}
	
	private void deleteEntry(Execution execution) throws SQLInterfaceException {
		deleteExecution.bindStrings(new String[] { Integer.toString(execution.getId()) });
		deleteExecution.addBatch();
	}

	@Override
	public void publish(Execution execution) throws PublisherException {
		try {
			// delete existing entry
			deleteEntry(execution);
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
		deleteExecution.executeBatch();
		sql.commit();
		insertExecution.executeBatch();
		sql.commit();
		executionsSinceLastCommit = 0;
	}
	
	private String truncateToVarchar(String stringToTruncate) {
		return Utils.truncate(stringToTruncate, sql.defaultVarcharLength());
	}
	
	private static String getResourceLocationString(Execution execution) {
		return execution.getResourceLocation().toString();
	}
}
