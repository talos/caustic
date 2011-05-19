package net.microscraper.client;

import java.net.URI;

import net.microscraper.execution.Context;
import net.microscraper.execution.LeafExecution;
import net.microscraper.execution.PageExecution;
import net.microscraper.execution.ScraperExecution;
import net.microscraper.execution.VariableExecution;
import net.microscraper.model.Link;
import net.microscraper.model.MustacheTemplate;
import net.microscraper.model.URIMustBeAbsoluteException;

/**
 * A microscraper Client allows for scraping from microscraper json.
 * @author john
 *
 */
public class Client {
	private final Context context;
	
	public Client(Context context) {
		this.context = context;
	}
	
	public void scrape(URI scraperLocation, UnencodedNameValuePair[] extraVariables) throws URIMustBeAbsoluteException {
		ScraperExecution scraperExecution = new ScraperExecution(new Link(scraperLocation), context, extraVariables);
		while(retryScraper(scraperExecution) != false) { }

	}
	
	private boolean retryScraper(ScraperExecution exc) {
		exc.run();
		if(exc.isComplete() || exc.isStuck() || exc.hasFailed()) {
			return false;
		} else {
			VariableExecution[] variableExecutions = exc.getVariableExecutions();
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				retryVariable(variableExecutions[i]);
			}
			LeafExecution[] leafExecutions = exc.getLeafExecutions();
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				retryLeaf(leafExecutions[i]);
			}
			ScraperExecution[] scraperExecutions = exc.getScraperExecutions();
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				retryScraper(scraperExecutions[i]);
			}
			return true;
		}
	}
	
	private boolean retryVariable(VariableExecution exc) {
		exc.run();
		if(exc.isComplete() || exc.isStuck() || exc.hasFailed()) {
			return false;
		} else {
			VariableExecution[] variableExecutions = exc.getVariableExecutions();
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				retryVariable(variableExecutions[i]);
			}
			LeafExecution[] leafExecutions = exc.getLeafExecutions();
			for(int i = 0 ; i < variableExecutions.length ; i ++) {
				retryLeaf(leafExecutions[i]);
			}
			return true;
		}
	}

	private boolean retryLeaf(LeafExecution exc) {
		exc.run();
		if(exc.isComplete() || exc.isStuck() || exc.hasFailed()) {
			return false;
		} else {
			ScraperExecution[] scraperExecutions = exc.getScraperExecutions();
			for(int i = 0 ; i < scraperExecutions.length ; i ++) {
				retryScraper(scraperExecutions[i]);
			}
			return true;
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
