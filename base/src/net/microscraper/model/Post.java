package net.microscraper.model;

import net.microscraper.client.Interfaces;


public class Post implements MustacheNameValuePair {
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
	 * Deserialize an array of {@link Post}s from a hash in {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An array of {@link Post}.
	 * @throws DeserializationException If this is not a valid JSON Hash of Posts.
	 */
	public static Post[] deserializeHash(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Object jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.Deserializer.deserializeHash(jsonInterface, jsonObject);
		Post[] posts = new Post[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			posts[i] = new Post(nameValuePairs[i]);
		}
		return posts;
	}
}
