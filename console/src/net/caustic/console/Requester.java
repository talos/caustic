package net.caustic.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

class Requester implements Loggable {

	private final ExecutorService exc;
	private final Scraper scraper;
	
	private final Map<String, CollectionStringMap> inputs = new HashMap<String, CollectionStringMap>();
	private final Map<String, String[]> cookieJars = new HashMap<String, String[]>();
	private final MultiLog log = new MultiLog();
	
	Requester(Scraper scraper, int nThreads) {
		this.scraper = scraper;
		exc = Executors.newFixedThreadPool(nThreads);
	}

	public void request(String instruction, String uri,
			String input, CollectionStringMap tags,
			String[] cookies, boolean force) {
		String id = UUID.randomUUID().toString();
		inputs.put(id, tags);
		exc.submit(new RunnableRequest(new Request(id, instruction, uri, input,
				tags, cookies, force)));
	}
	
	public void register(Logger logger) {
		log.register(logger);
	}
	
	public void join() throws InterruptedException {
		while(true) {
			if(inputs.size() == 0) {
				break;
			}
			Thread.sleep(100);
		}
	}
	
	void finished(Response response) {
		CollectionStringMap input = inputs.get(response.id);
		String[] cookies = cookieJars.get(response.id);
				
		// launch children
		for(String child : response.children) {
			
			// launch children from Find
			if(response.values != null) {
				final boolean isBranch = response.values.length > 1;
				for(String value : response.values) {
											
					// child input is destructive, if only one value is in response
					CollectionStringMap childInput = isBranch ? input : input.branch();
					childInput.put(response.name, value);
					
					request(child, response.uri, response.content, childInput, cookies, true);

				}
			} else if(response.content != null) { // launch children from Load
				// child cookies are destructive, if only one value is in response
				//ArrayList<String> childCookies = (ArrayList<String>) cookies.clone();
				String[] childCookies = new String[cookies.length
						+ response.cookies.length];
				System.arraycopy(cookies, 0, childCookies, 0, cookies.length);
				System.arraycopy(response.cookies, 0, childCookies, cookies.length,
						response.cookies.length);

				request(child, response.uri, response.content, input, childCookies, true);
			}
		}
	}
	
	void retry(RunnableRequest request) {
		exc.submit(request);
	}
	
	void stuck(Response response) {
		log.i("stuck");
	}
	
	void failed(Response response) {
		log.i("failed");
	}
	
	void interrupt() {
		exc.shutdownNow();
		cookieJars.clear();
		inputs.clear();
	}
	
	private void clear(String id) {
		cookieJars.remove(id);
		inputs.remove(id);
	}
	
	private class RunnableRequest implements Runnable {
		private final Request request;
		private List<String> lastMissingTags;
		
		RunnableRequest(Request request) {
			this.request = request;
		}
		
		public void run() {
			try {
				Response response = scraper.scrape(request);
				if(response.children != null) {
					finished(scraper.scrape(request));
					clear(request.id);
				} else if (response.missingTags != null) {
					if(lastMissingTags != null) {
						if(lastMissingTags.containsAll(Arrays.asList(response.missingTags))) {
							stuck(response);
							clear(request.id);
						} else {
							retry(this);
						}
					} else {
						retry(this);
					}
				} else {
					failed(response);
					clear(request.id);
				}
			} catch(InterruptedException e) {
				interrupt();
			}
		}
	}
}
