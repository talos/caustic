package net.microscraper.log;

import java.io.IOException;
import java.util.Date;


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
	
	private boolean isOpen = false;
	
	private static String now() {
		return new Date().toString();
	}
	
	private void ensureOpen() throws IllegalStateException {
		if(isOpen == false) {
			throw new IllegalStateException(getClass() + " must be opened.");
		}
	}
	
	/**
	 * Protected method to write formatted log text to wherever the subclass 
	 * is directing output.
	 * @param text The {@link String} to write.
	 * @throws IllegalStateException if the {@link Logger} is not open or is
	 * closed.
	 */
	protected abstract void write(String text) throws IllegalStateException;
	
	
	public final void e(Throwable e) throws IllegalStateException {
		ensureOpen();
		write(now() + " Error: " + e.getMessage());
	}

	public final void w(Throwable w) throws IllegalStateException {
		ensureOpen();
		write(now() + " Warning: " + w.getMessage());
	}

	public final void i(String infoText) throws IllegalStateException {
		ensureOpen();
		write(now() + " Info: " + infoText);
	}
	
	public void open() throws IOException {
		isOpen = true;
	}
	
	public void close() throws IOException {
		isOpen = false;
	}
}