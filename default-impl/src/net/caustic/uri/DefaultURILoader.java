package net.caustic.uri;

import net.caustic.file.JavaIOFileLoader;
import net.caustic.uri.JavaNetURILoader;
import net.caustic.uri.URILoader;
import net.caustic.uri.URILoaderException;

public class DefaultURILoader implements URILoader {

	private final URILoader loader;
	
	/**
	 * 
	 * @param loadLocal Whether to allow the loading of local files.
	 */
	public DefaultURILoader(boolean loadLocal) {
		if(loadLocal) {
			this.loader = new JavaNetURILoader(new JavaIOFileLoader());
		} else {
			this.loader = new JavaNetURILoader();
		}
	}
	
	@Override
	public String load(String uriStr) throws URILoaderException,
			InterruptedException {
		return loader.load(uriStr);
	}

}
