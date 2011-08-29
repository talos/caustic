package net.microscraper.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import net.microscraper.database.Insertable;
import net.microscraper.util.StringUtils;

public class DelimitedTable implements Insertable {
		
	private final CSVWriter writer;
		
	private final List<String> headers;
	
	public DelimitedTable(CSVWriter writer, String[] columns) {
		this.writer = writer;
		this.headers = new ArrayList<String>(Arrays.asList(columns));
		// Prepend ID to the array of column names.		
		writer.writeNext(this.headers.toArray(new String[0]));
	}
	
	@Override
	public void insert(@SuppressWarnings("rawtypes") Hashtable insertMap) throws TableManipulationException {
		if(insertMap.size() > headers.size()) {
			throw new TableManipulationException(StringUtils.quote(insertMap.toString()) + " is too wide for " +
					StringUtils.join(headers.toArray(new String[0]), ", "));
		}
		
		// CSVWriter can handle null elements.
		String[] valuesInOrder = new String[insertMap.size()];
		for(int i = 0 ; i < headers.size(); i ++) {
			String columnName = headers.get(i);
			if(insertMap.containsKey(columnName)) {
				valuesInOrder[i] = (String) insertMap.get(columnName);
			} else {
				throw new TableManipulationException(StringUtils.quote(columnName) + " could not be found in " +
						StringUtils.quote(insertMap.toString()));
			}
		}
		writer.writeNext(valuesInOrder);
	}
}
