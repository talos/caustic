package net.microscraper.client.impl;

import java.util.Vector;

import net.microscraper.client.Publisher;
import net.microscraper.database.Reference;
import net.microscraper.database.Result;

public class ThreadSafeJSONPublisher implements Publisher {
	Vector results = new Vector();
	
	public boolean live() {
		return true;
	}
	public void publish(Result result) throws PublisherException {
		results.add(result);
	}
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Pull out the oldest result.
	 * @return
	 */
	/*public Result shift() {
		try {
			return (Result) results.remove(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}*/
	public Result[] getResults(Reference ref) {
		
	}
	
	public Result[] allResults() {
		
	}
}
