package net.microscraper.impl.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.microscraper.interfaces.file.FileLoader;

public class IOFileLoader implements FileLoader {

	@Override
	public String load(String path) throws IOException {
		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return new String(buffer);		
	}
}
