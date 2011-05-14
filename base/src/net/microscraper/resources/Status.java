package net.microscraper.resources;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.resources.definitions.Reference;

public class Status {
	private Hashtable successes = new Hashtable();
	private Hashtable delays = new Hashtable();
	private Hashtable failures = new Hashtable();
	
	public void addSuccess(Reference ref, String success){
		successes.put(ref, success);
	}
	public void addDelay(Reference ref, ExecutionDelay e) {
		delays.put(ref, e);
	}
	public void addFailure(Reference ref, ExecutionFailure e) {
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
	private static ExecutionProblem[] vectorToExecutionProblemArray(Vector vector) {
		ExecutionProblem[] problems = new ExecutionProblem[vector.size()];
		vector.copyInto(problems);
		return problems;
	}
	public ExecutionProblem[] delays() {
		return vectorToExecutionProblemArray(delays);
	}
	public ExecutionProblem[] failures() {
		return vectorToExecutionProblemArray(failures);
	}
	
	// If any previous delay is no longer in our status, we have made progress.
	public boolean hasProgressedSince(Status lastStatus) {
		for(int i = 0 ; i < lastStatus.delays.size() ; i ++ ) {
			ExecutionProblem previousDelay = (ExecutionProblem) lastStatus.delays.elementAt(i);
			if(!this.delays.contains(previousDelay))
				return true;
		}
		return false;
	}
}