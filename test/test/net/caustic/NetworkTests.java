package net.caustic;

import net.caustic.client.ScraperNetworkTest;
import net.caustic.http.CookieManagerNetworkTest;
import net.caustic.http.HttpBrowserNetworkTest;
import net.caustic.http.HttpRequesterTest;
import net.caustic.instruction.LoadNetworkTest;

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
	ScraperNetworkTest.class,
	LoadNetworkTest.class
})
public class NetworkTests {

}
