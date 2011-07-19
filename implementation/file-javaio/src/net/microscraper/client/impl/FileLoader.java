package net.microscraper.client.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.microscraper.client.Utils;
import net.microscraper.client.interfaces.URIInterface;

/**
 * Class to load the contents of a file-schema absolute or relative
 * {@link URIInterface} into a {@link String}.
 * @author john
 *
 */
public class FileLoader extends CachedURILoader {

	/**
	 * @throws IOException if the scheme of {@link location} is not
	 * "file".
	 */
	protected String loadNew(URIInterface location) throws IOException {
		//File file = new File(new URI(fileLocation.getScheme(), fileLocation.getSchemeSpecificPart(), null));
		if(location.isAbsolute()) {
			if(!location.getScheme().equals("file")) {
				throw new IOException(Utils.quote(location.toString()) + " is not of scheme 'file'.");				
			}
		}
		return loadPath(location.getSchemeSpecificPart());
	}
	
	private String loadPath(String path) throws IOException {
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return new String(buffer);		
	}
}
