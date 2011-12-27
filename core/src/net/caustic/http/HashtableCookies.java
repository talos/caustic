package net.caustic.http;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.caustic.util.VectorUtils;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

public class HashtableCookies implements Cookies {

	private final Hashtable cookiesByHost = new Hashtable();
	private final Cookies wrapped;
	
	public HashtableCookies() { 
		this.wrapped = null;
	}
	
	private HashtableCookies(HashtableCookies cookies) {
		this.wrapped = cookies;
	}
	
	public void add(String host, String cookie) {
		Vector vec;
		if(!cookiesByHost.containsKey(host)) {
			vec = new Vector();
			cookiesByHost.put(host, vec);
		} else {
			vec = (Vector) cookiesByHost.get(host);
		}
		vec.add(cookie);
	}
	
	/* (non-Javadoc)
	 * @see net.caustic.http.Cookies#get(java.lang.String)
	 */
	/**
	 * This adds parent cookies in.
	 */
	public String[] get(String host) {
		Vector vec;
		if(cookiesByHost.containsKey(host)) {
			vec = (Vector) cookiesByHost.get(host);
		} else {
			vec = new Vector();
		}
		if(wrapped != null) {
			VectorUtils.arrayIntoVector(wrapped.get(host), vec);
		}
		
		String[] ary = new String[vec.size()];
		vec.copyInto(ary);
		return ary;
	}
	
	public String[] getHosts() {
		String[] hosts = new String[cookiesByHost.size()];
		int i = 0;
		Enumeration e = cookiesByHost.keys();
		while(e.hasMoreElements()) {
			hosts[i] = (String) e.nextElement();
			i++;
		}
		
		return hosts;
	}
	
	public JSONObject toJSON() {
		JSONObject obj = new JSONObject();
		Enumeration e = cookiesByHost.keys();
		while(e.hasMoreElements()) {
			String host = (String) e.nextElement();
			try {
				obj.put(host, new JSONArray((Vector) cookiesByHost.get(host)));
			} catch(JSONException exception) {
				throw new RuntimeException("Unexpected JSON problem", exception);
			}
		}
		return obj;
	}
	
	/**
	 * Wrap this {@link HashtableCookies} so it is not modified.
	 * @return
	 */
	public HashtableCookies branch() {
		return new HashtableCookies(this);
	}
	
	/**
	 * 
	 * @param extension
	 */
	public void extend(Cookies extension) {
		String[] hosts = extension.getHosts();
		for(int i = 0 ; i < hosts.length ; i ++) {
			String host = hosts[i];
			String[] cookies = extension.get(host);
			for(int j = 0 ; j < cookies.length ; j++) {
				this.add(host, cookies[j]);
			}
		}
	}
	
	public static Cookies deserialize(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		Enumeration e = obj.keys();
		HashtableCookies cookies = new HashtableCookies();
		while(e.hasMoreElements()) {
			String host = (String) e.nextElement();
			JSONArray ary = obj.getJSONArray(host);
			for(int i = 0 ; i < ary.length() ; i ++) {
				cookies.add(host, ary.getString(i));
			}
		}
		return cookies;
	}
}
