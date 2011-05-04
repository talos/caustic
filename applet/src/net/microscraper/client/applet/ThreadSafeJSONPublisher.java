package net.microscraper.client.applet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Stringer;
import net.microscraper.client.Interfaces.JSON.Writer;
import net.microscraper.client.Publisher;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionProblem;
import net.microscraper.database.Status;

public class ThreadSafeJSONPublisher implements Publisher {
	private final Map<Execution, Status> executions = Collections.synchronizedMap(new HashMap<Execution, Status>());
	private Iterator<Execution> iter = null;

	public boolean live() {
		return true;
	}
	public int size() {
		return executions.size();
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
	public String get(Execution exc) throws PublisherException {
		try {
			Status status = executions.get(exc);
			
			Stringer stringer = Client.json.getStringer();
			stringer.object()
				.key(ID).value(exc.id)
				.key(SOURCE_ID).value(exc.getSourceExecution().id)
				.key(NAME).value(exc.getPublishName());
			
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
			return stringer.toString();
		} catch(JSONInterfaceException e) {
			throw new PublisherException(e);
		}
	}
	
	public String next() throws PublisherException {
		if(iter == null)
			iter = executions.keySet().iterator();
		
		if(iter.hasNext()) {
			return get(iter.next());
		} else {
			iter = null;
			return null;
		}
	}
	@Override
	public void publish(Execution execution, Status status) throws PublisherException {
		executions.put(execution, status);
	}
}
