package net.microscraper.client.impl;

import net.microscraper.client.Publisher;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.database.Execution;
import net.microscraper.database.Status;

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
				"CREATE TABLE `"+ TABLE_NAME +"` (" +
					"`" + SOURCE_ID + "` " + inter.intColumnType() + ", " +
					"`" + ID + "` " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
					"`" + STATUS_CODE + "` " + inter.intColumnType() + ", " +
					"`" + NAME + "` " + inter.dataColumnType() + ", " + 
					"`" + VALUE + "` " + inter.dataColumnType() + " )");
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT `"+ SOURCE_ID +"`, `"+ ID +"`, `"+ STATUS_CODE +"`, `"
						+ NAME +"`, `"+ VALUE +"` FROM " + TABLE_NAME);
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
			inter.execute("DELETE FROM `" + TABLE_NAME +"` WHERE `" + SOURCE_ID + "` = ? AND `" + ID + "` = ?",
				new String[] {
					Integer.toString(execution.getSourceExecution().id),
					Integer.toString(execution.id)
				});
			
			Status status = execution.getStatus();
			String name = execution.getPublishName();
			String value = null;
			if(status.isSuccessful()) {
				value = ((Status.Successful) status).getResult();
			}
			inter.execute("INSERT INTO `" + TABLE_NAME +
					"` (`" + SOURCE_ID + "`,`" + ID + "`,`" + STATUS_CODE + "`,`" + NAME + "`,`" + VALUE + "`) " +
					"VALUES (?, ?, ?, ?, ?)",
					new String[] {
						Integer.toString(execution.getSourceExecution().id),
						Integer.toString(execution.id),
						Integer.toString(status.code),
						name, value });
		} catch(SQLInterfaceException e) {
			throw new PublisherException(e);
		}
	}

	public boolean live() {
		return true;
	}
}
