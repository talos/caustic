package net.microscraper.log;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


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
	private PrintStream printStream;
	
	public JavaIOFileLogger(File logFile) {
		this.file = logFile;
	}
	
	public void open() throws IOException {
		printStream = new PrintStream(file);
	}
	
	public void close() throws IOException {
		printStream.close();
	}
	
	protected void write(String text) throws IllegalStateException {
		printStream.println(text);
	}
}
