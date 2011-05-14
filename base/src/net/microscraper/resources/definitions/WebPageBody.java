package net.microscraper.resources.definitions;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFatality;

/**
 * Abstract class to obtain the body of a WebPage.
 * @author realest
 *
 */
public abstract class WebPageBody extends WebPage implements Stringable, Executable, Variable {
	private final Regexp[] terminates;
	private final Reference ref;
	protected WebPageBody(Reference ref, URL url, GenericHeader[] headers, Cookie[] cookies,
			WebPageHead[] priorWebPages, Regexp[] terminates) {
		super(url, headers, cookies, priorWebPages);
		this.ref = ref;
		this.terminates = terminates;
	}
	
	public Reference getRef() {
		return ref;
	}
	
	/**
	 * Load the WebPage.  Terminates at the point that one of the terminates regular expressions
	 * matches the body.
	 * @return The loaded body.
	 * @throws ExecutionFatality 
	 * @throws ExecutionDelay 
	 */
	public final String getString(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		headPriorWebPages(context);
		try {
			return getResponse(context);
		} catch (DelayRequest e) {
			throw new ExecutionDelay(e, this);
		} catch (BrowserException e) {
			throw new ExecutionFatality(e, this);
		}
	}
	
	protected abstract String getResponse(ExecutionContext context)
			throws ExecutionDelay, DelayRequest, BrowserException, ExecutionFatality;
	
	protected Pattern[] generateTerminates(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		Pattern[] patterns = new Pattern[terminates.length];
		for(int i = 0 ; i < terminates.length; i ++) {
			patterns[i] = terminates[i].getPattern(context);
		}
		return patterns;
	}
}
