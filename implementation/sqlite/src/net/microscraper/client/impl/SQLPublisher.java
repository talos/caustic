package net.microscraper.client.impl;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;

public class SQLPublisher implements Publisher {
	
	public static final String TABLE_NAME = "executions";
		
	public static final String SOURCE_ID = "source_id";
	public static final String ID = "id";
	public static final String STATUS_CODE = "status_code";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	
	private final SQLInterface inter;
	public SQLPublisher(SQLInterface sql_interface) throws SQLInterfaceException {
		inter = sql_interface;
		
		try {
			inter.execute(
				"CREATE TABLE `?` (" +
					"`?` " + inter.intColumnType() + ", " +
					"`?` " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
					"`?` " + inter.intColumnType() + ", " +
					"`?` " + inter.dataColumnType() + ", " + 
					"`?` " + inter.dataColumnType() + " )",
				new String[] { TABLE_NAME, SOURCE_ID, ID, STATUS_CODE, NAME, VALUE });
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT `?`, `?`, `?`, `?`, `?` FROM `?`", 
					new String[] { SOURCE_ID, ID, STATUS_CODE, NAME, VALUE, TABLE_NAME });
			} catch (SQLInterfaceException e2) {
				// Something is weird -- wrong schema in the specified SQL file?  Abort.
				throw new SQLInterfaceException("Error creating or using results table from the" +
						" specified SQL interface.", e2);
			}
		}
	}
	
	public void publish(Execution execution) throws PublisherException {
		try {
			// delete existing entry
			inter.execute("DELETE FROM `?` WHERE `?` = '?' AND `?` = '?'",
				new String[] { TABLE_NAME,
					SOURCE_ID, Integer.toString(execution.getSourceExecution().id),
					ID, Integer.toString(execution.id)
				});
			
			Status status = execution.getStatus();
			String name = null;
			String value = null;
			if(status == Status.SUCCESSFUL) {
				name = execution.getPublishName();
				value = execution.getPublishValue();
			}
			inter.execute("INSERT INTO `?` (`?`, `?`, `?`, `?`, `?`) " +
					"VALUES ('?', '?', '?', '?', '?')",
					new String[] { TABLE_NAME, SOURCE_ID, ID, STATUS_CODE, NAME, VALUE,
						Integer.toString(execution.getSourceExecution().id),
						Integer.toString(execution.id),
						Integer.toString(status.code),
						name, value });
		} catch(SQLInterfaceException e) {
			Client.log.e(e);
			throw new PublisherException();
		}
	}

	public boolean live() {
		return true;
	}
}
