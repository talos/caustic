package net.microscraper.impl.log;

import java.util.Date;

import net.microscraper.Utils;
import net.microscraper.interfaces.log.Logger;

/**
 * An abstract implementation of {@link Logger} that truncates entries and
 * prepends a timestamp &amp; level identification.
 * <p>
 * Subclasses still must implement {@link #open()}, {@link #close()}, and
 * {@link #write(String)}.
 * @author talos
 * @see Logger
 *
 */
public abstract class BasicLogger implements Logger {

	/**
	 * The cutoff for the length of a single entry.
	 */
	private static final int MAX_ENTRY_LENGTH = 512;

	/**
	 * Protected method to write formatted log text to wherever the subclass 
	 * is directing output.
	 * @param text The {@link String} to write.
	 * @throws IllegalStateException if the {@link Logger} is not open or is
	 * closed.
	 */
	protected abstract void write(String text) throws IllegalStateException;
	
	private static String now() {
		return new Date().toString();
	}
	
	public final void e(Throwable e) throws IllegalStateException {
		write(now() + " Error: " + Utils.truncate(e.getMessage(), MAX_ENTRY_LENGTH));
	}

	public final void w(Throwable w) throws IllegalStateException {
		write(now() + " Warning: " + Utils.truncate(w.getMessage(), MAX_ENTRY_LENGTH));
	}

	public final void i(String infoText) throws IllegalStateException {
		write(now() + " Info: " + Utils.truncate(infoText, MAX_ENTRY_LENGTH));
	}
}