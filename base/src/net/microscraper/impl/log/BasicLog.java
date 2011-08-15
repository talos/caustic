package net.microscraper.impl.log;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.Loggable;
import net.microscraper.client.Logger;

/**
 * A {@link BasicLog} is an implementation of {@link Loggable}
 * to coordinate many {@link Logger}s simultaneously through
 * {@link Logger}'s own methods.
 * <p>
 * All {@link Logger}s that are registered will receive
 * the {@link Logger} methods called on the {@link BasicLog}.
 * @author talos
 * @see Logger
 *
 */
public final class BasicLog implements Logger, Loggable {
	private final Vector loggers = new Vector();
	public void register(Logger logger) {
		loggers.addElement(logger);
	}
	public void e(Throwable e) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).e(e);
		}
	}
	public void w(Throwable w) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).w(w);
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