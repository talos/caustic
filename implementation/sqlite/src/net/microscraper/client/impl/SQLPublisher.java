package net.microscraper.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.microscraper.client.Utils;
import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.Result;
import net.microscraper.client.impl.SQLInterface.PreparedStatement;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;

public class SQLPublisher implements Publisher {
	private static final String RESULTS_TABLE = "results";

	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String URI = "uri";
	private static final String NUMBER = "number";
	private static final String SOURCE_URI = "source_uri";
	private static final String SOURCE_NUMBER = "source_number";
	
	//private static final String EXECUTABLE_ID = "executable_id";
	
	//private static final String STUCK_ON = "stuck_on";
	//private static final String FAILURE_BECAUSE = "failure_because";	
	
	//public static final String EXECUTABLES_TABLE = "executables";
	
	private final SQLInterface sql;
	//private final PreparedStatement createExecutionsTable;
	private final PreparedStatement createResultsTable;
	//private final PreparedStatement checkExecutionsTable;
	private final PreparedStatement checkResultsTable;
	//private final PreparedStatement insertExecution;
	//private final PreparedStatement deleteExecution;
	private final PreparedStatement insertResult;
	
	private final int batchSize;
	private int executionsSinceLastCommit = 0;
	//private final Map<Integer, String[]> batchExecutables = new HashMap<Integer, String[]>();
	private final List<String[]> batchResults = new ArrayList<String[]>();
	
	public SQLPublisher(SQLInterface sql, int batchSize) throws SQLInterfaceException {
		this.sql = sql;
		this.batchSize = batchSize;
		try {
			/*createExecutionsTable =
				sql.prepareStatement(
						"CREATE TABLE `" + EXECUTABLES_TABLE + "` (" +
					"`" + RESOURCE_LOCATION + "` " + sql.varcharColumnType() + ", " +
					"`" + SOURCE_RESULT_ID + "` " + sql.intColumnType() + ", " +
					"`" + ID + "` " + sql.intColumnType() + " " + sql.keyColumnDefinition() + ", " +
					"`" + STUCK_ON + "` " + sql.varcharColumnType() + ", " +
					"`" + FAILURE_BECAUSE + "` " + sql.varcharColumnType() + " )");
			
			createExecutionsTable.execute();*/
			
			createResultsTable = 
				sql.prepareStatement(
						"CREATE TABLE `" + RESULTS_TABLE + "` (" +
						"`" + NAME  + "` " + sql.varcharColumnType() + ", " + 
						"`" + VALUE + "` " + sql.textColumnType()    + ", " +
						"`" + URI   + "` " + sql.varcharColumnType() + ", " +
						"`" + NUMBER+ "` " + sql.intColumnType()     + ", " +
						"`" + SOURCE_URI   + "` " + sql.varcharColumnType() + ", " +
						"`" + SOURCE_NUMBER+ "` " + sql.intColumnType()     + " )");						
			createResultsTable.execute();
			
			/*checkExecutionsTable = 
				sql.prepareStatement(
						"SELECT `"+ RESOURCE_LOCATION + "`, " +
								"`" + SOURCE_RESULT_ID + "`, " +
								"`" + ID + "`, " +
								"`" + STUCK_ON + "`, " +
								"`" + FAILURE_BECAUSE + "` " +
								"FROM " + EXECUTABLES_TABLE);*/
			checkResultsTable = 
				sql.prepareStatement(
						"SELECT `"+ NAME + "`, " +
								"`" + VALUE + "`, " +
								"`" + URI + "`, " +
								"`" + NUMBER + "`, " +
								"`" + SOURCE_URI + "` " +
								"`" + SOURCE_NUMBER + "` " +
								"FROM " + RESULTS_TABLE);

			/*deleteExecution = 
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
							"VALUES (?, ?, ?, ?, ?)");*/
			insertResult =
				sql.prepareStatement(
						"INSERT INTO `" + RESULTS_TABLE + "` (" +
								"`" + NAME + "`," +
								"`" + VALUE + "`," +
								"`" + URI + "`," +
								"`" + NUMBER + "`," +
								"`" + SOURCE_URI + "`," +
								"`" + SOURCE_NUMBER + "`) " +
							"VALUES (?, ?, ?, ?, ?, ?)");
			
			//checkExecutionsTable.executeQuery();
			checkResultsTable.executeQuery();
			sql.disableAutoCommit();
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			throw new SQLInterfaceException(e);
		}
	}
	/*
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
	*/
	public void forceCommit() throws SQLInterfaceException {
		// Iterate through our batch of ids, update the PreparedStatements.
		/*Iterator<Integer> ids = batchExecutables.keySet().iterator();
		while(ids.hasNext()) {
			Integer id = ids.next();
			
			deleteExecution.bindStrings(new String[] { Integer.toString(id) } );
			deleteExecution.addBatch();
			
			insertExecution.bindStrings(batchExecutables.get(id));
			insertExecution.addBatch();
		}
		*/
		for(int i = 0 ; i < batchResults.size() ; i ++) {
			insertResult.bindStrings(batchResults.get(i));
			insertResult.addBatch();
		}
		// Delete, then insert.
		//deleteExecution.executeBatch();
		//insertExecution.executeBatch();
		insertResult.executeBatch();
		sql.commit();
		
		// Clear out our batch parameters
		//batchExecutables.clear();
		batchResults.clear();
		executionsSinceLastCommit = 0;
	}
	
	private String truncateToVarchar(String stringToTruncate) {
		return Utils.truncate(stringToTruncate, sql.defaultVarcharLength());
	}
	
	private static String getResourceLocationString(Executable execution) {
		return execution.getResource().location.toString();
	}

	@Override
	public void publishResult(String name, String value, String uri,
			int number, String sourceUri, Integer sourceNumber) throws PublisherException {
		try {
			batchResults.add(
					new String[] {
						name == null ? sql.nullValue() : name,
						value,
						Integer.toString(number),
						sourceUri == null ? sql.nullValue() : sourceUri,
						sourceNumber == null ? sql.nullValue() : Integer.toString(sourceNumber)
					}
				);
			executionsSinceLastCommit++;
			
			if(executionsSinceLastCommit > batchSize) {
				forceCommit();
			}
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}		
	}
}
