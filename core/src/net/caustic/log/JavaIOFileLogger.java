package net.caustic.log;

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
	
	//private final File file;
	private final String pathToFile;
	private PrintStream printStream;
	
	public JavaIOFileLogger(String pathToFile) {
		this.pathToFile = pathToFile;
	}
	
	public void open() throws IOException {
		super.open();
		printStream = new PrintStream(new File(pathToFile));
	}
	
	public void close() throws IOException {
		super.close();
		printStream.close();
	}
	
	protected void write(String text) {
		printStream.println(text);
	}
}
