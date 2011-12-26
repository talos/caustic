package net.caustic.console;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
	final Scraper scraper;
	private final Output output;
	
	private final MultiLog log = new MultiLog();
	private final Set<String> ready = Collections.synchronizedSet(new HashSet<String>());
	private final Queue<RunnableRequest> wait = new LinkedList<RunnableRequest>();
	
	private final int nThreads;
	private final AtomicInteger id = new AtomicInteger(0);
	
	Requester(Scraper scraper, int nThreads, Output output) {
		this.scraper = scraper;
		this.nThreads = nThreads;
		exc = Executors.newFixedThreadPool(nThreads);
		
		this.output = output;
	}

	public void request(String instruction, String uri,
			Map<String, String> inputMap) {
		request(uuid(), instruction, uri, null, new CollectionStringMap(inputMap), new String[] {}, false);
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void join() throws InterruptedException {
		while(true) {
			if(ready.size() == 0 || exc.isShutdown()) {
				exc.shutdownNow();
				break;
			}
			
			Thread.sleep(100);
		}
	}
	
	void finished(Request request, CollectionStringMap requestTags, Response response) {
		log.i("Finished " + StringUtils.quote(request.instruction) + "(" + request.id + ")");
		
		final String[] childCookies;
		
		// launch children from Find
		if(response.values != null) {
			childCookies = request.cookies;
			
			final boolean isBranch = response.values.length > 1;
			for(int i = 0 ; i < response.values.length ; i ++) {
				// child input is destructive, if only one value is in response
				CollectionStringMap tags = isBranch ? requestTags.branch(new HashMap<String, String>()) : requestTags;
				tags.put(response.name, response.values[i]);
				String id = uuid();
				
				output.print(id, request.id, response.name, response.values[i]);
				for(String child : response.children) {
					request(id, child, response.uri, response.content, tags, childCookies, false);
				}
			}
		} else if(response.content != null) { // launch children from Load
			// child cookies are destructive, if only one value is in response
			//ArrayList<String> childCookies = (ArrayList<String>) cookies.clone();
			childCookies = new String[request.cookies.length
					+ response.cookies.length];
			System.arraycopy(request.cookies, 0, childCookies, 0, request.cookies.length);
			System.arraycopy(response.cookies, 0, childCookies, request.cookies.length,
					response.cookies.length);
			
			//request(child, response.uri, response.content, input, childCookies, true);
			
			for(String child : response.children) {
				request(uuid(), child, response.uri, response.content, requestTags, childCookies, false);
			}
		} else {
			throw new RuntimeException("Invalid response, does not have content or values.");
		}

		remove(response.id);
	}
	
	void queue(RunnableRequest request) {
		synchronized(wait) {
			wait.offer(request);
		}
		remove(request.request.id);
	}
	
	void stuck(Request request, Response response) {
		log.i("Stuck on " + StringUtils.quote(request.instruction)  + "(" + request.id + ")"
				+ " because of missing tags: " + Arrays.asList(response.missingTags)
				+ " within " + request.tags.toString());
		remove(response.id);
	}
	
	void failed(Request request, Response response) {
		log.i("Failed on " + StringUtils.quote(request.instruction) + "(" + request.id + ")"
				+ " because of " + StringUtils.quote(response.failedBecause));
		remove(response.id);
	}
	
	void interrupt(Throwable why) {
		log.e(why);
		exc.shutdownNow();
	}

	/**
	 * 
	 * @return A {@link String} UUID.
	 */
	private String uuid() {
		return String.valueOf(id.getAndIncrement());
	}
	
	private void request(String id, String instruction, String uri,
			String input, CollectionStringMap tags,
			String[] cookies, boolean force) {
		request(new RunnableRequest(this, id, instruction, uri, input, tags, cookies, force));
	}

	private void request(RunnableRequest req) {
		ready.add(req.request.id);
		exc.submit(req);
	}
	
	private void remove(String id) {
		ready.remove(id);
		while(ready.size() < nThreads && !wait.isEmpty()) {
			RunnableRequest req = wait.poll();
			// submit with force
			request(req);
		}
	}
}