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
			System.out.println(location.toString());
			File file = new File(new URI(location.getScheme(), location.getSchemeSpecificPart(), null));
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[(int) file.length()];  
			fileInputStream.read(buffer);
			return new String(buffer);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
