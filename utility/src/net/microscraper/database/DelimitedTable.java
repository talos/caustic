package net.microscraper.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import net.microscraper.database.DatabaseException;
import net.microscraper.database.WritableTable;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.StringUtils;

public class DelimitedTable implements WritableTable {
	
	private static final String ID_COLUMN_NAME = "id";
	
	private final CSVWriter writer;
	
	private int curId = -1;
	
	private final List<String> columns;
	
	public DelimitedTable(CSVWriter writer, String[] columns) {
		this.writer = writer;
		this.columns = new ArrayList<String>(Arrays.asList(columns));
		// Prepend ID to the array of column names.
		this.columns.add(0, ID_COLUMN_NAME);
		
		writer.writeNext(this.columns.toArray(new String[0]));
	}
	
	@Override
	public int insert(NameValuePair[] nameValuePairs) throws DatabaseException {
		// Prepend ID to the array of nameValuePairs.
		curId ++;
		nameValuePairs = Arrays.copyOf(nameValuePairs, nameValuePairs.length + 1);
		nameValuePairs[nameValuePairs.length -1] = new BasicNameValuePair(ID_COLUMN_NAME, Integer.toString(curId));
		
		if(nameValuePairs.length != columns.size()) {
			throw new DatabaseException(BasicNameValuePair.preview(nameValuePairs) + " does not fit in " +
					StringUtils.join(columns.toArray(new String[0]), ", "));
		}
		
		String[] valuesInOrder = new String[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length; i ++) {
			int index = columns.indexOf(nameValuePairs[i].getName());
			if(index > -1) {
				valuesInOrder[index] = nameValuePairs[i].getValue();
			} else {
				throw new DatabaseException(nameValuePairs[i].getName() + " is not a column in " +
						StringUtils.join(columns.toArray(new String[0]), ", "));
			}
		}
		writer.writeNext(valuesInOrder);
		return curId;
	}


}
