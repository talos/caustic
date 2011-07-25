package net.microscraper.interfaces.json;

public interface JSONLocation {
	public JSONLocation resolve(JSONLocation jsonLocation) throws JSONInterfaceException;
	public JSONLocation resolve(String key) throws JSONInterfaceException;
	public JSONLocation resolve(int index) throws JSONInterfaceException;
	
	public boolean isFile();
	public boolean isHttp();
	
	public String getScheme();
	public String getSchemeSpecificPart();
	public String getFragment();
	
	public String[] explodeJSONPath();
	
	public String toString();
}
