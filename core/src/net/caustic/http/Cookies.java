package net.caustic.http;

import org.json.me.JSONObject;

public interface Cookies {

	public abstract String[] getHosts();
	public abstract String[] get(String host);
	public abstract JSONObject toJSON();

}