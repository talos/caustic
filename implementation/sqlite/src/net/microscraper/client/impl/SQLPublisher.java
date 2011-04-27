package net.microscraper.client.impl;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.impl.SQLInterface.SQLInterfaceException;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;

public class SQLPublisher implements Publisher {
	
	public static final String TABLE_NAME = "results";
		
	public static final String CALLER_ID = "caller_id";
	public static final String ID = "id";
	//public static final String REF = "ref";
	public static final String KEY = "key";
	public static final String VALUE = "value";
	
	private final SQLInterface inter;
	public SQLPublisher(SQLInterface sql_interface) throws SQLInterfaceException {
		inter = sql_interface;
		
		try {
			String create_table_sql = "CREATE TABLE " + inter.quoteField(TABLE_NAME) + " (" +
				inter.quoteField(CALLER_ID) + " " + inter.intColumnType() + ", " +
				inter.quoteField(ID) + " " + inter.idColumnType() + " " + inter.keyColumnDefinition() + ", " +
			//	inter.quoteField(REF) + " " + inter.dataColumnType() + ", " + 
				inter.quoteField(KEY) + " " + inter.dataColumnType() + ", " + 
				inter.quoteField(VALUE) + " " + inter.dataColumnType() + " )";
			inter.execute(create_table_sql);
		} catch(SQLInterfaceException e) {
			// The table might already exist.
			try {
				inter.query("SELECT " + inter.quoteField(CALLER_ID) +
					inter.quoteField(ID) +
			//		inter.quoteField(REF) +
					inter.quoteField(KEY) +
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
			if(execution.getStatus() == Status.SUCCESSFUL) {
				String insert_sql = "INSERT INTO " + inter.quoteField(TABLE_NAME) + " (" +
					inter.quoteField(CALLER_ID) + ", " +
					inter.quoteField(ID) + ", " + 
			//		inter.quoteField(REF) + ", " +
					inter.quoteField(KEY) + ", " +
					inter.quoteField(VALUE) +
					") VALUES (" + 
					inter.quoteValue(Integer.toString(execution.getSourceExecution().id)) + ", " +
					inter.quoteValue(Integer.toString(execution.id)) + ", " +
					inter.quoteValue(execution.ref.toString()) + ", " +
					inter.quoteValue(execution.key) + ", " +
					inter.quoteValue(execution.value) + " )";
				inter.execute(insert_sql);
			}
		} catch(SQLInterfaceException e) {
			Client.log.e(e);
			throw new PublisherException();
		}
	}

	public boolean live() {
		return true;
	}
}
