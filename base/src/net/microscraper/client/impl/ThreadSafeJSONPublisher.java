package net.microscraper.client.impl;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Publisher;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.Status;

public class ThreadSafeJSONPublisher implements Publisher {
	Vector executions = new Vector();
	private int loc = 0;
	
	public boolean live() {
		return true;
	}
	public void publish(Execution execution) throws PublisherException {
		Client.log.i("publishing");
		//results.add(result.ref, result);
		executions.addElement(execution);
	}
	
	public int size() {
		return executions.size();
	}
	
	public String get(int executionNumber) throws PublisherException {
		Execution exc = (Execution) executions.elementAt(executionNumber);
		Hashtable hash = new Hashtable();
		hash.put("id", Integer.toString(exc.id));
		hash.put("source_id", Integer.toString(exc.getSourceExecution().id));
		Status status = exc.getStatus();
		hash.put("status_code", Integer.toString(status.code));
		hash.put("name", exc.getPublishName());
		if(status == Status.SUCCESSFUL) {
			hash.put("value", exc.getPublishValue());
		}
		try {
			return Client.json.toJSON(hash);
		} catch(JSONInterfaceException e) {
			throw new PublisherException(e);
		}
	}
	
	public String next() throws PublisherException {
		if(size() > loc) {
			loc++;
			return get(loc - 1);
		} else {
			loc = 0;
			return null;
		}
	}
}
