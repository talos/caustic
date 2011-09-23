package net.microscraper;

import net.microscraper.client.ScraperLocalTest;
import net.microscraper.deserializer.JsonDeserializerTest;
import net.microscraper.file.FileLoaderTest;
import net.microscraper.http.CookieManagerLocalTest;
import net.microscraper.http.HttpBrowserLocalTest;
import net.microscraper.http.RateLimitManagerTest;
import net.microscraper.http.ResponseHeadersTest;
import net.microscraper.instruction.FindTest;
import net.microscraper.instruction.LoadLocalTest;
import net.microscraper.json.JsonObjectTest;
import net.microscraper.regexp.PatternTest;
import net.microscraper.regexp.StringTemplateTest;
import net.microscraper.template.HashtableTemplateTest;
import net.microscraper.uri.URILoaderTest;
import net.microscraper.uri.UriResolverTest;
import net.microscraper.util.HashtableUtilsTest;
import net.microscraper.util.StringUtilsTest;
import net.microscraper.util.VectorUtilsTest;

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
	ScraperLocalTest.class,
	StringUtilsTest.class,
	VectorUtilsTest.class
})
public class LocalTests {

}
