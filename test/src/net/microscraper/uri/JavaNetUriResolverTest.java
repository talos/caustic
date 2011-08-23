package net.microscraper.uri;

public class JavaNetUriResolverTest extends UriResolverTest {

	@Override
	protected UriResolver getUriResolver() {
		return new JavaNetUriResolver();
	}

}
