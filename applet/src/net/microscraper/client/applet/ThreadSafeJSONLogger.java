package net.microscraper.client.applet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Logger;
import net.microscraper.client.impl.commandline.json.JSONStringerInterface;
import net.microscraper.impl.log.BasicLogger;
import net.microscraper.json.JSONParser;
import net.microscraper.json.JSONParserException;
import net.microscraper.util.StringUtils;

public class ThreadSafeJSONLogger extends BasicLogger {
	private final List<JSONStringerInterface> logList = Collections.synchronizedList(new ArrayList<JSONStringerInterface>());
	private Integer pos = 0;
	private final JSONParser jsonInterface;
	
	public ThreadSafeJSONLogger(JSONParser jsonInterface) {
		this.jsonInterface = jsonInterface;
	}
	
	private JSONStringerInterface buildJSON(String key, String value) {
		try {
			JSONStringerInterface stringer = jsonInterface.getStringer();
			stringer.object().key(key).value(value).endObject();
			return stringer;
		} catch (JSONParserException e) {
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
	
	public JSONStringerInterface next() {
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
