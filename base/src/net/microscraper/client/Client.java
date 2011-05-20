package net.microscraper.client;

import java.net.URI;

import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.execution.Context;
import net.microscraper.execution.Execution;
import net.microscraper.execution.ScraperExecution;
import net.microscraper.model.Link;
import net.microscraper.model.URIMustBeAbsoluteException;

/**
 * A microscraper Client allows for scraping from microscraper json.
 * @author john
 *
 */
public class Client {
	private final Context context;
	private final Publisher publisher;
	
	public Client(Context context, Publisher publisher) {
		this.context = context;
		this.publisher = publisher;
	}
	
	public void scrape(URI scraperLocation, UnencodedNameValuePair[] extraVariables) throws URIMustBeAbsoluteException {
		execute(new ScraperExecution(new Link(scraperLocation), context, extraVariables));
	}
	private void execute(Execution exc) {
		exc.run();
		try {
			publisher.publish(exc);
		} catch(PublisherException e) {
			context.e(e);
		}
		Execution[] children = exc.getChildren();
		for(int i = 0 ; i < children.length ; i ++) {
			execute(children[i]);
		}
		if(!exc.isComplete() && !exc.isStuck() && !exc.hasFailed()) {
			execute(exc);
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
	
	public static class MicroScraperClientException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8899853760225376402L;

		public MicroScraperClientException(Throwable e) { super(e); }
	}
}
