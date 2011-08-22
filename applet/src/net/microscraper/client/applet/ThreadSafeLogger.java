package net.microscraper.client.applet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.impl.log.BasicLogger;

public class ThreadSafeLogger extends BasicLogger {
	private final List<String> logList = Collections.synchronizedList(new ArrayList<String>());
	private Integer pos = 0;
	
	public boolean hasNext() {
		synchronized(logList) {
			synchronized(pos) {
				return logList.size() > pos + 1;
			}
		}
	}
	
	public String next() {
		synchronized(logList) {
			synchronized(pos) {
				pos++;
				return logList.get(pos);
			}
		}
	}

	@Override
	public void open() throws IOException {	}

	@Override
	public void close() throws IOException {	}

	@Override
	protected void write(String text) throws IllegalStateException {
		logList.add(text);
	}
}
