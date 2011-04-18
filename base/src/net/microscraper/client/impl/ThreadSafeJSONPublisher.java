package net.microscraper.client.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Publisher;
import net.microscraper.client.Utils;
import net.microscraper.database.Reference;
import net.microscraper.database.Result;

public class ThreadSafeJSONPublisher implements Publisher {
	Hashtable results = new Hashtable();
	
	public boolean live() {
		return true;
	}
	public void publish(Result result) throws PublisherException {
		Client.context().log.i("publishing");
		//results.add(result.ref, result);
		Vector results_for_ref;
		if(results.containsKey(result.ref))
			results_for_ref = (Vector) results.get(result.ref);
		else
			results_for_ref = new Vector();
		results_for_ref.addElement(result);
	}
	
	public Result[] getResults(Reference ref) {
		if(results.containsKey(ref)) {
			Vector results_for_ref = (Vector) results.get(ref);
			Result[] results = new Result[results_for_ref.size()];
			results_for_ref.copyInto(results);
			return results;
		} else {
			return new Result[] {};
		}
	}
	
	public Result[] allResults() {
		Enumeration e = results.keys();
		Vector all_results = new Vector();
		while(e.hasMoreElements()) {
			Reference ref = (Reference) e.nextElement();
			Utils.arrayIntoVector(getResults(ref), all_results);
		}
		Result[] all_results_ary = new Result[all_results.size()];
		all_results.copyInto(all_results_ary);
		return all_results_ary;
	}
	
	public Result shiftRef(Reference ref) {
		if(results.containsKey(ref)) {
			Vector results_for_ref = (Vector) results.get(ref);
			if(results_for_ref.size() > 0) {
				Result result = (Result) results_for_ref.elementAt(0);
				results_for_ref.removeElementAt(0);
				return result;
			} else {
				results.remove(ref);
			}
		}
		return null;
	}
	
	public Result shift() {
		Enumeration e = results.keys();
		while(e.hasMoreElements()) {
			Reference ref = (Reference) e.nextElement();
			return shiftRef(ref);
		}
		return null;
	}
}
