package net.microscraper.client.applet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.Interfaces.JSON.Stringer;
import net.microscraper.client.Publisher;
import net.microscraper.database.Execution;
import net.microscraper.database.Execution.ExecutionDelay;
import net.microscraper.database.Execution.ExecutionFailure;
import net.microscraper.database.Status;

public class ThreadSafeJSONPublisher implements Publisher {
	private final Map<Execution, String> executions = Collections.synchronizedMap(new HashMap<Execution, String>());
	private Iterator<Execution> iter = null;

	public boolean live() {
		return true;
	}
	public int size() {
		return executions.size();
	}
	
	public String get(Execution exc) throws PublisherException {
		try {
			String value = executions.get(exc);
			Status status = exc.getStatus();
			String statusString = SUCCESSFUL;
			if(status.hasDelay()) {
				statusString = DELAY;
			} else if(status.hasFailure()) {
				statusString = FAILURE;
			}
			//Status status = exc.getStatus();
			Stringer stringer = Client.json.getStringer();
			stringer.object()
				.key(ID).value(exc.id)
				.key(SOURCE_ID).value(exc.getSourceExecution().id)
				.key(NAME).value(exc.getPublishName())
				.key(STATUS_STRING).value(statusString)
				.key(VALUE).value(value);
			
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
	public void publish(Execution execution, String result)
			throws PublisherException {
		executions.put(execution, result);		
	}
	@Override
	public void publish(Execution execution, ExecutionDelay delay)
			throws PublisherException {
		executions.put(execution, delay.reason());		
	}
	@Override
	public void publish(Execution execution, ExecutionFailure failure)
			throws PublisherException {
		executions.put(execution, failure.reason());		
	}
}
