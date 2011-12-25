package net.caustic.console;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.caustic.Request;
import net.caustic.Response;
import net.caustic.Scraper;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringUtils;

class Requester implements Loggable {

	private final ExecutorService exc;
	private final Scraper scraper;
	
	//private final Map<String, CollectionStringMap> inputs = new HashMap<String, CollectionStringMap>();
	//private final Map<String, String[]> cookieJars = new HashMap<String, String[]>();
	private final MultiLog log = new MultiLog();
	private final Set<String> running = new HashSet<String>();
	
	Requester(Scraper scraper, int nThreads) {
		this.scraper = scraper;
		exc = Executors.newFixedThreadPool(nThreads);
	}

	public void request(String instruction, String uri,
			String input, CollectionStringMap tags,
			String[] cookies, boolean force) {
		//String id = UUID.randomUUID().toString();
		//inputs.put(id, tags);
		running.add(tags.id);
		exc.submit(new RunnableRequest(instruction, uri, input,
				tags, cookies, force));
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void join() throws InterruptedException {
		while(true) {
			if(running.size() == 0) {
				break;
			}
			
			Thread.sleep(100);
		}
	}
	
	void finished(CollectionStringMap requestTags, String[] requestCookies, Response response) {
		log.i("Finished " + StringUtils.quote(response.uri));
				
		final CollectionStringMap[] childTags;
		final String[] childCookies;
		
		// launch children from Find
		if(response.values != null) {
			childCookies = requestCookies;
			childTags = new CollectionStringMap[response.values.length];
			
			final boolean isBranch = response.values.length > 1;
			for(int i = 0 ; i < response.values.length ; i ++) {
				// child input is destructive, if only one value is in response
				childTags[i] = isBranch ? requestTags : requestTags.branch(uuid(), new HashMap<String, String>());
				childTags[i].put(response.name, response.values[i]);

				Output.print(requestTags.getParentId(), response.id, response.name, response.values[i]);
			}
		} else if(response.content != null) { // launch children from Load
			// child cookies are destructive, if only one value is in response
			//ArrayList<String> childCookies = (ArrayList<String>) cookies.clone();
			childCookies = new String[requestCookies.length
					+ response.cookies.length];
			System.arraycopy(requestCookies, 0, childCookies, 0, requestCookies.length);
			System.arraycopy(response.cookies, 0, childCookies, requestCookies.length,
					response.cookies.length);
			
			childTags = new CollectionStringMap[] { requestTags };
			//request(child, response.uri, response.content, input, childCookies, true);
		} else {
			throw new RuntimeException("Invalid response, does not have content or values.");
		}
				
		// launch children
		for(CollectionStringMap tags : childTags) {
			for(String child : response.children) {
				request(child, response.uri, response.content, tags, childCookies, true);
			}
		}
		running.remove(response.id);
	}
	
	void retry(RunnableRequest request) {
		exc.submit(request);
		running.remove(request.request.id);
	}
	
	void stuck(Response response) {
		running.remove(response.id);
		log.i("Stuck on " + StringUtils.quote(response.uri) + " because of missing tags: " + Arrays.asList(response.missingTags));
	}
	
	void failed(Response response) {
		running.remove(response.id);
		log.i("Failed on " + StringUtils.quote(response.uri) + " because of " + StringUtils.quote(response.failedBecause));
	}
	
	void interrupt() {
		exc.shutdownNow();
		//cookieJars.clear();
		//inputs.clear();
	}
	/*
	private void clear(String id) {
		cookieJars.remove(id);
		inputs.remove(id);
	}
	*/
	private class RunnableRequest implements Runnable {
		private final Request request;
		private List<String> lastMissingTags;
		private final CollectionStringMap tags;
		
		RunnableRequest(String instruction, String uri, String input,
				CollectionStringMap tags, String[] cookies, boolean force) {
			this.tags = tags;
			this.request = new Request(tags.id, instruction, uri, input,
					tags, cookies, force);
		}
		
		public void run() {
			try {
				Response response = scraper.scrape(request);
				if(response.children != null) {
					
					finished(tags, request.cookies, scraper.scrape(request));
					//clear(request.id);
				} else if (response.missingTags != null) {
					if(lastMissingTags != null) {
						if(lastMissingTags.containsAll(Arrays.asList(response.missingTags))) {
							stuck(response);
							//clear(request.id);
						} else {
							retry(this);
						}
					} else {
						retry(this);
					}
				} else {
					failed(response);
					//clear(request.id);
				}
			} catch(InterruptedException e) {
				interrupt();
			}
		}
	}
	
	/**
	 * 
	 * @return A {@link String} UUID.
	 */
	private static String uuid() {
		return UUID.randomUUID().toString();
	}
}
