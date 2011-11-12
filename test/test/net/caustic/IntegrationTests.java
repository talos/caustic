package net.caustic;

import net.caustic.http.CookieManagerNetworkTest;
import net.caustic.http.HttpBrowserNetworkTest;
import net.caustic.http.HttpRequesterTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
/**
 * A test suite for all tests that use network connectivity.
 * @author talos
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
	CookieManagerNetworkTest.class,
	HttpBrowserNetworkTest.class,
	HttpRequesterTest.class,
})
public class IntegrationTests {

}
