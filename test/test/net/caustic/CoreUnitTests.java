package net.caustic;

import net.caustic.file.FileLoaderTest;
import net.caustic.http.HttpBrowserUnitTest;
import net.caustic.http.LoadTest;
import net.caustic.http.RateLimitManagerTest;
import net.caustic.http.ResponseHeadersTest;
import net.caustic.json.JSONValueTest;
import net.caustic.regexp.PatternTest;
import net.caustic.regexp.StringTemplateTest;
import net.caustic.template.HashtableTemplateTest;
import net.caustic.uri.UriResolverTest;
import net.caustic.util.HashtableUtilsTest;
import net.caustic.util.StringUtilsTest;
import net.caustic.util.VectorUtilsTest;

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
	FileLoaderTest.class,
	HttpBrowserUnitTest.class,
	RateLimitManagerTest.class,
	ResponseHeadersTest.class,
	FindTest.class,
	LoadTest.class,
	JSONValueTest.class,
	PatternTest.class,
	HashtableTemplateTest.class,
	StringTemplateTest.class,
	UriResolverTest.class,
	HashtableUtilsTest.class,
	StringUtilsTest.class,
	VectorUtilsTest.class
})
public class CoreUnitTests {

}
