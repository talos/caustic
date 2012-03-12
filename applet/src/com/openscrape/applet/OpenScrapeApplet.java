package com.openscrape.applet;

import java.applet.Applet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.json.me.JSONObject;

import net.caustic.DefaultScraper;
import net.caustic.Request;
import net.caustic.Scraper;

/**
 * Provides interface between browser and scraper applet through public methods.
 * @author john
 *
 */
public class OpenScrapeApplet extends Applet {
	private static final long serialVersionUID = 2768937336583253219L;
	
	// thanks http://stackoverflow.com/questions/1272648/need-to-read-own-jars-manifest-and-not-root-classloaders-manifest
	private static final String className = OpenScrapeApplet.class.getSimpleName() + ".class";
	private static final String classPath = OpenScrapeApplet.class.getResource(className).toString();
	private static final String manifestPath;
	
	static {
		manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
	}
	
	private class Scrape implements Callable<String> {
		private final String request;
			
		public Scrape(String request) {
			this.request = request;
		}
		
		@Override
		public String call() throws Exception {
			try {
				return AccessController.doPrivileged(new ScrapeAction());
			} catch(Throwable e) {
				throw new ExecutionException(e);
			}
		}
		
		private class ScrapeAction implements PrivilegedAction<String> {
			public String run() {
				try {
					return scraper.scrape(Request.fromJSON(request)).serialize();
				} catch(Throwable e) {
					e.printStackTrace();
					errors.add(e);
					return null;
				}
			}
		}
	}
	
	private final Scraper scraper = new DefaultScraper();
	private ExecutorService svc;
	private Future<String> inProgress;
	
	private final Queue<Throwable> errors = new LinkedList<Throwable>();

	@Override
	public void init() {
		svc = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void start() {
	}
	
	@Override
	public void stop() {
	}
	
	@Override
	public void destroy() {
		svc.shutdownNow();
	}
	
	/**
	 * Obtain a JSON representation of the applet's manifest attributes.
	 * @return A JSON String representing the applet's manifest attributes.  If
	 * <code>null</code> is returned, something went wrong: use
	 * {@link #pollError()} to find out what.
	 */
	public String getAttributes() {
		try {
			Attributes attrs = new Manifest(new URL(manifestPath).openStream())
						.getMainAttributes();
									
			JSONObject obj = new JSONObject();
			for(Object key : attrs.keySet()) {
				Attributes.Name name = (Attributes.Name) key; 
				obj.put(name.toString(), (String) attrs.getValue(name));
			}
			
			return obj.toString();
		} catch (Throwable e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}
	
	/**
	 * Start scraping a request.
	 * @param jsonRequest A request serialized in JSON.
	 * @param boolean <code>true</code> if the request was started, <code>false</code> otherwise.
	 * Requests won't be started if something went wrong, or if there is already a request being executed.
	 */
	public boolean request(String jsonRequest) {
		try {
			if(isAvailable()) {
				inProgress = svc.submit(new Scrape(jsonRequest));
				return true;
			} else {
				return false;
			}
		} catch(Throwable e) {
			e.printStackTrace();
			errors.add(e);
			return false;
		}
	}
	
	/**
	 * Cancel a pending request.
	 * @return <code>true</code> if the request was cancelled, <code>false</code> if there was no request to cancel
	 * or something went wrong.
	 */
	public boolean cancel() {
		try {
			if(isAvailable()) {
				return false;
			} else {
				inProgress.cancel(true);
				inProgress = null;
				return true;
			}
		} catch(Throwable e) {
			e.printStackTrace();
			errors.add(e);
			return false;
		}
	}
	
	/**
	 * Poll the applet for the response to a pending request.
	 * @return A JSON String representing the response, if one is available, or <code>null</code> if
	 * there is none.  You may wish to check {@link #pollError()} to see if some exception has
	 * prevented a request from returning.
	 */
	public String poll() {
		try {
			if(inProgress != null) {
				if(inProgress.isDone() == true) {
					String result = inProgress.get();
					inProgress = null;
					return result;
				}
			}
			return null;
		} catch(Throwable e) {
			e.printStackTrace();
			errors.add(e);
			return null;
		}
	}
	
	/**
	 * Is the applet able to take another request?  This is only true if it is not currently processing any requests,
	 * and if it is not holding the results of the last request.  Use {@link #poll()} to get the response from
	 * the last request.
	 * @return <code>true</code> if the applet is free to take another request, <code>false</code> if it is not.
	 * Will also return <code>false</code> if something went wrong checking, in which case you may wish to check
	 * {@link #pollError()}.
	 */
	public boolean isAvailable() {
		try {
			return inProgress == null;
		} catch(Throwable e) {
			e.printStackTrace();
			errors.add(e);
			return false;
		}
	}
	
	/**
	 * Poll the applet for any errors that occurred during execution.
	 * @return A {@link String} representation of the error, including the trace,
	 * or <code>null</code> if there are no exceptions.
	 */
	public String pollError() {
		try {
			// Extract the stack trace using StringWriter
			StringWriter strWriter = new StringWriter();
			Throwable err = errors.poll();
			if(err != null) {
				err.printStackTrace(new PrintWriter(strWriter));
				
				return new StringBuffer(err.toString())
								.append("; trace: ")
								.append(strWriter.getBuffer()).toString();
			} else {
				return null;
			}
		} catch(Throwable e) {
			e.printStackTrace();
			return "Error in pollError: " + e.toString();
		}
	}
}
