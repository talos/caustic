package net.microscraper.resources;

import java.util.Vector;

import net.microscraper.client.Utils;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionDelay;
import net.microscraper.resources.DefaultExecutionProblem.ExecutionFailure;

public class Status {
	private Vector successes = new Vector();
	private Vector delays = new Vector();
	private Vector failures = new Vector();
	
	public void addSuccess(Result success){
		successes.addElement(success);
	}
	public void addDelay(ExecutionDelay e) {
		delays.addElement(e);
	}
	public void addFailure(ExecutionFailure e) {
		failures.addElement(e);
	}
	public void merge(Status other) {
		Utils.vectorIntoVector(other.successes, this.successes);
		Utils.vectorIntoVector(other.delays, this.delays);
		Utils.vectorIntoVector(other.failures, this.failures);
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