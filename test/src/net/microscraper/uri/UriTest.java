package net.microscraper.uri;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import net.microscraper.uri.JavaNetURI;
import net.microscraper.uri.Uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UriTest {
	
	private final Constructor<Uri> constructor;
	
	private static final String filePathWithFragment = "file:/path/to/file#fragment";
	private static final String pathWithoutFragment = "path/to/file";
	private static final String httpWithFragment = "http://www.site.com/#fragment";
	
	private Uri newLocation(String uriOrPath) throws Exception {
		return constructor.newInstance(uriOrPath);
	}
	
	public UriTest(final Class<Uri> klass) throws Exception {
		constructor = klass.getConstructor(String.class);
	}
	
	@Parameters
	public static Collection<Class[]> implementations() {
		return Arrays.asList(new Class[][] {
				{ JavaNetURI.class }
		});
	}
	
	@Test
	public void testResolveJSONLocation() throws Exception {
		assertEquals(newLocation("file:/path/to/path/to/file"),
				newLocation(filePathWithFragment).resolve(newLocation(pathWithoutFragment)));
		assertEquals(newLocation("file:/path/to/file#fragment"),
				newLocation(pathWithoutFragment).resolve(newLocation(filePathWithFragment)));
		
		assertEquals(newLocation("http://www.site.com/path/to/file"),
				newLocation(httpWithFragment).resolve(newLocation(pathWithoutFragment)));
		assertEquals(newLocation("http://www.site.com/#fragment"),
				newLocation(pathWithoutFragment).resolve(newLocation(httpWithFragment)));
	}

	@Test
	public void testResolveString() throws Exception {
		assertEquals(newLocation("file:/path/to/path/to/file"),
				newLocation(filePathWithFragment).resolve(pathWithoutFragment));
		assertEquals(newLocation("file:/path/to/file#fragment"),
				newLocation(pathWithoutFragment).resolve(filePathWithFragment));
		
		assertEquals(newLocation("http://www.site.com/path/to/file"),
				newLocation(httpWithFragment).resolve(pathWithoutFragment));
		assertEquals(newLocation("http://www.site.com/#fragment"),
				newLocation(pathWithoutFragment).resolve(httpWithFragment));
	}
	
	@Test
	public void testToString() throws Exception {
		assertEquals("file:/path/to/file#fragment", newLocation(filePathWithFragment).toString());
		assertEquals("path/to/file", newLocation(pathWithoutFragment).toString());
		assertEquals("http://www.site.com/#fragment", newLocation(httpWithFragment).toString());
	}

	@Test
	public void testEquals() throws Exception {
		assertTrue(newLocation(filePathWithFragment).equals(newLocation(filePathWithFragment)));
		assertTrue(newLocation(pathWithoutFragment).equals(newLocation(pathWithoutFragment)));
		assertTrue(newLocation(httpWithFragment).equals(newLocation(httpWithFragment)));
	}
}
