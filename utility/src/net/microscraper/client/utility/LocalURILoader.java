package net.microscraper.client.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.client.impl.CachedURILoader;
import net.microscraper.client.interfaces.URIInterface;

public class LocalURILoader extends CachedURILoader {
	@Override
	protected String loadNew(URIInterface location) throws IOException {
		try {
			File file = new File(new URI(location.toString()));
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];  
			fileInputStream.read(buffer);
			return new String(buffer);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
