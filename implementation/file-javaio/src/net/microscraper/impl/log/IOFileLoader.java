package net.microscraper.impl.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.microscraper.Utils;
import net.microscraper.interfaces.file.FileLoader;

/**
 * Class to load the contents of a file-schema absolute or relative
 * {@link URIInterface} into a {@link String}.
 * @author john
 *
 */
public class IOFileLoader implements FileLoader {

	/**
	 * @throws IOException if the scheme of {@link location} is not
	 * "file".
	 */
	public String loadURI(String uri) throws IOException {
		//File file = new File(new URI(fileLocation.getScheme(), fileLocation.getSchemeSpecificPart(), null));
		try {
			URI location = new URI(uri);
			
			if(location.isAbsolute()) {
				if(!location.getScheme().equals("file")) {
					throw new IOException(Utils.quote(location.toString()) + " is not of scheme 'file'.");				
				}
			}
			return loadPath(location.getSchemeSpecificPart());
		} catch(URISyntaxException e) {
			throw new IOException(e);
		}
	}
	
	public String loadPath(String path) throws IOException {
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return new String(buffer);		
	}
}
