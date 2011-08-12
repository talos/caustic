package net.microscraper.client.applet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.Utils;
import net.microscraper.impl.log.BasicLogger;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONInterfaceStringer;
import net.microscraper.interfaces.log.Logger;

public class ThreadSafeJSONLogger extends BasicLogger {
	private final List<JSONInterfaceStringer> logList = Collections.synchronizedList(new ArrayList<JSONInterfaceStringer>());
	private Integer pos = 0;
	private final JSONInterface jsonInterface;
	
	public ThreadSafeJSONLogger(JSONInterface jsonInterface) {
		this.jsonInterface = jsonInterface;
	}
	
	private JSONInterfaceStringer buildJSON(String key, String value) {
		try {
			JSONInterfaceStringer stringer = jsonInterface.getStringer();
			stringer.object().key(key).value(value).endObject();
			return stringer;
		} catch (JSONInterfaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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

	@Override
	public void open() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void write(String text) throws IllegalStateException {
		synchronized(logList) {
			logList.addAll(newEntries);
		}
	}
}
