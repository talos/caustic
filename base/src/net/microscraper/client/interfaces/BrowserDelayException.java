package net.microscraper.client.interfaces;

/**
 * This is thrown when the Browser should wait to request this Page in order to limit traffic.
 * @author john
 *
 */
public class BrowserDelayException extends BrowserException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8357347717343426486L;
	public final URLInterface url;
	public final float kbpsSinceLastLoad;
	public BrowserDelayException(URLInterface url, float kbpsSinceLastLoad) {
		super(url);
		this.url = url;
		this.kbpsSinceLastLoad = kbpsSinceLastLoad;
		/*super("Delaying load of " + Utils.quote(url.toString()) +
				", has been loaded at " +
				Utils.quote(Float.toString(kbpsSinceLastLoad)));*/
	}
}