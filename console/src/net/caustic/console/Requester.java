package net.caustic.console;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private final Scraper scraper;
	private final Output output;
	
	//private final Map<String, CollectionStringMap> inputs = new HashMap<String, CollectionStringMap>();
	//private final Map<String, String[]> cookieJars = new HashMap<String, String[]>();
	private final MultiLog log = new MultiLog();
	private final Set<String> running = new HashSet<String>();
	
	private final AtomicInteger id = new AtomicInteger(0);
	
	Requester(Scraper scraper, int nThreads, Output output) {
		this.scraper = scraper;
		exc = Executors.newFixedThreadPool(nThreads);
		this.output = output;
	}

	public void request(String instruction, String uri,
			Map<String, String> inputMap) {
		request(instruction, uri, null, new CollectionStringMap(uuid(), inputMap), new String[] {}, true);
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void join() throws InterruptedException {
		while(true) {
			if(running.size() == 0 || exc.isShutdown()) {
				exc.shutdownNow();
				break;
			}
			
			Thread.sleep(100);
		}
	}
	
	private void finished(Request request, CollectionStringMap requestTags, Response response) {
		log.i("Finished " + StringUtils.quote(request.instruction) + "(" + request.id + ")");
				
		final CollectionStringMap[] childTags;
		final String[] childCookies;
		
		// launch children from Find
		if(response.values != null) {
			childCookies = request.cookies;
			childTags = new CollectionStringMap[response.values.length];
			
			final boolean isBranch = response.values.length > 1;
			for(int i = 0 ; i < response.values.length ; i ++) {
				// child input is destructive, if only one value is in response
				childTags[i] = isBranch ? requestTags.branch(uuid(), new HashMap<String, String>()) : requestTags;
				childTags[i].put(response.name, response.values[i]);

				output.print(childTags[i].id, requestTags.id, response.name, response.values[i]);
			}
		} else if(response.content != null) { // launch children from Load
			// child cookies are destructive, if only one value is in response
			//ArrayList<String> childCookies = (ArrayList<String>) cookies.clone();
			childCookies = new String[request.cookies.length
					+ response.cookies.length];
			System.arraycopy(request.cookies, 0, childCookies, 0, request.cookies.length);
			System.arraycopy(response.cookies, 0, childCookies, request.cookies.length,
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
	
	private void retry(RunnableRequest request) {
		exc.submit(request);
		running.remove(request.request.id);
	}
	
	private void stuck(Request request, Response response) {
		log.i("Stuck on " + StringUtils.quote(request.instruction)  + "(" + request.id + ")"
				+ " because of missing tags: " + Arrays.asList(response.missingTags)
				+ " within " + request.tags.toString());
		running.remove(response.id);
	}
	
	private void failed(Request request, Response response) {
		log.i("Failed on " + StringUtils.quote(request.instruction) + "(" + request.id + ")"
				+ " because of " + StringUtils.quote(response.failedBecause));
		running.remove(response.id);
	}
	
	private void interrupt(Throwable why) {
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
	
	private void request(String instruction, String uri,
			String input, CollectionStringMap tags,
			String[] cookies, boolean force) {
		running.add(tags.id);
		exc.submit(new RunnableRequest(instruction, uri, input, tags, cookies, force));
	}
	
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
					
					finished(request, tags, response);
					//clear(request.id);
				} else if (response.missingTags != null) {
					if(lastMissingTags != null) {
						if(lastMissingTags.containsAll(Arrays.asList(response.missingTags))) {
							stuck(request, response);
							//clear(request.id);
						} else {
							retry(this);
						}
					} else {
						retry(this);
					}
				} else {
					failed(request, response);
					//clear(request.id);
				}
			} catch(InterruptedException e) {
				interrupt(e);
			} catch(Throwable e) {
				interrupt(e);
			}
		}
	}
	
}