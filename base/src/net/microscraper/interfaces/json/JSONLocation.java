package net.microscraper.interfaces.json;

public interface JSONLocation {
	public JSONLocation resolve(JSONLocation jsonLocation);
	public String resolve(String path);
	
	public JSONInterfaceObject load();
}
