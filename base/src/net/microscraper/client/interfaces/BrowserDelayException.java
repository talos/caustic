package net.microscraper.client.interfaces;

import net.microscraper.client.ClientException;

/**
 * This is thrown when the {@link Browser} should wait to request this {@link Page} in order to limit traffic.
 * @author john
 *
 */
public class BrowserDelayException extends ClientException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8357347717343426486L;
	public final String url;
	public final float kbpsSinceLastLoad;
	public BrowserDelayException(String url, float kbpsSinceLastLoad) {
		//super(url);
		this.url = url;
		this.kbpsSinceLastLoad = kbpsSinceLastLoad;
		/*super("Delaying load of " + Utils.quote(url.toString()) +
				", has been loaded at " +
				Utils.quote(Float.toString(kbpsSinceLastLoad)));*/
	}
}