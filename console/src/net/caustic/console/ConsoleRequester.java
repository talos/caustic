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
import net.caustic.http.HashtableCookies;
import net.caustic.log.Loggable;
import net.caustic.log.Logger;
import net.caustic.log.MultiLog;
import net.caustic.util.CollectionStringMap;
import net.caustic.util.StringUtils;

class ConsoleRequester implements Loggable, Requester {

	private final ExecutorService exc;
	final Scraper scraper;
	private final Output output;
	
	private final MultiLog log = new MultiLog();
	private final Set<String> ready = Collections.synchronizedSet(new HashSet<String>());
	private final Queue<RunnableRequest> wait = new LinkedList<RunnableRequest>();
	
	private final int nThreads;
	private final AtomicInteger id = new AtomicInteger(0);
	
	ConsoleRequester(Scraper scraper, int nThreads, Output output) {
		this.scraper = scraper;
		this.nThreads = nThreads;
		exc = Executors.newFixedThreadPool(nThreads);
		
		this.output = output;
	}

	public void request(String instruction, String uri,
			Map<String, String> inputMap) {
		request(uuid(), instruction, uri, null, new CollectionStringMap(inputMap), new HashtableCookies(), false);
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
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#finished(net.caustic.Request, net.caustic.util.CollectionStringMap, net.caustic.Response)
	 */
	@Override
	public void finished(RunnableRequest rRequest, Response response) {
		final Request request = rRequest.request;
		final CollectionStringMap requestTags = rRequest.tags;

		log.i("Finished " + StringUtils.quote(request.instruction) + "(" + request.id + ")");
		
		// launch children from Find, allow cookie destruction
		if(response.values != null) {
			final boolean isBranch = response.values.length > 1;

			for(int i = 0 ; i < response.values.length ; i ++) {
				
				final String id;
				final CollectionStringMap tags;
				final HashtableCookies childCookies;
				
				// if this is a branch, generate new id, branch cookies, and branch tags
				if(isBranch) {
					id = uuid();
					childCookies = rRequest.cookies.branch();
					tags = requestTags.branch(new HashMap<String, String>());
				} else {
					id = request.id;
					childCookies = rRequest.cookies;
					tags = requestTags;
				}
				tags.put(response.name, response.values[i]);
				
				output.print(id, request.id, response.name, response.values[i]);
				for(String child : response.children) {
					request(id, child, response.uri, response.content, tags, childCookies, false);
				}
			}
		} else if(response.content != null) { // launch children from Load, allow cookie destruction
			HashtableCookies childCookies = rRequest.cookies;
			childCookies.extend(response.cookies);
			for(String child : response.children) {
				request(response.id, child, response.uri, response.content, requestTags, childCookies, false);
			}
		} else {
			throw new RuntimeException("Invalid response, does not have content or values.");
		}

		remove(response.id);
	}
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#loadQueue(net.caustic.RunnableRequest)
	 */
	@Override
	public void loadQueue(RunnableRequest rRequest, Response response) {
		Request req = rRequest.request;
		// add to missing tags queue with force enabled
		missingTagsQueue(new RunnableRequest(this, scraper, req.id, req.instruction, req.uri, req.input,
				rRequest.tags, rRequest.cookies, true), response);
	}
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#missingTagsQueue(net.caustic.RunnableRequest)
	 */
	@Override
	public void missingTagsQueue(RunnableRequest rRequest, Response response) {
		synchronized(wait) {
			wait.offer(rRequest);
		}
		remove(rRequest.request.id);
	}
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#stuck(net.caustic.Request, net.caustic.Response)
	 */
	@Override
	public void stuck(RunnableRequest rRequest, Response response) {
		final Request request = rRequest.request;
		log.i("Stuck on " + StringUtils.quote(request.instruction)  + "(" + request.id + ")"
				+ " because of missing tags: " + Arrays.asList(response.missingTags)
				+ " within " + request.tags.toString());
		remove(response.id);
	}
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#failed(net.caustic.Request, net.caustic.Response)
	 */
	@Override
	public void failed(RunnableRequest rRequest, Response response) {
		final Request request = rRequest.request;
		log.i("Failed on " + StringUtils.quote(request.instruction) + "(" + request.id + ")"
				+ " because of " + StringUtils.quote(response.failedBecause));
		remove(response.id);
	} 
	
	/* (non-Javadoc)
	 * @see net.caustic.Requester#interrupt(java.lang.Throwable)
	 */
	@Override
	public void interrupt(Throwable why) {
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
			HashtableCookies cookies, boolean force) {
		request(new RunnableRequest(this, scraper, id, instruction, uri, input, tags,
				cookies, force));
	}

	private void request(RunnableRequest req) {
		ready.add(req.request.id);
		exc.submit(req);
	}
	
	private void remove(String id) {
		ready.remove(id);
		synchronized(wait) {
			while(ready.size() < nThreads && !wait.isEmpty()) {
				RunnableRequest req = wait.poll();
				// submit with force
				request(req);
			}
		}
	}
}