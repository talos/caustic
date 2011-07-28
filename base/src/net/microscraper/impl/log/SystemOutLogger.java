package net.microscraper.impl.log;

import net.microscraper.interfaces.log.Logger;

/**
 * An implementation of {@link BasicLogger} using {@link System.out}.
 * @author talos
 * @see Logger
 * @see BasicLogger
 * @see System.out
 */
public class SystemOutLogger extends BasicLogger {
	
	public void open() { }
	
	public void close() { }

	protected void write(String text) throws IllegalStateException {
		System.out.print(text);
		System.out.println();
	}
}
