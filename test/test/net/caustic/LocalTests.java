package net.caustic;

import net.caustic.deserializer.JsonDeserializerTest;
import net.caustic.file.FileLoaderTest;
import net.caustic.http.CookieManagerLocalTest;
import net.caustic.http.HttpBrowserLocalTest;
import net.caustic.http.RateLimitManagerTest;
import net.caustic.http.ResponseHeadersTest;
import net.caustic.instruction.FindTest;
import net.caustic.instruction.LoadLocalTest;
import net.caustic.json.JsonObjectTest;
import net.caustic.regexp.PatternTest;
import net.caustic.regexp.StringTemplateTest;
import net.caustic.template.HashtableTemplateTest;
import net.caustic.uri.URILoaderTest;
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
	CookieManagerLocalTest.class,
	HttpBrowserLocalTest.class,
	RateLimitManagerTest.class,
	ResponseHeadersTest.class,
	FindTest.class,
	LoadLocalTest.class,
	JsonDeserializerTest.class,
	JsonObjectTest.class,
	PatternTest.class,
	HashtableTemplateTest.class,
	StringTemplateTest.class,
	URILoaderTest.class,
	UriResolverTest.class,
	HashtableUtilsTest.class,
	StringUtilsTest.class,
	VectorUtilsTest.class
})
public class LocalTests {

}
