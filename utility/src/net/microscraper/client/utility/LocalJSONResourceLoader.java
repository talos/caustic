package net.microscraper.client.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.impl.JSONResourceLoader;
import net.microscraper.model.Link;

public class LocalJSONResourceLoader extends JSONResourceLoader {
	private final Interfaces.JSON jsonInterface;
	public LocalJSONResourceLoader(JSON jsonInterface) {
		super(jsonInterface);
		this.jsonInterface = jsonInterface;
	}

	@Override
	public Interfaces.JSON.Object obtainJSON(Link link) throws IOException, JSONInterfaceException {
		File file = new File(link.location);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];  
		fileInputStream.read(buffer);
		return jsonInterface.getTokener(new String(buffer)).nextValue();
	}
}
