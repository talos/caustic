package net.microscraper.console;

import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class wraps around {@link CSVReader} to provide a {@link #next()} method
 * to read lines of input but return a {@link Map} of column titles to
 * values.  It also can be initialized without a {@link CSVReader}, in which case
 * it returns only a single row.
 * @author realest
 *
 */
public class Input {
	/**
	 * Extra {@link Map} mappings that each input row is merged into.
	 */
	private final Map<String, String> shared;
	
	private final boolean hasCSV;
	private int rowsRead = 0;
	
	private final String pathToCSV;
	private final char inputColumnDelimiter;
	
	/**
	 * The file from which {@link #csvRows} reads.
	 */
	private FileReader fReader;
	
	/**
	 * An optional {@link CSVReader} with rows of input.
	 * Reads from {@link #fReader}.
	 */
	private CSVReader csvRows;
	
	/**
	 * The first row of {@link #csvRows}.
	 */
	private String[] headers;
	private boolean isOpen = false;
	
	private Input(Map<String,String> shared, String pathToCSV, char inputColumnDelimiter) {
		this.shared = shared;
		this.pathToCSV = pathToCSV;
		this.inputColumnDelimiter = inputColumnDelimiter;
		if(pathToCSV != null) {
			this.hasCSV = true;
		} else {
			this.hasCSV = false;
		}
	}
	
	public static Input fromSharedAndCSV(Map<String, String> shared, String pathToCSV, char inputColumnDelimiter) {
		return new Input(shared, pathToCSV, inputColumnDelimiter);
	}
	
	public static Input fromShared(Map<String, String> shared) {
		return new Input(shared, null, '\0');
	}
	
	/**
	 * Open this {@link Input}'s {@link #fReader} and {@link #csvRows},
	 * if it has them.
	 * @throws IOException
	 */
	public void open() throws IOException {
		if(hasCSV) {
			fReader = new FileReader(pathToCSV);
			csvRows = new CSVReader(fReader, inputColumnDelimiter);
			
			headers = csvRows.readNext();
			if(headers == null) {
				throw new IOException("No lines in input CSV.");
			}
			isOpen = true;
		}
	}
	
	/**
	 * Close this {@link Input}'s {@link #fReader}, if it has one.
	 * @throws IOException
	 */
	public void close() throws IOException {
		if(hasCSV) {
			fReader.close();
		}
	}
	
	/**
	 * 
	 * @return <code>null</code> if there are no more values, a {@link Map}
	 * of the next row of input values otherwise.  Must call {@link #open()} before.
	 * @throws IOException if there is an error reading from the input file.
	 */
	public Map<String, String> next() throws IOException {
		final Map<String, String> result;
		if(!hasCSV) { // return the shared map on the first run if there's no CSV.
			if(rowsRead > 0) {
				result = null;
			} else {
				result = shared;
			}
		} else {
			if(!isOpen) {
				throw new IllegalStateException("Must open input before reading from it.");
			}
			String[] values = csvRows.readNext();
			if(values != null) {
				Map<String, String> rowInput = new HashMap<String, String>(shared);
				for(int i = 0 ; i < values.length ; i ++) {
					rowInput.put(headers[i], values[i]);
				}
				result = rowInput;
			} else {
				result = null; // no more rows
			}
		}
		rowsRead++;
		return result;
	}
}
