package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Stringer;
import net.microscraper.client.Interfaces.Logger;
import net.microscraper.client.Utils;

public class ThreadSafeJSONLogger implements Logger {
	private final List<Stringer> logList = Collections.synchronizedList(new ArrayList<Stringer>());
	private Integer pos = 0;
	private final Interfaces.JSON json;
	private static final int STACK_TRACE_DEPTH = 10;
	
	public ThreadSafeJSONLogger(Interfaces.JSON _json) {
		json = _json;
	}
	
	@Override
	public void e(Throwable e) {
		List<Stringer> list = stackTraceList(e);
		list.add(buildJSON("error", Utils.truncate(e.toString(), MAX_ENTRY_LENGTH)));
		addToList(list);
	}
	
	@Override
	public void w(Throwable w) {
		List<Stringer> list = stackTraceList(w);
		list.add(buildJSON("warning", Utils.truncate(w.toString(), MAX_ENTRY_LENGTH)));
		addToList(list);
	}

	@Override
	public void i(String infoText) {
		List<Stringer> list = new ArrayList<Stringer>();
		list.add(buildJSON("info", Utils.truncate(infoText, MAX_ENTRY_LENGTH)));
		addToList(list);
	}
	
	private void addToList(List<Stringer> newEntries) {
		synchronized(logList) {
			logList.addAll(newEntries);
		}
	}
	
	private Stringer buildJSON(String key, String value) {
		try {
			Stringer stringer = json.getStringer();
			stringer.object().key(key).value(value).endObject();
			return stringer;
		} catch (JSONInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	// Run the traces backwards so that they appear in correct order when unshifted.
	private List<Stringer> stackTraceList(Throwable t) {
		StackTraceElement[] traces = t.getStackTrace();
		List<Stringer> list = new ArrayList<Stringer>();
		int depth = traces.length < STACK_TRACE_DEPTH ? traces.length : STACK_TRACE_DEPTH;
		for( int i = depth - 1 ; i >= 0; i--) {
			list.add(buildJSON("trace", traces[i].toString()));
		}
		return list;
	}

	public boolean hasNext() {
		synchronized(logList) {
			synchronized(pos) {
				return logList.size() > pos + 1;
			}
		}
	}
	
	public Stringer next() {
		synchronized(logList) {
			synchronized(pos) {
				pos++;
				return logList.get(pos);
			}
		}
	}
}
