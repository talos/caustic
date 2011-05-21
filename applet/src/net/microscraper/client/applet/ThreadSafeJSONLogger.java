package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.Logger;
import net.microscraper.client.Utils;

public class ThreadSafeJSONLogger implements Logger {
	private final List<JSONInterfaceStringer> logList = Collections.synchronizedList(new ArrayList<JSONInterfaceStringer>());
	private Integer pos = 0;
	private final JSONInterface json;
	private static final int STACK_TRACE_DEPTH = 10;
	
	public ThreadSafeJSONLogger(JSONInterface _json) {
		json = _json;
	}
	
	@Override
	public void e(Throwable e) {
		List<JSONInterfaceStringer> list = stackTraceList(e);
		list.add(buildJSON("error", Utils.truncate(e.toString(), MAX_ENTRY_LENGTH)));
		addToList(list);
	}
	
	@Override
	public void w(Throwable w) {
		List<JSONInterfaceStringer> list = stackTraceList(w);
		list.add(buildJSON("warning", Utils.truncate(w.toString(), MAX_ENTRY_LENGTH)));
		addToList(list);
	}

	@Override
	public void i(String infoText) {
		List<JSONInterfaceStringer> list = new ArrayList<JSONInterfaceStringer>();
		list.add(buildJSON("info", Utils.truncate(infoText, MAX_ENTRY_LENGTH)));
		addToList(list);
	}
	
	private void addToList(List<JSONInterfaceStringer> newEntries) {
		synchronized(logList) {
			logList.addAll(newEntries);
		}
	}
	
	private JSONInterfaceStringer buildJSON(String key, String value) {
		try {
			JSONInterfaceStringer stringer = json.getStringer();
			stringer.object().key(key).value(value).endObject();
			return stringer;
		} catch (JSONInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	// Run the traces backwards so that they appear in correct order when unshifted.
	private List<JSONInterfaceStringer> stackTraceList(Throwable t) {
		StackTraceElement[] traces = t.getStackTrace();
		List<JSONInterfaceStringer> list = new ArrayList<JSONInterfaceStringer>();
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
	
	public JSONInterfaceStringer next() {
		synchronized(logList) {
			synchronized(pos) {
				pos++;
				return logList.get(pos);
			}
		}
	}
}
