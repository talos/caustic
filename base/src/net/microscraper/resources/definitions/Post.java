package net.microscraper.resources.definitions;

import net.microscraper.client.Interfaces;


public class Post {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
	public Post(MustacheNameValuePair nameValuePair) {
		name = nameValuePair.name;
		value = nameValuePair.value;
	}
	
	/**
	 * Deserialize an array of {@link Post} from a hash in {@link Interfaces.JSON.Object}.
	 * @param jsonInterface {@link Interfaces.JSON} used to process JSON.
	 * @param jsonObject Input {@link Interfaces.JSON.Object} object.
	 * @return An array of {@link Post}.
	 * @throws DeserializationException If this is not a valid JSON Hash of Posts.
	 */
	public static Post[] deserializeHash(Interfaces.JSON jsonInterface,
			Interfaces.JSON.Object jsonObject) throws DeserializationException {
		MustacheNameValuePair[] nameValuePairs = MustacheNameValuePair.deserializeHash(jsonInterface, jsonObject);
		Post[] posts = new Post[nameValuePairs.length];
		for(int i = 0 ; i < nameValuePairs.length ; i ++) {
			posts[i] = new Post(nameValuePairs[i]);
		}
		return posts;
	}
}
