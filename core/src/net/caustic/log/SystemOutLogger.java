package net.caustic.log;

import java.io.IOException;


/**
 * An implementation of {@link BasicLogger} using {@link System#out}.
 * @author talos
 * @see Logger
 * @see BasicLogger
 * @see System#out
 */
public class SystemOutLogger extends BasicLogger {
	
	public void open() throws IOException {
		super.open();
	}
	
	public void close() throws IOException {
		super.close();
	}

	protected void write(String text) throws IllegalStateException {
		System.out.print(text);
		System.out.println();
	}
}
