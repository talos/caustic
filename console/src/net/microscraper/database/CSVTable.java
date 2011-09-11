package net.microscraper.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.microscraper.console.UUID;
import net.microscraper.util.StringUtils;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVTable implements WritableTable {

	public static final String ID_COLUMN = "id";
	
	private final CSVWriter writer;

	private final List<String> headers;

	public CSVTable(CSVWriter writer, String[] columns) {
		this.writer = writer;
		this.headers = new ArrayList<String>(Arrays.asList(columns));
		this.headers.add(0, ID_COLUMN);
		writer.writeNext(this.headers.toArray(new String[0]));
	}

	@Override
	public void insert(UUID id, Map<String, String> insertMap) throws TableManipulationException {
		/*if(insertMap.size() > headers.size()) {
			throw new TableManipulationException(StringUtils.quote(insertMap.toString()) + " is too wide for " +
					StringUtils.join(headers.toArray(new String[0]), ", "));
		}*/
		if(!headers.containsAll(insertMap.keySet())) {
			throw new TableManipulationException(StringUtils.quote(insertMap.toString()) +
					" has key not contained in " + StringUtils.quote(headers.toString()));
		}
		
		// CSVWriter can handle null elements.
		String[] valuesInOrder = new String[headers.size()];
		for(int i = 0 ; i < headers.size(); i ++) {
			String columnName = headers.get(i);
			if(insertMap.containsKey(columnName)) {
				valuesInOrder[i] = (String) insertMap.get(columnName);
			}/* else {
				throw new TableManipulationException(StringUtils.quote(columnName) +
						" could not be found in " +
						StringUtils.quote(insertMap.toString()));
			}*/
		}
		writer.writeNext(valuesInOrder);
	}
}
