package net.microscraper.impl.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.microscraper.interfaces.log.Logger;

/**
 * An implementation of {@link BasicLogger} using {@link java.io.File}.
 * @author talos
 * @see Logger
 * @see BasicLogger
 * @see java.io.File
 *
 */
public class JavaIOFileLogger extends BasicLogger {
	
	private final File file;
	private PrintWriter writer;
	
	public JavaIOFileLogger(File logFile) {
		this.file = logFile;
	}
	
	public void open() throws IOException {
		writer = new PrintWriter(file);
	}
	
	public void close() throws IOException {
		writer.close();
	}
	
	@Override
	protected void write(String text) throws IllegalStateException {
		writer.print(text);
		writer.println();
	}
}
