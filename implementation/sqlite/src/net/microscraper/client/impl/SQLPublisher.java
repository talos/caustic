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
			String create_table_sql = "CREATE TABLE " + inter.quoteField(TABLE_NAME) + " (" +
				inter.quoteField(SOURCE_ID) + " " + inter.intColumnType() + ", " +
				inter.quoteField(ID) + " " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
				inter.quoteField(STATUS_CODE) + " " + inter.intColumnType() + ", " +
				inter.quoteField(NAME) + " " + inter.dataColumnType() + ", " + 
				inter.quoteField(VALUE) + " " + inter.dataColumnType() + " )";
			inter.execute(create_table_sql);
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT " + inter.quoteField(SOURCE_ID) +
					inter.quoteField(ID) +
					inter.quoteField(STATUS_CODE) + 
					inter.quoteField(NAME) +
					inter.quoteField(VALUE) +
					" FROM " + inter.quoteField(TABLE_NAME));
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
			String delete_sql = "DELETE FROM " + inter.quoteField(TABLE_NAME) + " WHERE " +
				inter.quoteField(SOURCE_ID) + " = "
						+ inter.quoteValue(Integer.toString(execution.getSourceExecution().id)) +
				inter.quoteField(ID) + " = " + inter.quoteValue(Integer.toString(execution.id));
			inter.execute(delete_sql);
			
			Status status = execution.getStatus();
			String insert_sql = "INSERT INTO " + inter.quoteField(TABLE_NAME) + " (" +
				inter.quoteField(SOURCE_ID) + ", " +
				inter.quoteField(ID) + ", " + 
				inter.quoteField(STATUS_CODE) + ", " +
				inter.quoteField(NAME) + ", " +
				inter.quoteField(VALUE) +
				") VALUES (" + 
				inter.quoteValue(Integer.toString(execution.getSourceExecution().id)) + ", " +
				inter.quoteValue(Integer.toString(execution.id)) + ", " +
				inter.quoteValue(Integer.toString(execution.getStatus().code)) + ", " +
				// insert NULL for name if incomplete.
				(status == Status.SUCCESSFUL ? inter.quoteValue(execution.getPublishName()) : inter.nullValue() ) + ", " +
				// insert NULL for value if incomplete.
				(status == Status.SUCCESSFUL ? inter.quoteValue(execution.getPublishValue()): inter.nullValue() ) + " )";
			inter.execute(insert_sql);
		} catch(SQLInterfaceException e) {
			Client.log.e(e);
			throw new PublisherException();
		}
	}

	public boolean live() {
		return true;
	}
}
