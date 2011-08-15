package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Microscraper;
import net.microscraper.client.impl.commandline.json.JSONStringerInterface;
import net.microscraper.database.Database;
import net.microscraper.database.DatabaseException;
import net.microscraper.json.JSONParser;

public class ThreadSafeJSONDatabase implements Database {
	private final List<JSONStringerInterface> executions = Collections.synchronizedList(new ArrayList<JSONStringerInterface>());
	private Integer pos = -1;
	private final JSONParser json;
	public ThreadSafeJSONDatabase(JSONParser json) {
		this.json = json;
	}
	public void resetIterator() {
		synchronized(pos) {
			pos = -1;
		}
	}
	public boolean hasNext() {
		synchronized(executions) {
			synchronized(pos) {
				return executions.size() > pos + 1;
			}
		}
	}
	public JSONStringerInterface next() {
		synchronized(executions) {
			synchronized(pos) {
				pos++;
				return executions.get(pos);
			}
		}
	}
}
