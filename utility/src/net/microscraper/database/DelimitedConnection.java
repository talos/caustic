package net.microscraper.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVWriter;

import net.microscraper.database.InsertableConnection;
import net.microscraper.database.Insertable;

public class DelimitedConnection implements InsertableConnection {
		
	private final CSVWriter writer;
	
	private DelimitedConnection(char delimiter, Writer writer) {
		this.writer = new CSVWriter(writer, delimiter);
	}
	
	
	public static DelimitedConnection toFile(String pathToFile, char delimiter) throws IOException {
		return new DelimitedConnection(delimiter, new FileWriter(new File(pathToFile)));
	}
	
	public static DelimitedConnection toStdOut(char delimiter) {
		return new DelimitedConnection(delimiter, new Writer() {

			@Override
			public void close() throws IOException { }

			@Override
			public void flush() throws IOException {
				System.out.println();
			}

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				System.out.print(Arrays.copyOfRange(cbuf, off, off+len));
			}
			
		});
	}
	

	@Override
	public Insertable getInsertable(String[] textColumns) {
		return new DelimitedTable(writer, textColumns);
	}


}
