/**
 * Geogrape
 * A project to enable public access to public building information.
 */
package com.invisiblearchitecture.scraper;


/**
 * An equivalent interface to CookieStore, implemented by the network
 * library (org.apache.* etc.) of choice.  It is recommended that implementations
 * be synchronized, as each Gatherer (as opposed to each Information-Gatherer combo)
 * has its own GeograpeCookieStore.
 * @author john
 *
 */
public interface CookieStoreInterface {
	public abstract CookieInterface[] getCookies();
	public abstract void addCookies(CookieInterface[] cookies);
}