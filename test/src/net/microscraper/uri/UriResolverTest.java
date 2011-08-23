package net.microscraper.uri;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public abstract class UriResolverTest {
	
	private static final String filePathWithFragment = "file:/path/to/file#fragment";
	private static final String pathWithoutFragment = "path/to/file";
	private static final String httpWithFragment = "http://www.site.com/#fragment";
	
	private UriResolver resolver;
	
	protected abstract UriResolver getUriResolver();
	
	@Before
	public void setUp() throws Exception {
		resolver = getUriResolver();
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
