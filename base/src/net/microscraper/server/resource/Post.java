package net.microscraper.server.resource;

import net.microscraper.client.executable.MustacheEncodedNameValuePair;
import net.microscraper.client.executable.MustacheNameValuePair;
import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.server.MustacheTemplate;


public class Post extends MustacheEncodedNameValuePair {
	private final MustacheNameValuePair nameValuePair;
	public Post(MustacheNameValuePair nameValuePair) {
		this.nameValuePair = nameValuePair;
	}

	public MustacheTemplate getName() {
		return nameValuePair.getName();
	}

	public MustacheTemplate getValue() {
		return nameValuePair.getValue();
	}
	
	/**
	 * Deserialize an array of {@link Post}s from a hash in {@link JSONInterfaceObject}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An array of {@link Post}.
	 * @throws DeserializationException If this is not a valid JSON Hash of Posts.
	 */
	public static Post[] deserializeHash(JSONInterface jsonInterface,
			JSONInterfaceObject jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.Deserializer.deserializeHash(jsonInterface, jsonObject);
		Post[] posts = new Post[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			posts[i] = new Post(nameValuePairs[i]);
		}
		return posts;
	}
}
