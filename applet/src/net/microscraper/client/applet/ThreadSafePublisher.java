package net.microscraper.client.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.microscraper.client.Publisher;
import net.microscraper.client.ResultSet.Result;

public class ThreadSafePublisher implements Publisher {
	List<Result> results = Collections.synchronizedList(new ArrayList<Result>());
	
	@Override
	public void publish(Result result) throws PublisherException {
		results.add(result);
	}

	/**
	 * Pull out the oldest result.
	 * @return
	 */
	public Result unshift() {
		try {
			return results.remove(0);
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}
}
