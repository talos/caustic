package net.caustic;

import net.caustic.http.HttpBrowserIntegrationTest;
import net.caustic.http.HttpRequesterTest;
import net.caustic.uri.URILoaderIntegrationTest;

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
	HttpBrowserIntegrationTest.class,
	HttpRequesterTest.class,
	URILoaderIntegrationTest.class,

})
public class CoreIntegrationTests {

}
