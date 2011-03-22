package net.microscraper.client.deprecated;

import java.io.IOException;
import java.util.Hashtable;



public interface HttpInterface {
	
	public abstract EntityInterface attributesToEntity(String url, 
			CookieStoreInterface cookieStore, Hashtable gets, Hashtable posts,
			CookieInterface[] cookies, Hashtable headers) throws IOException;
	
	public abstract CookieStoreInterface newCookieStore();
}
