package net.caustic.database.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import net.caustic.database.ConnectionException;
import net.caustic.database.WritableConnection;
import net.caustic.database.WritableTable;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVConnection implements WritableConnection {

	private final char delimiter;
	private final boolean isToStdout;
	private final String pathToFile;
	private CSVWriter csvWriter;
	
	private CSVConnection(char delimiter) {
		this.isToStdout = true;
		this.delimiter = delimiter;
		this.pathToFile = null;
	}
	
	private CSVConnection(char delimiter, String pathToFile) {
		this.isToStdout = false;
		this.delimiter = delimiter;
		this.pathToFile = pathToFile;
	}
	
	/**
	 * Obtain a {@link CSVConnection} to a file.  Must be {@link #open}ed separately.
	 * @param pathToFile {@link String} path to the file that this {@link CSVConnection}
	 * will write to.
	 * @param delimiter The {@link char} delimiter to use.
	 * @return A {@link CSVConnection}.
	 */
	public static CSVConnection toFile(String pathToFile, char delimiter) {
		return new CSVConnection(delimiter, pathToFile);
	}

	/**
	 * Obtain a {@link DelimitedConnection} to {@link System#out}.
	 * @param delimiter The {@link char} delimiter to use.
	 * @return A {@link DelimitedConnection}.
	 */
	public static CSVConnection toSystemOut(char delimiter) {
		return new CSVConnection(delimiter);
	}
	
	@Override
	public WritableTable newWritable(String name, String[] textColumns) {
		return new CSVTable(csvWriter, textColumns);
	}
	
	@Override
	public void open() throws ConnectionException {
		Writer writer;
		if(isToStdout) {
			writer = new SystemOutWriter();
		} else {
			try {
				writer = new FileWriter(new File(pathToFile));
			} catch (IOException e) {
				throw new ConnectionException(e);
			}
		}
		this.csvWriter = new CSVWriter(writer, delimiter);
	}

	@Override
	public void close() throws ConnectionException {
		try {
			csvWriter.close();
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
	
	/**
	 * The path to the CSV, or <code>stdout</code> if there is
	 * no saved-to-file.
	 */
	@Override
	public String toString() {
		return pathToFile == null ? "stdout" : pathToFile;
	}
}
