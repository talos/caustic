package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Interfaces.Logger;

public class ThreadSafeLogger implements Logger {
	private final List<String> log_list = Collections.synchronizedList(new ArrayList<String>());
	private static final int STACK_TRACE_DEPTH = 10;
	
	@Override
	public void e(Throwable e) {
		List<String> list = stackTraceList(e);
		list.add(0, "ERROR: " + e.toString());
		log_list.addAll(list);
	}

	@Override
	public void w(Throwable w) {
		List<String> list = stackTraceList(w);
		list.add(0, "WARNING: " + w.toString());
		log_list.addAll(list);
	}

	@Override
	public void i(String infoText) {
		List<String> list = new ArrayList<String>();
		list.add("INFO: " + infoText);
		log_list.addAll(list);
	}
	
	/**
	 * Pull out the most recent log item.
	 * @return The log item, or null if there is none.
	 */
	public String unshift() {
		try {
			return log_list.remove(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	// Run the traces backwards so that they appear in correct order when unshifted.
	private static List<String> stackTraceList(Throwable t) {
		StackTraceElement[] traces = t.getStackTrace();
		List<String> list = new ArrayList<String>();
		int depth = traces.length < STACK_TRACE_DEPTH ? traces.length : STACK_TRACE_DEPTH;
		for( int i = depth ; i >= 0; i--) {
			list.add("TRACE: " + traces[i].toString());
		}
		return list;
	}
}
