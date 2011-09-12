package net.microscraper;

import net.microscraper.client.ScraperNetworkTest;
import net.microscraper.http.CookieManagerNetworkTest;
import net.microscraper.http.HttpBrowserNetworkTest;
import net.microscraper.http.HttpRequesterTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
/**
 * A test suite for all tests that do not require network connectivity.
 * @author talos
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
	CookieManagerNetworkTest.class,
	HttpBrowserNetworkTest.class,
	HttpRequesterTest.class,
	ScraperNetworkTest.class
})
public class NetworkTests {

}
