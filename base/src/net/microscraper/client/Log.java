package net.microscraper.client;

import java.util.Vector;

import net.microscraper.client.Interfaces.Log.Logger;

public class Log {
	private final Vector loggers = new Vector();
	public void register(Logger logger) {
		loggers.addElement(logger);
	}
	public void e(Throwable e) {
		e.printStackTrace();
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).e(e);
		}
	}
	public void i(String infoText) {
		for(int i = 0; i < loggers.size(); i ++) {
			((Logger) loggers.elementAt(i)).i(infoText);
		}
	}
}