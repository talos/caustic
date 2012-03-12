package net.caustic.uri;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import net.caustic.file.FileLoader;
import net.caustic.http.HttpBrowser;
import net.caustic.uri.MalformedUriException;
import net.caustic.uri.URILoader;
import net.caustic.uri.URILoaderException;
import net.caustic.uri.UriResolver;

/**
 * Implementation of {@link URILoader} that determines URI scheme using
 * {@link java.net.URI}.
 * @author realest
 *
 */
public class JavaNetURILoader implements URILoader {
	
	//private final HttpBrowser browser;
	private final FileLoader fileLoader;

	/**
	 * Create a {@link JavaNetURILoader} using a specific browser.
	 * @param browser The {@link HttpBrowser} to load remote URIs with.
	 * @param fileLoader The {@link FileLoader} to load local URIs with.
	 */
	public JavaNetURILoader(FileLoader fileLoader) {
		//this.browser = browser;
		this.fileLoader = fileLoader;
	}
	
	public String load(String uriStr) throws URILoaderException, InterruptedException {
		try {
			URI uri = new URI(uriStr);
			if(uri.getScheme() == null) {
				return fileLoader.load(uri.getSchemeSpecificPart());
			} else if(uri.getScheme().equalsIgnoreCase(UriResolver.FILE_SCHEME)) {
				return fileLoader.load(uri.getSchemeSpecificPart());
			} else {
				URLConnection conn = new URL(uriStr).openConnection();
				conn.addRequestProperty("request", "application/json");
				InputStreamReader stream = new InputStreamReader(conn.getInputStream());
				StringBuffer content = new StringBuffer();
				char[] buf = new char[512];
				int len;
				while((len = stream.read(buf)) != -1) {
					content.append(buf, 0, len);
				}
				
				return content.toString();
				//return browser.get(uriStr, new Hashtable<String, String>(), new Pattern[] {});
			}
		} catch (IOException e) {
			// TODO this is thrown from remote as well.
			throw URILoaderException.fromLocal(e);
		} catch (URISyntaxException e) {
			throw new MalformedUriException(e);
		} /*catch (HttpException e) {
			throw URILoaderException.fromRemote(e);
		}*/
	}
}
