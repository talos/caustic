package net.microscraper.file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * An implementation of {@link FileLoader} using {@link java.io.FileReader}.
 * @author talos
 * @see FileLoader
 * @see java.io.FileReader
 *
 */
public class JavaIOFileLoader implements FileLoader {

	public String load(String path) throws IOException {
		File file = new File(path);
		FileReader fileReader = new FileReader(file);
		char[] buffer = new char[(int) file.length()];  
		fileReader.read(buffer);
		fileReader.close();
		return new String(buffer);
	}
}
