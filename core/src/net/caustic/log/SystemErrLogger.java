package net.caustic.log;

/**
 * An implementation of {@link BasicLogger} using {@link System#err}.
 * @author talos
 * @see Logger
 * @see BasicLogger
 * @see System#out
 */
public final class SystemErrLogger extends BasicLogger {
	
	protected void write(String text) throws IllegalStateException {
		System.err.print(text);
		System.err.println();
	}
}
