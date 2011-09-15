package net.microscraper.database.csv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.microscraper.database.TableManipulationException;
import net.microscraper.database.WritableTable;
import net.microscraper.util.StringUtils;
import net.microscraper.uuid.UUID;

import au.com.bytecode.opencsv.CSVWriter;

class CSVTable implements WritableTable {

	public static final String SCOPE_COLUMN = "scope";
	
	private final CSVWriter writer;

	private final List<String> headers;

	public CSVTable(CSVWriter writer, String[] columns) {
		this.writer = writer;
		this.headers = new ArrayList<String>(Arrays.asList(columns));
		
		// headersAry has to include scope column
		String[] headersAry = new String[columns.length + 1];
		headersAry[0] = SCOPE_COLUMN;
		for(int i = 0 ; i < columns.length ; i ++) {
			headersAry[i + 1] = columns[i];
		}
		
		this.writer.writeNext(headersAry);
	}

	@Override
	public void insert(UUID id, Map<String, String> insertMap) throws TableManipulationException {
		if(!headers.containsAll(insertMap.keySet())) {
			throw new TableManipulationException(StringUtils.quote(insertMap.toString()) +
					" has key not contained in " + StringUtils.quote(headers.toString()));
		}
		
		// CSVWriter can handle null elements.
		String[] valuesInOrder = new String[headers.size() + 1];
		for(int i = 0 ; i < headers.size(); i ++) {
			String columnName = headers.get(i);
			if(insertMap.containsKey(columnName)) {
				valuesInOrder[i + 1] = (String) insertMap.get(columnName);
			}
		}
		valuesInOrder[0] = id.asString();
		writer.writeNext(valuesInOrder);
	}
}
