package net.microscraper;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.executable.Executable;
import net.microscraper.executable.PageExecutable;
import net.microscraper.executable.Result;
import net.microscraper.instruction.DeserializationException;
import net.microscraper.instruction.Page;
import net.microscraper.instruction.Scraper;
import net.microscraper.interfaces.browser.Browser;
import net.microscraper.interfaces.browser.BrowserException;
import net.microscraper.interfaces.json.JSONInterface;
import net.microscraper.interfaces.json.JSONInterfaceException;
import net.microscraper.interfaces.json.JSONLocation;
import net.microscraper.interfaces.publisher.Publisher;
import net.microscraper.interfaces.publisher.PublisherException;
import net.microscraper.interfaces.regexp.RegexpCompiler;

/**
 * A microscraper {@link Client} can scrape a {@link Instruction}.
 * @author john
 *
 */
public final class Client {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle this warning differently
	private final Interfaces interfaces;
	
	/**
	 * @param regexpCompiler The {@link RegexpCompiler} to use when compiling regular
	 * expressions.
	 * @param log The {@link Log} this {@link Client} should use.
	 * @param browser A {@link Browser} to use for HTTP requests.
	 * @param jsonInterface A {@link JSONInterface} to use when
	 * loading {@link JSONInterfaceObject}s.
	 */
	public Client(RegexpCompiler regexpCompiler,
			Log log, Browser browser, JSONInterface jsonInterface) {
		this.interfaces = new Interfaces(log,
				regexpCompiler, browser,
				jsonInterface);
	}
	
	/**
	 * 
	 * @param pageLocation A {@link JSONLocation} with the {@link Scraper}'s instructions.
	 * @param extraVariables An array of {@link NameValuePair}s to use initially.
	 * @param publisher A {@link Publisher} to send the results of {@link Executable}s to.
	 * @throws BrowserException If a {@link Browser} problem prevented the {@link Scraper} from running.
	 * @throws ClientException If the {@link Scraper} could not be run.
	 */
	public void scrape(JSONLocation pageLocation, NameValuePair[] extraVariables,
			Publisher publisher) throws BrowserException, ClientException {
		try {
			Page page = new Page(interfaces.getJSONInterface().load(pageLocation));
			
			Executable rootExecutable = new PageExecutable(interfaces,
					page, new DefaultVariables(extraVariables), 
					null);
			execute(rootExecutable, publisher);
		} catch(IOException e) {
			throw new ClientException(e);
		} catch (DeserializationException e) {
			throw new ClientException(e);
		} catch (JSONInterfaceException e) {
			throw new ClientException(e);
		}
	}
	
	/**
	 * {@link #execute} runs an {@link Executable} and its children, publishing them after each run.
	 * @param rootExecutable The {@link Executable} to start with.
	 * @param publisher The {@link Publisher} to publish to after each execution.
	 * @throws PublisherException if the {@link Publisher} experienced an error.
	 */
	private void execute(Executable rootExecutable, Publisher publisher) throws PublisherException {
		Vector queue = new Vector();
		queue.add(rootExecutable);
		while(queue.size() > 0) {
			if(queue.size() > LARGE_QUEUE) {
				interfaces.getLog().i("Large execution queue: " + Utils.quote(queue.size()));
			}
			Executable exc = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			exc.run();
			// If the execution is complete, add its children to the queue.
			if(exc.isComplete()) {
				Result[] results = exc.getResults();
				for(int i = 0 ; i < results.length ; i++) {
					results[i].publishTo(publisher);
				}
				Executable[] children = exc.getChildren();
				Utils.arrayIntoVector(children, queue);
			} else if (exc.isStuck()) {
				//publisher.publishStuck(exc.getSource().getId(), exc.getId(), exc.stuckOn());
			} else if (exc.hasFailed()) {
				//publisher.publishFailure(exc.getSource().getId(), exc.getId(), exc.failedBecause());
			// If the execution is not stuck and is not failed, add it back to the queue.
			} else {
				queue.addElement(exc);
			}
		}
	}
}