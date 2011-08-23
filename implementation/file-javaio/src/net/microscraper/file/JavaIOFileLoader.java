package net.microscraper.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.microscraper.file.FileLoader;

/**
 * An implementation of {@link FileLoader} using {@link java.io.File}.
 * @author talos
 * @see FileLoader
 * @see java.io.File
 *
 */
public class JavaIOFileLoader implements FileLoader {

	@Override
	public String load(String path) throws IOException {
		//File file = new File(path.getSchemeSpecificPart());
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return new String(buffer);		
	}
}
