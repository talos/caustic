package net.microscraper.client.impl;

import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.database.Execution;

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
	
	public Execution get(int executionNumber) {
		return (Execution) executions.elementAt(executionNumber);
	}
	
	public Execution next() {
		if(size() > loc) {
			loc++;
			return get(loc - 1);
		} else {
			loc = 0;
			return null;
		}
	}
}
