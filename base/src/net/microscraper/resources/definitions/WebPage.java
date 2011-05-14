package net.microscraper.resources.definitions;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Browser.DelayRequest;
import net.microscraper.client.EncodedNameValuePair;
import net.microscraper.client.UnencodedNameValuePair;
import net.microscraper.resources.ExecutionContext;
import net.microscraper.resources.ExecutionDelay;
import net.microscraper.resources.ExecutionFailure;
import net.microscraper.resources.ExecutionFatality;

/**
 * Abstract class to request a web page using a browser.
 * @author realest
 *
 */
public abstract class WebPage implements Executable {
	private final URL url;
	private final GenericHeader[] headers;
	private final Cookie[] cookies;
	private final WebPageHead[] priorWebPages;

	protected WebPage(URL url, GenericHeader[] headers, Cookie[] cookies,
				WebPageHead[] priorWebPages) {
		this.url = url;
		this.headers = headers;
		this.cookies = cookies;
		this.priorWebPages = priorWebPages;
	}

	protected java.net.URL generateURL(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		return url.getURL(context);
	}
	
	protected UnencodedNameValuePair[] generateHeaders(ExecutionContext context)
				throws ExecutionDelay, ExecutionFatality {
		UnencodedNameValuePair[] headersAry = new UnencodedNameValuePair[this.headers.length];
		for(int i = 0 ; i < this.headers.length ; i ++) {
			headersAry[i] = headers[i].getNameValuePair(context);
		}
		return headersAry;
	}
	
	protected EncodedNameValuePair[] generateEncodedNameValuePairs(
				ExecutionContext context, EncodedHeader[] encodedHeaders)
				throws ExecutionDelay, ExecutionFatality {
		try {
			EncodedNameValuePair[] nameValuePairs = new EncodedNameValuePair[encodedHeaders.length];
			for(int i = 0 ; i < nameValuePairs.length ; i ++) {
				nameValuePairs[i] = encodedHeaders[i].getNameValuePair(context);
			}
			return nameValuePairs;
		} catch (UnsupportedEncodingException e) {
			throw new ExecutionFatality(e, this);
		}
	}
	
	protected EncodedNameValuePair[] generateCookies(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		return generateEncodedNameValuePairs(context, cookies);
	}
	
	protected void headPriorWebPages(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		for(int i = 0 ; i < priorWebPages.length ; i ++) {
			priorWebPages[i].headUsing(context);
		}
	}
	
	/**
	 * Send an HTTP Head for the web page.  This will add cookies to the browser.
	 * @param browser the browser to use.
	 * @throws ExecutionFatality 
	 * @throws ExecutionFailure 
	 * @throws ExecutionDelay 
	 * @throws BrowserException 
	 * @throws DelayRequest 
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	public void headUsing(ExecutionContext context) throws ExecutionDelay, ExecutionFatality {
		try {
			UnencodedNameValuePair[] headers = generateHeaders(context);
			EncodedNameValuePair[] cookies;
			cookies = generateCookies(context);
			
			headPriorWebPages(context);
			
			context.getBrowser().head(generateURL(context), headers, cookies);
		} catch (DelayRequest e) {
			throw new ExecutionDelay(e, this);
		} catch (BrowserException e) {
			throw new ExecutionFatality(e, this);
		}
	}
}
