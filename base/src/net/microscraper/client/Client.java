package net.microscraper.client;

import java.io.IOException;
import java.util.Vector;

import net.microscraper.client.executable.Executable;
import net.microscraper.client.executable.ScraperExecutable;
import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.Interfaces;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.RegexpCompiler;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.DeserializationException;
import net.microscraper.server.resource.Scraper;

/**
 * A microscraper {@link Client} can scrape a {@link Resource}.
 * @author john
 *
 */
public final class Client {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle this warning differently
	private final Interfaces interfaces;
	
	/**
	 * @param browser The {@link Browser} this {@link Client} should use.
	 * @param log The {@link Log} this {@link Client} should use.
	 * @param encoding The encoding to use when encoding or decoding post data, cookies,
	 * and JSON resources. "UTF-8" is recommended.
	 * @param netInterface A {@link NetInterface} to use when handling {@link URIInterface} and
	 * {@link URLInterface}.
	 * @param jsonInterface A {@link JSONInterface} to use when loading {@link JSONInterfaceObject}s.
	 * @param regexpCompiler The {@link RegexpCompiler} interface to use when compiling regexps.
	 */
	public Client(RegexpCompiler regexpCompiler, Browser browser,
			Log log, NetInterface netInterface, JSONInterface jsonInterface,
			String encoding) {
		this.interfaces = new Interfaces( log,
				regexpCompiler, browser, netInterface,
				jsonInterface, encoding);
	}
	
	/**
	 * 
	 * @param scraperLocation A {@link java.net.URI} to get the {@link Scraper} instructions from.
	 * @param extraVariables An array of {@link UnencodedNameValuePair}s to stock the {@link ScraperExecutable}s
	 * {@link FindOne}s with.
	 * @param publisher A {@link Publisher} to send the results of {@link Executable}s to.
	 * @throws ClientException If the {@link Scraper} could not be run.
	 */
	public void scrape(URIInterface scraperLocation, NameValuePair[] extraVariables,
			Publisher publisher) throws ClientException {
		try {
			Scraper scraper = new Scraper(interfaces.jsonInterface.loadJSONObject(scraperLocation));
			
			Executable rootExecutable = new ScraperExecutable(interfaces,
					scraper, new DefaultVariables(extraVariables));
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
	 */
	private void execute(Executable rootExecutable, Publisher publisher) {
		Vector queue = new Vector();
		queue.add(rootExecutable);
		while(queue.size() > 0) {
			if(queue.size() > LARGE_QUEUE) {
				interfaces.log.i("Large execution queue: " + Utils.quote(queue.size()));
			}
			Executable exc = (Executable) queue.elementAt(0);
			queue.removeElementAt(0);
			exc.run();
			try {
				publisher.publish(exc);
			} catch(PublisherException e) {
				interfaces.log.e(e);
			}
			// If the execution is complete, add its children to the queue.
			if(exc.isComplete()) {
				Executable[] children = exc.getChildren();
				Utils.arrayIntoVector(children, queue);
				
			// If the execution is not stuck and is not failed, add it back to the queue.
			} else if(!exc.hasFailed() && !exc.isStuck()) {
				queue.addElement(exc);
			}
		}
	}
	/*
	public void testPage(URI pageLocation, UnencodedNameValuePair[] extraVariables) {
		///new PageExecution(new Link(pageLocation), context, extraVariables);
		
	}
	
	public void testVariable(URI variableLocation, MustacheTemplate input, UnencodedNameValuePair[] extraVariables) {
		//new VariableExecution(new Link(variableLocation), context, extraVariables, input);
		
	}
	
	public void testLeaf(URI leafLocation, MustacheTemplate input, UnencodedNameValuePair[] extraVariables) {
		//new LeafExecution(new Link(leafLocation), context, extraVariables);
		
	}*/
}
