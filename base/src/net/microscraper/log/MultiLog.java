package net.microscraper.log;

import java.io.IOException;
import java.util.Vector;

/**
 * A {@link MultiLog} is an implementation of {@link Loggable}
 * to coordinate many {@link Logger}s simultaneously through
 * {@link Logger}'s own methods.
 * <p>
 * All {@link Logger}s that are registered will receive
 * the {@link Logger} methods called on the {@link MultiLog}.
 * @author talos
 * @see Logger
 *
 */
public final class MultiLog implements Logger, Loggable {
	private final Vector loggers = new Vector();
	public void register(Logger logger) {
		loggers.addElement(logger);
	}
	public void e(Throwable e) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).e(e);
		}
	}
	public void i(String infoText) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).i(infoText);
		}
	}
	public void open() throws IOException {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).open();
		}
	}
	public void close() throws IOException {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).close();
		}
	}
}