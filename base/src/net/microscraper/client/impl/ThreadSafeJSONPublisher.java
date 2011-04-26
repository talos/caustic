package net.microscraper.client.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.database.Execution;
import net.microscraper.database.Reference;

public class ThreadSafeJSONPublisher implements Publisher {
	Vector executions = new Vector();
	
	public boolean live() {
		return true;
	}
	public void publish(Execution execution) throws PublisherException {
		Client.log.i("publishing");
		//results.add(result.ref, result);
		executions.addElement(execution);
	}
	
	public Execution shift() {
		Enumeration e = results.keys();
		while(e.hasMoreElements()) {
			Reference ref = (Reference) e.nextElement();
			return shiftRef(ref);
		}
		return null;
	}
}
