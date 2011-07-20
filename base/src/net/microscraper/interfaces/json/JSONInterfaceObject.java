package net.microscraper.interfaces.json;

import java.io.IOException;


public interface JSONInterfaceObject {
	public abstract JSONInterfaceArray getJSONArray(String name) throws JSONInterfaceException;
	public abstract JSONInterfaceObject getJSONObject(String name) throws JSONInterfaceException, IOException;
	public abstract Object get(String name) throws JSONInterfaceException;
	public abstract String getString(String name) throws JSONInterfaceException;
	public abstract int getInt(String name) throws JSONInterfaceException;
	public abstract boolean getBoolean(String name) throws JSONInterfaceException;
	public abstract boolean has(String name);
	public abstract boolean isNull(String name);
	public abstract JSONInterfaceIterator keys();
	public abstract int length();
	public abstract JSONLocation getLocation();
}