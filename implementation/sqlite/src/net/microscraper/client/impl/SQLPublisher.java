package net.microscraper.client.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.microscraper.client.NameValuePair;
import net.microscraper.client.Utils;
import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.Result;
import net.microscraper.client.impl.SQLInterface.PreparedStatement;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;

public class SQLPublisher implements Publisher {
	public static final String EXECUTABLES_TABLE = "executables";
	public static final String RESULTS_TABLE = "results";
	
	private final SQLInterface sql;
	private final PreparedStatement createExecutionsTable;
	private final PreparedStatement createResultsTable;
	private final PreparedStatement checkExecutionsTable;
	private final PreparedStatement checkResultsTable;
	private final PreparedStatement insertExecution;
	private final PreparedStatement deleteExecution;
	private final PreparedStatement insertResult;
	
	private final int batchSize;
	private int executionsSinceLastCommit = 0;
	private final Map<Integer, String[]> batchExecutables = new HashMap<Integer, String[]>();
	private final List<String[]> batchResults = new ArrayList<String[]>();
	
	public SQLPublisher(SQLInterface sql, int batchSize) throws SQLInterfaceException {
		this.sql = sql;
		this.batchSize = batchSize;
		try {
			createExecutionsTable =
				sql.prepareStatement(
						"CREATE TABLE `" + EXECUTABLES_TABLE + "` (" +
					"`" + RESOURCE_LOCATION + "` " + sql.varcharColumnType() + ", " +
					"`" + SOURCE_RESULT_ID + "` " + sql.intColumnType() + ", " +
					"`" + ID + "` " + sql.intColumnType() + " " + sql.keyColumnDefinition() + ", " +
					"`" + STUCK_ON + "` " + sql.varcharColumnType() + ", " +
					"`" + FAILURE_BECAUSE + "` " + sql.varcharColumnType() + " )");
			
			createExecutionsTable.execute();
			
			createResultsTable = 
				sql.prepareStatement(
						"CREATE TABLE `" + RESULTS_TABLE + "` (" +
						"`" + EXECUTABLE_ID + "` " + sql.intColumnType() + ", " +
						"`" + ID + "` " + sql.intColumnType() + " " + sql.keyColumnDefinition() + ", " +
						"`" + NAME + "` " + sql.varcharColumnType() + ", " + 
						"`" + VALUE + "` " + sql.textColumnType() + " )");
			createResultsTable.execute();
			
			checkExecutionsTable = 
				sql.prepareStatement(
						"SELECT `"+ RESOURCE_LOCATION + "`, " +
								"`" + SOURCE_RESULT_ID + "`, " +
								"`" + ID + "`, " +
								"`" + STUCK_ON + "`, " +
								"`" + FAILURE_BECAUSE + "` " +
								"FROM " + EXECUTABLES_TABLE);
			checkResultsTable = 
				sql.prepareStatement(
						"SELECT `"+ EXECUTABLE_ID + "`, " +
								"`" + ID + "`, " +
								"`" + NAME + "`, " +
								"`" + VALUE + "` " +
								"FROM " + RESULTS_TABLE);

			deleteExecution = 
				sql.prepareStatement(
						"DELETE FROM `" + EXECUTABLES_TABLE + "` " +
						"WHERE `" + ID + "` = ?");
			insertExecution =
				sql.prepareStatement(
						"INSERT INTO `" + EXECUTABLES_TABLE + "` (" +
								"`" + RESOURCE_LOCATION + "`," +
								"`" + SOURCE_RESULT_ID + "`," +
								"`" + ID + "`," +
								"`" + STUCK_ON + "`," +
								"`" + FAILURE_BECAUSE + "`) " +
							"VALUES (?, ?, ?, ?, ?)");
			insertResult =
				sql.prepareStatement(
						"INSERT INTO `" + RESULTS_TABLE + "` (" +
								"`" + EXECUTABLE_ID + "`," +
								"`" + ID + "`," +
								"`" + NAME + "`," +
								"`" + VALUE + "`) " +
							"VALUES (?, ?, ?, ?)");
			
			checkExecutionsTable.executeQuery();
			checkResultsTable.executeQuery();
			sql.disableAutoCommit();
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			throw new SQLInterfaceException(e);
		}
	}
	
	private void addEntry(Executable executable) throws SQLInterfaceException {
		batchExecutables.put(executable.getId(),
			new String[] {
				getResourceLocationString(executable),
				Integer.toString(executable.getSource().getId()),
				Integer.toString(executable.getId()),
				executable.isStuck()    ? truncateToVarchar(executable.stuckOn()) : sql.nullValue(),
				executable.hasFailed()  ? truncateToVarchar(executable.failedBecause().toString()) : sql.nullValue()
			}
		);
		if(executable.isComplete()) {
			Result[] results = executable.getResults();
			for(int i = 0 ; i < results.length ; i++) {
				Result result = results[i];
				batchResults.add(
						new String[] {
							Integer.toString(executable.getId()),
							Integer.toString(result.getId()),
							result.getName(),
							result.getValue()
						}
					);
			}
		}
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
		Iterator<Integer> ids = batchExecutables.keySet().iterator();
		while(ids.hasNext()) {
			Integer id = ids.next();
			
			deleteExecution.bindStrings(new String[] { Integer.toString(id) } );
			deleteExecution.addBatch();
			
			insertExecution.bindStrings(batchExecutables.get(id));
			insertExecution.addBatch();
		}
		
		for(int i = 0 ; i < batchResults.size() ; i ++) {
			insertResult.bindStrings(batchResults.get(i));
			insertResult.addBatch();
		}
		// Delete, then insert.
		deleteExecution.executeBatch();
		insertExecution.executeBatch();
		insertResult.executeBatch();
		sql.commit();
		
		
		
		// Clear out our batch parameters
		batchExecutables.clear();
		executionsSinceLastCommit = 0;
	}
	
	private String truncateToVarchar(String stringToTruncate) {
		return Utils.truncate(stringToTruncate, sql.defaultVarcharLength());
	}
	
	private static String getResourceLocationString(Executable execution) {
		return execution.getResource().location.toString();
	}
}
