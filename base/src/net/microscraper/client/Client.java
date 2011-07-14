package net.microscraper.client;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.executable.BasicResult;
import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.PageExecutable;
import net.microscraper.client.executable.Result;
import net.microscraper.client.executable.SpawnedScraperExecutable;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.BrowserException;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.RegexpCompiler;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.instruction.Page;
import net.microscraper.server.instruction.Scraper;

/**
 * A microscraper {@link Client} can scrape a {@link Instruction}.
 * @author john
 *
 */
public final class Client {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle this warning differently
	private final Interfaces interfaces;
	
	/**
	 * @param log The {@link Log} this {@link Client} should use.
	 * @param encoding The encoding to use when encoding or decoding post data, cookies,
	 * and JSON resources. "UTF-8" is recommended.
	 * @param netInterface A {@link NetInterface} to use when handling {@link URIInterface} and
	 * {@link URLInterface}.
	 * @param jsonInterface A {@link JSONInterface} to use when loading {@link JSONInterfaceObject}s.
	 * @param regexpCompiler The {@link RegexpCompiler} interface to use when compiling regexps.
	 */
	public Client(RegexpCompiler regexpCompiler,
			Log log, NetInterface netInterface, JSONInterface jsonInterface,
			String encoding) {
		this.interfaces = new Interfaces( log,
				regexpCompiler, netInterface,
				jsonInterface, encoding);
	}
	
	/**
	 * 
	 * @param pageLocation A {@link java.net.URI} to get the {@link Scraper} instructions from.
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to stock the {@link SpawnedScraperExecutable}s
	 * {@link FindOne}s with.
	 * @param publisher A {@link Publisher} to send the results of {@link Executable}s to.
	 * @throws BrowserException If a {@link Browser} problem prevented the {@link Scraper} from running.
	 * @throws ClientException If the {@link Scraper} could not be run.
	 */
	public void scrape(URIInterface pageLocation, NameValuePair[] extraVariables,
			Publisher publisher) throws BrowserException, ClientException {
		try {
			Page page = new Page(interfaces.jsonInterface.loadJSONObject(pageLocation));
			
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
				interfaces.log.i("Large execution queue: " + Utils.quote(queue.size()));
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
