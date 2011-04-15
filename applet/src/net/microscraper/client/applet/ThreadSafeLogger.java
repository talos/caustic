package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.Logger;

public class ThreadSafeLogger implements Logger {
	private final List<String> log_list = Collections.synchronizedList(new ArrayList<String>());
	private final Interfaces.JSON json;
	private static final int STACK_TRACE_DEPTH = 10;
	
	public ThreadSafeLogger(Interfaces.JSON _json) {
		json = _json;
	}
	
	@Override
	public void e(Throwable e) {
		List<String> list = stackTraceList(e);
		list.add(buildJSON("error", e.toString()));
		log_list.addAll(list);
	}

	@Override
	public void w(Throwable w) {
		List<String> list = stackTraceList(w);
		list.add(buildJSON("warning", w.toString()));
		log_list.addAll(list);
	}

	@Override
	public void i(String infoText) {
		List<String> list = new ArrayList<String>();
		list.add(buildJSON("info", infoText));
		log_list.addAll(list);
	}
	
	/**
	 * Pull out the oldest log item.
	 * @return The log item, or null if there is none.
	 */
	public String shift() {
		try {
			return log_list.remove(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	private String buildJSON(String key, String value) {
		Hashtable<String, String> hash = new Hashtable<String, String> ();
		hash.put(key, value);
		try {
			return json.toJSON(hash);
		} catch (JSONInterfaceException e) {
			return "{\"error\":\"Logger unable to create JSON.\"}";
		}
	}
	
	// Run the traces backwards so that they appear in correct order when unshifted.
	private List<String> stackTraceList(Throwable t) {
		StackTraceElement[] traces = t.getStackTrace();
		List<String> list = new ArrayList<String>();
		int depth = traces.length < STACK_TRACE_DEPTH ? traces.length : STACK_TRACE_DEPTH;
		for( int i = depth - 1 ; i >= 0; i--) {
			list.add(buildJSON("trace", traces[i].toString()));
		}
		return list;
	}
}
