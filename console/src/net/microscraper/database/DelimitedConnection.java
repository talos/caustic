package net.microscraper.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVWriter;

import net.microscraper.database.InsertableConnection;
import net.microscraper.database.Insertable;

/**
 * An {@link InsertableConnection} implementation meant for delimited output.
 * @author realest
 *
 */
public class DelimitedConnection implements InsertableConnection {
	
	private final char delimiter;
	private final boolean isToStdout;
	private final String pathToFile;
	private CSVWriter csvWriter;
	
	/**
	 * A basic {@link Writer} implementation that pipes output to stdout using
	 * {@link System#out}.
	 * @author realest
	 *
	 */
	private static class SystemOutWriter extends Writer {

		@Override
		public void close() throws IOException { }

		@Override
		public void flush() throws IOException {
			System.out.println();
		}
		
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			System.out.print(new String(Arrays.copyOfRange(cbuf, off, off+len)));
		}
	}
	
	private DelimitedConnection(char delimiter) {
		this.isToStdout = true;
		this.delimiter = delimiter;
		this.pathToFile = null;
	}
	
	private DelimitedConnection(char delimiter, String pathToFile) {
		this.isToStdout = false;
		this.delimiter = delimiter;
		this.pathToFile = pathToFile;
	}
	
	/**
	 * Obtain a {@link DelimitedConnection} to a file.  Must be {@link #open}ed separately.
	 * @param pathToFile {@link String} path to the file that this {@link DelimitedConnection}
	 * will write to.
	 * @param delimiter The {@link char} delimiter to use.
	 * @return A {@link DelimitedConnection}.
	 */
	public static DelimitedConnection toFile(String pathToFile, char delimiter) {
		return new DelimitedConnection(delimiter, pathToFile);
	}

	/**
	 * Obtain a {@link DelimitedConnection} to {@link System#out}.
	 * @param delimiter The {@link char} delimiter to use.
	 * @return A {@link DelimitedConnection}.
	 */
	public static DelimitedConnection toSystemOut(char delimiter) {
		return new DelimitedConnection(delimiter);
	}
	
	@Override
	public Insertable newInsertable(String name, String[] textColumns) {
		return new DelimitedTable(csvWriter, textColumns);
	}
	
	@Override
	public void open() throws IOException {
		Writer writer;
		if(isToStdout) {
			writer = new SystemOutWriter();
		} else {
			writer = new FileWriter(new File(pathToFile));
		}
		this.csvWriter = new CSVWriter(writer, delimiter);
	}

	@Override
	public void close() throws IOException {
		csvWriter.close();
	}
}
