package net.microscraper.client;

import java.net.URI;
import java.util.Vector;

import net.microscraper.client.interfaces.Browser;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.Publisher;
import net.microscraper.client.interfaces.PublisherException;
import net.microscraper.client.interfaces.RegexpInterface;
import net.microscraper.execution.Execution;
import net.microscraper.execution.ExecutionContext;
import net.microscraper.execution.ResourceLoader;
import net.microscraper.execution.ScraperExecution;
import net.microscraper.model.Link;
import net.microscraper.model.URIMustBeAbsoluteException;

/**
 * A microscraper Client allows for scraping from microscraper json.
 * @author john
 *
 */
public final class Client {
	private static final int LARGE_QUEUE = 1000000; // TODO: handle differently
	//private final Context context;
	private final Publisher publisher;
	
	private final ExecutionContext context;
	private final Vector queue = new Vector();
	

	/**
	 * @param resourceLoader The {@link ResourceLoader} this {@link ScraperExecution} is set to use.
	 * @param browser The {@link Browser} this {@link ScraperExecution} is set to use.
	 * @param log The {@link Log} this {@link ScraperExecution} is set to use.
	 * @param encoding The encoding to use when encoding post data and cookies. "UTF-8" is recommended.
	 * @param regexp The {@link RegexpInterface} interface to use when compiling regexps.
	 */
	public Client(ResourceLoader resourceLoader, RegexpInterface regexpInterface,
			JSONInterface jsonInterface, Browser browser,
			Log log, String encoding, Publisher publisher) {
		this.context = new ExecutionContext( log,
				regexpInterface, browser, resourceLoader, encoding);
		this.publisher = publisher;
	}
	
	public void scrape(URI scraperLocation, UnencodedNameValuePair[] extraVariables) throws URIMustBeAbsoluteException {
		queue.add(
				new ScraperExecution(context, new Link(scraperLocation), extraVariables)
			);
		execute();
	}
	
	private void execute() {
		while(queue.size() > 0) {
			if(queue.size() > LARGE_QUEUE) {
				context.log.i("Large execution queue: " + Utils.quote(queue.size()));
			}
			Execution exc = (Execution) queue.elementAt(0);
			queue.removeElementAt(0);
			exc.run();
			try {
				publisher.publish(exc);
			} catch(PublisherException e) {
				context.log.e(e);
			}
			// If the execution is complete, add its children to the queue.
			if(exc.isComplete()) {
				Execution[] children = exc.getChildren();
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
