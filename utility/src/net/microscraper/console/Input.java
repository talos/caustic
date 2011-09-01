package net.microscraper.console;

import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import au.com.bytecode.opencsv.CSVReader;

import net.microscraper.util.HashtableUtils;

public class Input {
	private final Hashtable<String, String> shared;
	
	private String[] headers;
	private final boolean isSingleRow;
	private String pathToInput;
	private boolean hasReadSingleRow = false;
	private FileReader fReader;
	private CSVReader input;
	private char inputColumnDelimiter;
	
	public Input(Hashtable<String,String> shared, String pathToInput, char inputColumnDelimiter) {
		this.shared = shared;
		this.pathToInput = pathToInput;
		//this.headers = input.readNext();
		this.inputColumnDelimiter = inputColumnDelimiter;
		this.isSingleRow = false;
	}
	
	public Input(Hashtable<String,String> shared) {
		this.shared = shared;
		this.headers = new String[] {};
		this.isSingleRow = true;
	}
	
	public void open() throws IOException {
		if(!isSingleRow) {
			fReader = new FileReader(pathToInput);
			input = new CSVReader(fReader, inputColumnDelimiter);
		}
	}
	
	public void close() throws IOException {
		if(!isSingleRow) {
			fReader.close();
		}
	}
	
	/**
	 * 
	 * @return <code>null</code> if there are no more values, a {@link Hashtable}
	 * of the next row of input values otherwise.
	 * @throws IOException
	 */
	public Hashtable<String, String> next() throws IOException {
		String[] values = null;
		if(isSingleRow && !hasReadSingleRow) {
			values = new String[] {};
		} else {
			values = input.readNext();
		}
		if(values != null) {
			Hashtable<String, String> rowInput = new Hashtable<String, String>();
			for(int i = 0 ; i < values.length ; i ++) {
				rowInput.put(headers[i], values[i]);
			}
			return HashtableUtils.combine(new Hashtable[] { shared, rowInput } );
		} else {
			return null;
		}
	}
	

}
