package com.invisiblearchitecture.scraper.impl;

import java.util.Hashtable;

import com.invisiblearchitecture.scraper.Information;
import com.invisiblearchitecture.scraper.Publisher;
import com.invisiblearchitecture.scraper.SQLInterface;
import com.invisiblearchitecture.scraper.SQLInterface.SQLInterfaceException;
import com.invisiblearchitecture.scraper.Utils;

/**
 * Class to publish to a SQL database.
 * @author john
 *
 */
public class SQLPublisher implements Publisher {
	
	private final SQLInterface sqlInterface;
	
	/*
	 * Keep track of what tables we have created.
	 */
	private final Hashtable extantTables = new Hashtable();
	
	public SQLPublisher(SQLInterface inter) {
		sqlInterface = inter;
	}
	
	@Override
	public void publishProgress(Information information, int progressPart,
			int progressTotal) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publish(Information information) {
		String[] values = quotedValues(information);
		if(values.length == 0)
			return;
		try {
			createTable(information);
		} catch (SQLInterfaceException e) {} // Catch createTable's throw if table already exists
		try {
			String sql;
			sql = "DELETE FROM " + tableName(information) + " WHERE " + sqlInterface.idColumnName() + " = " + information.id;
			sqlInterface.query(sql);
			sql = "INSERT INTO " + tableName(information) +
				"("+ Utils.join(quotedColumnNames(information), ", ") + ") " +
				"VALUES (" + Utils.join(values, ", ") + ")";
			sqlInterface.query(sql);
		} catch(SQLInterfaceException e) {
			
		}
	}
	
	/**
	 * Generate a table name from an information.
	 * @param rawTableName
	 * @return
	 */
	private String tableName(Information information) {
		return sqlInterface.fieldQuotation() + information.type + sqlInterface.fieldQuotation();
	}
	
	/**
	 * Generate an array of quoted fields from an information.
	 * @param tableName
	 * @return
	 */
	private String[] quotedColumnNames(Information information) {
		String[] quotedFields = new String[information.fieldsToPublish.length];
		for(int i = 0; i < quotedFields.length; i++) {
			quotedFields[i] = sqlInterface.fieldQuotation() + information.fieldsToPublish[i] + sqlInterface.fieldQuotation();
		}
		return quotedFields;
	}
	
	/**
	 * Generate an array of quoted values from an information.
	 * @param tableName
	 * @return
	 */
	private String[] quotedValues(Information information) {
		String[] quotedValues = new String[information.fieldsToPublish.length];
		for(int i = 0; i < quotedValues.length; i++) {
			quotedValues[i] = sqlInterface.fieldQuotation() + information.getField(information.fieldsToPublish[i]) + sqlInterface.fieldQuotation();
		}
		return quotedValues;
	}
	
	private boolean tableExists(String tableName) {
		if(extantTables.containsKey(tableName))
			return true;
		return false;
	}
	
	/**
	 * Create a very basic table to hold a single type of information.
	 * @param tableName
	 * @param columns
	 * @throws SQLInterfaceException
	 */
	private void createTable(Information information) throws SQLInterfaceException {
		if(tableExists(tableName(information))) {
			throw new SQLInterfaceException("Table " + tableName(information) + " already exists, did not create.");
		}
		for(int i = 0; i < information.fieldsToPublish.length; i++) {
			if(information.fieldsToPublish[i].equals(sqlInterface.idColumnName()))
				throw new SQLInterfaceException("Publish field " + sqlInterface.idColumnName() + " overlaps with id column.");
		}
		String sql = "CREATE TABLE " + tableName(information) + 
			" (" + Utils.join(quotedColumnNames(information), " " + sqlInterface.dataColumnType() + ", ") + " " + sqlInterface.dataColumnType() + ")";
		sqlInterface.query(sql);
	}
}
