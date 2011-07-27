package net.microscraper;

import java.util.Vector;

import net.microscraper.interfaces.log.Logger;


public class Log {
	private final Vector loggers = new Vector();
	public void register(Logger logger) {
		loggers.addElement(logger);
	}
	public void e(Throwable e) {
		//e.printStackTrace();
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).e(e);
		}
	}
	// No stacktrace for warnings.
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
}