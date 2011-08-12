package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.Microscraper;
import net.microscraper.interfaces.database.Database;
import net.microscraper.interfaces.database.DatabaseException;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceStringer;

public class ThreadSafeJSONDatabase implements Database {
	private final List<JSONInterfaceStringer> executions = Collections.synchronizedList(new ArrayList<JSONInterfaceStringer>());
	private Integer pos = -1;
	private final JSONInterface json;
	public ThreadSafeJSONDatabase(JSONInterface json) {
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
	public JSONInterfaceStringer next() {
		synchronized(executions) {
			synchronized(pos) {
				pos++;
				return executions.get(pos);
			}
		}
	}
}
