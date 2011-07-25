package net.microscraper.interfaces.json;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;

import net.microscraper.impl.json.JavaNetJSONLocation;
import net.microscraper.impl.regexp.JakartaRegexpCompiler;
import net.microscraper.impl.regexp.JavaUtilRegexpCompiler;
import net.microscraper.interfaces.regexp.RegexpCompiler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class JSONLocationTest {
	
	private Constructor<JSONLocation> constructor;
	
	private static final String filePathWithFragment = "file:/path/to/file#path/to/obj";
	private static final String pathWithoutFragment = "path/to/file";
	private static final String httpWithFragment = "http://www.site.com/#1/2/3/4";
	
	private JSONLocation newLocation(String uriOrPath) throws Exception {
		return constructor.newInstance(uriOrPath);
	}
	
	public JSONLocationTest(final Class<JSONLocation> klass) throws Exception {
		constructor = klass.getConstructor(String.class);
	}
	
	@Parameters
	public static Collection<Class[]> implementations() {
		return Arrays.asList(new Class[][] {
				{ JavaNetJSONLocation.class }
		});
	}
	
	@Test
	public void testResolveJSONLocation() throws Exception {
		assertEquals(newLocation("file:/path/to/path/to/file"),
				newLocation(filePathWithFragment).resolve(newLocation(pathWithoutFragment)));
		assertEquals(newLocation("file:/path/to/file#path/to/obj"),
				newLocation(pathWithoutFragment).resolve(newLocation(filePathWithFragment)));
		
		assertEquals(newLocation("http://www.site.com/path/to/file"),
				newLocation(httpWithFragment).resolve(newLocation(pathWithoutFragment)));
		assertEquals(newLocation("http://www.site.com/#1/2/3/4"),
				newLocation(pathWithoutFragment).resolve(newLocation(httpWithFragment)));
	}

	@Test
	public void testResolveString() throws Exception {
		assertEquals(newLocation("file:/path/to/path/to/file"),
				newLocation(filePathWithFragment).resolve(pathWithoutFragment));
		assertEquals(newLocation("file:/path/to/file#path/to/obj"),
				newLocation(pathWithoutFragment).resolve(filePathWithFragment));
		
		assertEquals(newLocation("http://www.site.com/path/to/file"),
				newLocation(httpWithFragment).resolve(pathWithoutFragment));
		assertEquals(newLocation("http://www.site.com/#1/2/3/4"),
				newLocation(pathWithoutFragment).resolve(httpWithFragment));
	}
	
	@Test
	public void testResolveFragmentString() throws Exception {
		assertEquals(newLocation("file:/path/to/file#/path/to/obj/more/path"),
				newLocation(filePathWithFragment).resolveFragment("more/path"));
		
		assertEquals(newLocation("http://www.site.com/#/1/2/3/4/more/path"),
				newLocation(httpWithFragment).resolveFragment("more/path"));
		
		assertEquals(newLocation("http://www.site.com/#/1/2/alternate"),
				newLocation(httpWithFragment).resolveFragment("../../alternate"));
		
		assertEquals(newLocation("http://www.site.com/#/other/path"),
				newLocation(httpWithFragment).resolveFragment("/other/path"));
	}
	
	@Test
	public void testResolveFragmentInteger() throws Exception {
		assertEquals(newLocation("file:/path/to/file#path/to/obj/5"),
				newLocation(filePathWithFragment).resolveFragment(5));
		
		assertEquals(newLocation("http://www.site.com/#1/2/3/4/5"),
				newLocation(httpWithFragment).resolveFragment(5));
	}
	
	@Test
	public void testIsFile() throws Exception {
		assertTrue(newLocation(filePathWithFragment).isFile());
		assertTrue(newLocation(pathWithoutFragment).isFile());
		assertFalse(newLocation(httpWithFragment).isFile());
	}

	@Test
	public void testIsHttp() throws Exception {
		assertFalse(newLocation(filePathWithFragment).isHttp());
		assertFalse(newLocation(pathWithoutFragment).isHttp());
		assertTrue(newLocation(httpWithFragment).isHttp());
	}

	@Test
	public void testGetScheme() throws Exception {
		assertEquals("file", newLocation(filePathWithFragment).getScheme());
		assertNull(newLocation(pathWithoutFragment).getScheme());
		assertEquals("http", newLocation(httpWithFragment).getScheme());
	}

	@Test
	public void testGetSchemeSpecificPart() throws Exception {
		assertEquals("/path/to/file", newLocation(filePathWithFragment).getSchemeSpecificPart());
		assertEquals("path/to/file", newLocation(pathWithoutFragment).getSchemeSpecificPart());
		assertEquals("//www.site.com/", newLocation(httpWithFragment).getSchemeSpecificPart());
	}

	@Test
	public void testGetFragment() throws Exception {
		assertEquals("/path/to/obj", newLocation(filePathWithFragment).getFragment());
		assertEquals("/", newLocation(pathWithoutFragment).getFragment());
		assertEquals("/1/2/3/4", newLocation(httpWithFragment).getFragment());
	}

	@Test
	public void testExplodeJSONPath() throws Exception {
		assertArrayEquals(
				new String[] {"path", "to", "obj"},
				newLocation(filePathWithFragment).explodeJSONPath());
		assertArrayEquals(
				new String[] {},
				newLocation(pathWithoutFragment).explodeJSONPath());
		assertArrayEquals(
				new String[] { "1", "2", "3", "4" },
				newLocation(httpWithFragment).explodeJSONPath());
	}

	@Test
	public void testToString() throws Exception {
		assertEquals("file:/path/to/file#/path/to/obj", newLocation(filePathWithFragment).toString());
		assertEquals("path/to/file#/", newLocation(pathWithoutFragment).toString());
		assertEquals("http://www.site.com/#/1/2/3/4", newLocation(httpWithFragment).toString());
	}

	@Test
	public void testEquals() throws Exception {
		assertTrue(newLocation(filePathWithFragment).equals(newLocation(filePathWithFragment)));
		assertTrue(newLocation(pathWithoutFragment).equals(newLocation(pathWithoutFragment)));
		assertTrue(newLocation(httpWithFragment).equals(newLocation(httpWithFragment)));
	}
}
