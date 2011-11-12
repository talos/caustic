package net.caustic.uri;

import net.caustic.file.JavaIOFileLoader;
import net.caustic.http.DefaultHttpBrowser;
import net.caustic.uri.JavaNetURILoader;
import net.caustic.uri.URILoader;
import net.caustic.uri.URILoaderException;

public class DefaultURILoader implements URILoader {

	private final URILoader loader;
	
	public DefaultURILoader() {
		this.loader = new JavaNetURILoader(
				new DefaultHttpBrowser(),
				new JavaIOFileLoader());
	}
	
	@Override
	public String load(String uriStr) throws URILoaderException,
			InterruptedException {
		return loader.load(uriStr);
	}

}
