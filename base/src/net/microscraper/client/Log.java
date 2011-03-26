package net.microscraper.client;

import java.util.Vector;

public class Log {
	private static final Vector loggers = new Vector();
	public static void addLogger(Interfaces.Logger logger) {
		loggers.addElement(logger);
	}
	public static void e(Throwable e) {
		e.printStackTrace();
		for(int i = 0; i < loggers.size(); i ++) {
			((Interfaces.Logger) loggers.elementAt(i)).e(e);
		}
	}
	public static void i(String infoText) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Interfaces.Logger) loggers.elementAt(i)).i(infoText);
		}
	}
}
