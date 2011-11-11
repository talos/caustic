package net.caustic.uri;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import net.caustic.uri.JavaNetUriResolver;
import net.caustic.uri.MalformedUriException;
import net.caustic.uri.RemoteToLocalSchemeResolutionException;
import net.caustic.uri.UriResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UriResolverTest {
	private final Class<UriResolver> klass;
	private static final String filePathWithFragment = "file:/path/to/file#fragment";
	private static final String pathWithoutFragment = "path/to/file";
	private static final String httpWithFragment = "http://www.site.com/#fragment";
	
	private UriResolver resolver;
	
	public UriResolverTest(Class<UriResolver> klass) {
		this.klass = klass;
	}
	
	@Parameters
	public static List<Class<?>[]> implementations() {
		return Arrays.asList(new Class<?>[][] {
				{	JavaNetUriResolver.class }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		resolver = klass.newInstance();
	}
	
	/*
	@Test
	public void testWindowsFragments() throws Exception {
		assertEquals("Does not resolve Windows filepath.", "C:\\path\\to\\file", resolver.resolve("C:", "path\\to\\file"));
		assertEquals("Does not resolve Unix fragment against Windows filepath", "C:\\path\\to", resolver.resolve("C:\\path\\to\\file", "../"));
	}*/
	
	@Test(expected = RemoteToLocalSchemeResolutionException.class)
	public void testThrowsRemoteToFileSchemeResolutionException() throws Exception {
		resolver.resolve("http://www.site.com/", "file://path/to/file");
	}

	@Test(expected = MalformedUriException.class)
	public void testFailsOnWindowsURIs() throws Exception {
		System.out.println(resolver.resolve("http://www.site.com/", "C:\\path\\to\\file"));
		resolver.resolve("http://www.site.com/", "C:\\path\\to\\file");
	}
	
	@Test
	public void testResolveFragments() throws Exception {
		assertEquals("file:/path/to/path/to/file", 
				resolver.resolve(filePathWithFragment, pathWithoutFragment));
		assertEquals("file:/path/to/file#fragment",
				resolver.resolve(pathWithoutFragment, filePathWithFragment));
		
		assertEquals("http://www.site.com/path/to/file",
				resolver.resolve(httpWithFragment, pathWithoutFragment));
		assertEquals("http://www.site.com/#fragment",
				resolver.resolve(pathWithoutFragment, httpWithFragment));
	}

	@Test
	public void testResolveString() throws Exception {
		assertEquals("file:/path/to/path/to/file",
				resolver.resolve(filePathWithFragment, pathWithoutFragment));
		assertEquals("file:/path/to/file#fragment",
				resolver.resolve(pathWithoutFragment, filePathWithFragment));
		
		assertEquals("http://www.site.com/path/to/file",
			resolver.resolve(httpWithFragment, pathWithoutFragment));
		assertEquals("http://www.site.com/#fragment",
				resolver.resolve(pathWithoutFragment, httpWithFragment));
	}
}
