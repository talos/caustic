package net.microscraper.client.impl;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.resources.Execution;
import net.microscraper.resources.Status;
import net.microscraper.resources.Execution.ExecutionProblem;

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
					"`" + STATUS + "` " + inter.varcharColumnType() + ", " +
					"`" + NAME + "` " + inter.varcharColumnType() + ", " + 
					"`" + TYPE + "` " + inter.varcharColumnType() + ", " + 
					"`" + VALUE + "` " + inter.textColumnType() + " )");
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT `"+ SOURCE_ID +"`, `"+ ID +"`, `"+ STATUS +"`, `"
						+ NAME + "`, `"+ TYPE +"`, `"+ VALUE +"` FROM " + TABLE_NAME);
			} catch (SQLInterfaceException e2) {
				// Something is weird -- wrong schema in the specified SQL file?  Abort.
				throw new SQLInterfaceException("Error creating or using results table from the" +
						" specified SQL interface.", e2);
			}
		}
	}
	
	private void addEntry(Execution execution, String status, Class<?> klass, String value) throws SQLInterfaceException {
		String[] substitutions = new String[] {
			Integer.toString(execution.getSourceExecution().id),
			Integer.toString(execution.id),
			status,
			execution.getPublishName(),
			klass.getSimpleName(),
			value };
		inter.execute("INSERT INTO `" + TABLE_NAME +
				"` (`" + SOURCE_ID + "`,`" + ID + "`,`" + STATUS + "`,`" + NAME + "`,`" + TYPE + "`,`" + VALUE + "`) " +
				"VALUES (?, ?, ?, ?, ?, ?)", substitutions);
	}
	
	public void publish(Execution execution, Status status) throws PublisherException {
		try {
			// delete existing entries
			inter.execute("DELETE FROM `" + TABLE_NAME +"` WHERE `" + SOURCE_ID + "` = ? AND `" + ID + "` = ?",
				new String[] {
					Integer.toString(execution.getSourceExecution().id),
					Integer.toString(execution.id)
				});
			String[] successes = status.successes();
			for(int i = 0 ; i < successes.length ; i ++ ) {
				addEntry(execution, SUCCESS, successes[i].getClass(), successes[i].toString());
			}
			ExecutionProblem[] delays = status.delays();
			for(int i = 0 ; i < delays.length ; i ++ ) {
				addEntry(execution, DELAY, delays[i].problemClass(), delays[i].reason());
			}
			ExecutionProblem[] failures = status.failures();
			for(int i = 0 ; i < failures.length ; i ++ ) {
				addEntry(execution, FAILURE, failures[i].problemClass(), failures[i].reason());
			}
			
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}
	}

	public boolean live() {
		return true;
	}
}
