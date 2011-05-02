package net.microscraper.client.impl;

import net.microscraper.client.Publisher;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;

public class SQLPublisher implements Publisher {
	public static final String TABLE_NAME = "executions";
	
	private final SQLInterface inter;
	public SQLPublisher(SQLInterface sql_interface) throws SQLInterfaceException {
		inter = sql_interface;
		
		try {
			inter.execute(
				"CREATE TABLE `"+ TABLE_NAME +"` (" +
					"`" + SOURCE_ID + "` " + inter.intColumnType() + ", " +
					"`" + ID + "` " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
					"`" + STATUS_STRING + "` " + inter.varcharColumnType() + ", " +
					"`" + NAME + "` " + inter.varcharColumnType() + ", " + 
					"`" + VALUE + "` " + inter.textColumnType() + " )");
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT `"+ SOURCE_ID +"`, `"+ ID +"`, `"+ STATUS_STRING +"`, `"
						+ NAME +"`, `"+ VALUE +"` FROM " + TABLE_NAME);
			} catch (SQLInterfaceException e2) {
				// Something is weird -- wrong schema in the specified SQL file?  Abort.
				throw new SQLInterfaceException("Error creating or using results table from the" +
						" specified SQL interface.", e2);
			}
		}
	}
	
	
	
	private void publish(Execution execution, String status, String value) throws PublisherException {
		try {
			// delete existing entry
			inter.execute("DELETE FROM `" + TABLE_NAME +"` WHERE `" + SOURCE_ID + "` = ? AND `" + ID + "` = ?",
				new String[] {
					Integer.toString(execution.getSourceExecution().id),
					Integer.toString(execution.id)
				});
			
			inter.execute("INSERT INTO `" + TABLE_NAME +
					"` (`" + SOURCE_ID + "`,`" + ID + "`,`" + STATUS_STRING + "`,`" + NAME + "`,`" + VALUE + "`) " +
					"VALUES (?, ?, ?, ?, ?)",
					new String[] {
						Integer.toString(execution.getSourceExecution().id),
						Integer.toString(execution.id),
						status,
						execution.getPublishName(),
						value });
			
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}
	}

	@Override
	public void publish(Execution execution, String result)
			throws PublisherException {
		publish(execution, SUCCESSFUL, result);
	}
	
	@Override
	public void publish(Execution execution, ExecutionDelay delay)
			throws PublisherException {
		publish(execution, DELAY, delay.reason());
	}

	@Override
	public void publish(Execution execution, ExecutionFailure failure)
			throws PublisherException {
		publish(execution, FAILURE, failure.reason());		
	}

	public boolean live() {
		return true;
	}
}
