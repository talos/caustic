package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Stringer;
import net.microscraper.client.Interfaces.JSON.Writer;
import net.microscraper.client.Publisher;
import net.microscraper.execution.ScraperExecution;
import net.microscraper.execution.Status;
import net.microscraper.execution.ScraperExecution.ExecutionProblem;

public class ThreadSafeJSONPublisher implements Publisher {
	private final List<Stringer> executions = Collections.synchronizedList(new ArrayList<Stringer>());
	private Integer pos = -1;
	private final Interfaces.JSON json;
	public ThreadSafeJSONPublisher(Interfaces.JSON json) {
		this.json = json;
	}
	private static void appendProblemToJSON(ExecutionProblem problem, Writer writer) throws JSONInterfaceException {
		writer.object()
			.key(SOURCE_ID)
			.value(problem.callerExecution().id)
			.key(TYPE)
			.value(problem.problemClass().getSimpleName())
			.key(VALUE)
			.value(problem.reason())
			.endObject();
	}
	
	@Override
	public void publish(ScraperExecution execution, Status status) throws PublisherException {
		try {
			Stringer stringer = json.getStringer();
			stringer.object()
				.key(ID).value(execution.id)
				.key(SOURCE_ID).value(execution.getSourceExecution().id)
				.key(NAME).value(execution.getPublishName());
			
			String[] successes = status.successes();
			stringer.key(SUCCESS).array();
			for(int i = 0 ; i < successes.length ; i ++) {
				stringer.value(successes[i]);
			}
			stringer.endArray();
			
			ExecutionProblem[] delays = status.delays();
			stringer.key(DELAY).array();
			for(int i = 0 ; i < delays.length ; i ++) {
				appendProblemToJSON(delays[i], stringer);
			}
			stringer.endArray();
			
			ExecutionProblem[] failures = status.failures();
			stringer.key(FAILURE).array();
			for(int i = 0 ; i < failures.length ; i ++) {
				appendProblemToJSON(failures[i], stringer);
			}
			stringer.endArray();
			
			stringer.endObject();
			synchronized(executions) {
				executions.add(stringer);
			}
		} catch(JSONInterfaceException e) {
			throw new PublisherException(e);
		}
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
	public Stringer next() {
		synchronized(executions) {
			synchronized(pos) {
				pos++;
				return executions.get(pos);
			}
		}
	}
}
