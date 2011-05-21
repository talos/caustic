package net.microscraper.client.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import net.microscraper.client.impl.JSONResourceLoader;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;

public class LocalJSONResourceLoader extends JSONResourceLoader {
	private final JSONInterface jsonInterface;
	public LocalJSONResourceLoader(JSONInterface jsonInterface) {
		super(jsonInterface);
		this.jsonInterface = jsonInterface;
	}

	@Override
	public JSONInterfaceObject obtainJSON(URI location) throws IOException, JSONInterfaceException {
		File file = new File(location);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return jsonInterface.getTokener(new String(buffer)).nextValue();
	}
}
