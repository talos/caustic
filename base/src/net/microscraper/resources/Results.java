package net.microscraper.resources;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.resources.definitions.Reference;

public class Results {
	private Hashtable successes = new Hashtable();
	private Hashtable delays = new Hashtable();
	private Hashtable failures = new Hashtable();
	
	public void addSuccess(Reference ref, String success){
		successes.put(ref, success);
	}
	public void addDelay(Reference ref, ScrapingDelay e) {
		delays.put(ref, e);
	}
	public void addFailure(Reference ref, ScrapingFailure e) {
		failures.put(ref, e);
	}
	public boolean hasDelay() {
		return delays.size() > 0;
	}
	public boolean hasFailure() {
		return failures.size() > 0;
	}
	public String[] successes() {
		String[] successes = new String[this.successes.size()];
		this.successes.copyInto(successes);
		return successes;
	}
	// convenience method
	/*private static ScrapingProblem[] vectorToExecutionProblemArray(Vector vector) {
		ScrapingProblem[] problems = new ScrapingProblem[vector.size()];
		vector.copyInto(problems);
		return problems;
	}*/
	public ScrapingProblem[] delays() {
		return vectorToExecutionProblemArray(delays);
	}
	public ScrapingProblem[] failures() {
		return vectorToExecutionProblemArray(failures);
	}
	
	// If any previous delay is no longer in our status, we have made progress.
	public boolean hasProgressedSince(Status lastStatus) {
		for(int i = 0 ; i < lastStatus.delays.size() ; i ++ ) {
			ScrapingProblem previousDelay = (ScrapingProblem) lastStatus.delays.elementAt(i);
			if(!this.delays.contains(previousDelay))
				return true;
		}
		return false;
	}
}