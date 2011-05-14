package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Variables;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * Abstract class to obtain the body of a WebPage.
 * @author realest
 *
 */
public abstract class WebPageBody extends WebPage implements Parsable {
	private final Regexp[] terminates;
	protected WebPageBody(URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPageHead[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages);
		this.terminates = terminates;
	}
	
	/**
	 * Load the WebPage.  Terminates at the point that one of the terminates regular expressions
	 * matches the body.
	 * @return The loaded body.
	 * @throws ExecutionFatality 
	 * @throws ExecutionFailure 
	 * @throws ExecutionDelay 
	 */
	public final String parse(ExecutionContext context) throws ExecutionDelay, ExecutionFailure, ExecutionFatality {
		Variables variables = context.getVariables();
		try {
			headPriorWebPages(context);
			return getResponse(context);
		} catch(DelayRequest e) {
			throw new ExecutionDelay(e, this, variables);
		} catch(BrowserException e) {
			throw new ExecutionFailure(e, this, variables);
		} catch (MalformedURLException e) {
			throw new ExecutionFatality(e, this, variables);
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionFatality(e, this, variables);
		}
	}
	
	protected abstract String getResponse(ExecutionContext context)
			throws ExecutionDelay, ExecutionFailure, ExecutionFatality,
			DelayRequest, BrowserException, MalformedURLException, UnsupportedEncodingException;
	
	protected Pattern[] generateTerminates(ExecutionContext context) {
		Pattern[] patterns = new Pattern[terminates.length];
		for(int i = 0 ; i < terminates.length; i ++) {
			
		}
		return patterns;
	}
}
