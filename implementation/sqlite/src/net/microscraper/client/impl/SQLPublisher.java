package net.microscraper.client.impl;

import net.microscraper.client.Publisher;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.execution.Execution;

public class SQLPublisher implements Publisher {
	public static final String TABLE_NAME = "executions";
	
	private final SQLInterface inter;
	public SQLPublisher(SQLInterface sql_interface) throws SQLInterfaceException {
		inter = sql_interface;
		
		try {
			inter.execute(
				"CREATE TABLE `"+ TABLE_NAME +"` (" +
					"`" + RESOURCE_LOCATION + "` " + inter.varcharColumnType() + ", " +
					"`" + SOURCE_ID + "` " + inter.intColumnType() + ", " +
					"`" + ID + "` " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
					"`" + COMPLETE + "` " + inter.intColumnType() + ", " +
					"`" + STUCK + "` " + inter.intColumnType() + ", " +
					"`" + FAILURE + "` " + inter.intColumnType() + ", " +
					"`" + NAME + "` " + inter.varcharColumnType() + ", " + 
					"`" + TYPE + "` " + inter.varcharColumnType() + ", " + 
					"`" + VALUE + "` " + inter.textColumnType() + " )");
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT `"+ RESOURCE_LOCATION + "`, `" + SOURCE_ID +"`, `"+ ID +"`, `"+ COMPLETE +"`, `" + 
						"`, `" + STUCK + "`, `"+ FAILURE 
						+ NAME + "`, `"+ TYPE +"`, `"+ VALUE +"` FROM " + TABLE_NAME);
			} catch (SQLInterfaceException e2) {
				// Something is weird -- wrong schema in the specified SQL file?  Abort.
				throw new SQLInterfaceException("Error creating or using results table from the" +
						" specified SQL interface.", e2);
			}
		}
	}
	
	private void addEntry(Execution execution) throws SQLInterfaceException {
		String[] substitutions = new String[] {
				getResourceLocationString(execution),
				Integer.toString(execution.getCaller().getId()),
				Integer.toString(execution.getId()),
				execution.isComplete() ? "1" : "0",
				execution.isStuck()    ? "1" : "0",
				execution.hasFailed()  ? "1" : "0",
				execution.hasPublishName() ? execution.getPublishName() : inter.nullValue(),
				execution.getResource().getClass().getSimpleName(),
				execution.hasPublishValue() ? execution.getPublishValue() : inter.nullValue() };
		inter.execute("INSERT INTO `" + TABLE_NAME +
				"` (`" + RESOURCE_LOCATION + "`,`" + SOURCE_ID + "`,`" + ID + "`,`" + COMPLETE + "`,`"
				+ STUCK + "`,`" + FAILURE + "`,`" + NAME + "`,`"
				+ TYPE + "`,`" + VALUE + "`) " +
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", substitutions);
	}

	@Override
	public void publish(Execution execution) throws PublisherException {
		try {
			// delete existing entries
			inter.execute("DELETE FROM `" + TABLE_NAME +"` WHERE `"
					+ RESOURCE_LOCATION + "` = ? AND `"
					+ SOURCE_ID + "` = ? AND `" + ID + "` = ?",
				new String[] {
					getResourceLocationString(execution), 
					Integer.toString(execution.getCaller().getId()),
					Integer.toString(execution.getId())
				});
			// Add new entry
			addEntry(execution);
			
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}
	}
	
	private static String getResourceLocationString(Execution execution) {
		return execution.getResource().location.toString();
	}
}
