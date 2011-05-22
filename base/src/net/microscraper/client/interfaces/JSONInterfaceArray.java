package net.microscraper.client.interfaces;

public interface JSONInterfaceArray {
	public abstract JSONInterfaceArray getJSONArray(int index) throws JSONInterfaceException;
	public abstract JSONInterfaceObject getJSONObject(int index) throws JSONInterfaceException;
	public abstract java.lang.Object get(int index) throws JSONInterfaceException;
	public abstract String getString(int index) throws JSONInterfaceException;
	public abstract int getInt(int index) throws JSONInterfaceException;
	public abstract boolean getBoolean(int index) throws JSONInterfaceException;
	public abstract String[] toArray() throws JSONInterfaceException; 
	public abstract int length();
}