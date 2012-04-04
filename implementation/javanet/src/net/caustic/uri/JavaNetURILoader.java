package net.caustic.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
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
	
	private final FileLoader fileLoader;
	private final boolean allowsFileLoading;
	
	private String loadLocal(URI uri) throws URILoaderException {
		if(allowsFileLoading) {
			String path = uri.getSchemeSpecificPart();
			try {
				return fileLoader.load(path);
			} catch(FileNotFoundException e) {
				throw URILoaderException.fromLocal(e, path, "could not find file");			
			} catch(IOException e) {
				throw URILoaderException.fromLocal(e, path, "IO exception");
			}
		} else {
			throw new UnsupportedSchemeException(uri.getScheme());
		}
	}
	
	private String loadRemote(String urlStr) throws URILoaderException {
		try {
			URL url = new URL(urlStr);
			try {
				URLConnection conn = url.openConnection();
				conn.addRequestProperty("Accept", "application/json,text/javascript");
				conn.connect();			
				try {
					InputStreamReader stream = new InputStreamReader(conn.getInputStream());
					StringBuffer content = new StringBuffer();
					char[] buf = new char[512];
					int len;
					while((len = stream.read(buf)) != -1) {
						content.append(buf, 0, len);
					}
					
					return content.toString();
				} catch(IOException e) {
					throw URILoaderException.fromRemote(e, urlStr, conn.getHeaderField(0));
				}
			} catch(SocketTimeoutException e) {
				throw URILoaderException.fromRemote(e, urlStr, "timeout");
			} catch(IOException e) {
				throw URILoaderException.fromRemote(e, urlStr, "could not open connection");
			}
		} catch(MalformedURLException e) {
			throw URILoaderException.fromRemote(e, urlStr, "malformed URL");
		}
	}

	/**
	 * Create a {@link JavaNetURILoader} that can load local files.
	 * @param fileLoader The {@link FileLoader} to load local URIs with.
	 */
	public JavaNetURILoader(FileLoader fileLoader) {
		this.fileLoader = fileLoader;
		this.allowsFileLoading = true;
	}
	
	/**
	 * Create a {@link JavaNetURILoader} that cannot read local files.  If asked to,
	 * it will throws a {@link UnsupportedSchemeException}.
	 */
	public JavaNetURILoader() {
		this.fileLoader = null;
		this.allowsFileLoading = false;
	}
	
	public String load(String uriStr) throws URILoaderException, InterruptedException {
		try {
			URI uri = new URI(uriStr);
			if(uri.getScheme() == null) {
				return loadLocal(uri);
			} else if(uri.getScheme().equalsIgnoreCase(UriResolver.FILE_SCHEME)) {
				return loadLocal(uri);
			} else {
				return loadRemote(uriStr);
				//return browser.get(uriStr, new Hashtable<String, String>(), new Pattern[] {});
			}
		} catch (URISyntaxException e) {
			throw new MalformedUriException(e);
		}
	}
}
