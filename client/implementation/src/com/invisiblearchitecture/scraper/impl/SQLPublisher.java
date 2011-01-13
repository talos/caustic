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
	 * Relationships table is defined here to ensure that its name does
	 * not overlap with data tables (which have a "_" appended before).
	 */
	public static final String relationshipsTableName = "relationships";
	
	/*
	 * This is prepended before tables and columns to make sure there aren't
	 * overlaps between dynamic and non-dynamic columns.
	 */
	public static final String prepend = "_";
	
	public static final String idColumnName = "id";
	public static final String parentNameColumnName = "parentName";
	public static final String parentIdColumnName = "parentId";
	public static final String childNameColumnName = "childName";
	public static final String childIdColumnName = "childId";
	
	/*
	 * Keep track of what tables we have created.
	 */
	private final Hashtable extantTables = new Hashtable();
	
	public SQLPublisher(SQLInterface inter) throws SQLInterfaceException {
		sqlInterface = inter;
		createRelationshipsTable();
	}
	
	@Override
	public void publishProgress(Information information, int progressPart,
			int progressTotal) {
		// TODO Should this be handled at all by publishers?

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
			
			/* Update the data table for this information type. */
			String sql;
			sql = "DELETE FROM " + sqlInterface.quoteField(tableName(information)) + " WHERE "
				+ sqlInterface.quoteField(idColumnName) + " = " + sqlInterface.quoteValue(Integer.toString(information.id));
			sqlInterface.execute(sql);
			sql = "INSERT INTO " + sqlInterface.quoteField(tableName(information)) +
				"("+ Utils.join(quotedColumnNames(information), ", ") + ") " +
				"VALUES (" + Utils.join(values, ", ") + ")";
			sqlInterface.execute(sql);
			Information[] children = information.children();
			
			/* Update the relationships table. */
			for(int i = 0; i < children.length; i++) {
				sql = "DELETE FROM " + relationshipsTableName + " WHERE " +
					sqlInterface.quoteField(parentNameColumnName) + " = " + sqlInterface.quoteValue(information.type) + " AND " +
					sqlInterface.quoteField(parentIdColumnName)   + " = " + sqlInterface.quoteValue(Integer.toString(information.id)) + " AND " + 
					sqlInterface.quoteField(childNameColumnName)  + " = " + sqlInterface.quoteValue(children[i].type) + " AND " +
					sqlInterface.quoteField(childIdColumnName)    + " = " + sqlInterface.quoteValue(Integer.toString(children[i].id));
				sqlInterface.execute(sql);
				sql = "INSERT INTO " + relationshipsTableName + " (" +
					sqlInterface.quoteField(parentNameColumnName) + ", "  +
					sqlInterface.quoteField(parentIdColumnName) + ", "  +
					sqlInterface.quoteField(childNameColumnName) + ", "  +
					sqlInterface.quoteField(childIdColumnName) + ") " +
					" VALUES (" + sqlInterface.quoteValue(information.type) + ", " +
					sqlInterface.quoteValue(Integer.toString(information.id)) + ", " +
					sqlInterface.quoteValue(children[i].type) + ", " +
					sqlInterface.quoteValue(Integer.toString(children[i].id)) + ")";
				sqlInterface.execute(sql);
			}
		} catch(SQLInterfaceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate a table name from an information. Also, append an underscore before table names so they do not overlap with
	 * relationship table.
	 * @param rawTableName
	 * @return
	 */
	private String tableName(Information information) {;
		return sqlInterface.quoteField(prepend + information.type);
	}
	
	/**
	 * Generate an array of quoted fields from an information.
	 * @param tableName
	 * @return
	 */
	private String[] quotedColumnNames(Information information) {
		String[] quotedFields = new String[information.fieldsToPublish.length];
		for(int i = 0; i < quotedFields.length; i++) {
			quotedFields[i] = sqlInterface.quoteField(prepend + information.fieldsToPublish[i]);
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
			quotedValues[i] = sqlInterface.quoteValue(information.getField(information.fieldsToPublish[i]));
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
		String sql = "CREATE TABLE " + tableName(information) + 
			" (" + sqlInterface.quoteField(idColumnName) + " " + sqlInterface.idColumnType() + " " + sqlInterface.keyColumnDefinition() + ", " +
			Utils.join(quotedColumnNames(information), " " + sqlInterface.dataColumnType() + ", ") + " " + sqlInterface.dataColumnType() + ")";
		sqlInterface.execute(sql);
		extantTables.put(tableName(information), true);
	}
	
	/**
	 * Create the relationships table.
	 */
	private void createRelationshipsTable() throws SQLInterfaceException {
		if(tableExists(relationshipsTableName))
			throw new SQLInterfaceException("There can only be one relationships table.");
		String sql = "CREATE TABLE " + sqlInterface.quoteField(relationshipsTableName) + " (" +
			sqlInterface.quoteField(parentNameColumnName) + " " + sqlInterface.idColumnType() + ", " +
			sqlInterface.quoteField(parentIdColumnName)   + " " + sqlInterface.idColumnType() + ", " +
			sqlInterface.quoteField(childNameColumnName)  + " " + sqlInterface.idColumnType() + ", " +
			sqlInterface.quoteField(childIdColumnName)    + " " + sqlInterface.idColumnType() + ")";
		sqlInterface.execute(sql);
		extantTables.put(relationshipsTableName, true);
	}
}
