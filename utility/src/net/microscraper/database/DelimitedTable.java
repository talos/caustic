package net.microscraper.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;
import net.microscraper.database.Insertable;
import net.microscraper.util.StringUtils;

public class DelimitedTable implements Insertable {
		
	private final CSVWriter writer;
		
	private final List<String> columns;
	
	public DelimitedTable(CSVWriter writer, String[] columns) {
		this.writer = writer;
		this.columns = new ArrayList<String>(Arrays.asList(columns));
		// Prepend ID to the array of column names.		
		writer.writeNext(this.columns.toArray(new String[0]));
	}
	
	@Override
	public void insert(@SuppressWarnings("rawtypes") Hashtable map) throws TableManipulationException {
		if(map.size() != columns.size()) {
			throw new TableManipulationException(StringUtils.quote(map.toString()) + " does not fit in " +
					StringUtils.join(columns.toArray(new String[0]), ", "));
		}
		
		String[] valuesInOrder = new String[map.size()];
		for(int i = 0 ; i < columns.size(); i ++) {
			String columnName = columns.get(i);
			if(map.containsKey(columnName)) {
				valuesInOrder[i] = (String) map.get(columnName);
			} else {
				throw new TableManipulationException(StringUtils.quote(columnName) + " could not be found in " +
						StringUtils.quote(map.toString()));
			}
		}
		writer.writeNext(valuesInOrder);
	}
}
