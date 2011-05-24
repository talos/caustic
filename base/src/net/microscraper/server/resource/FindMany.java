package net.microscraper.server.resource;

import net.microscraper.client.interfaces.JSONInterface;
import net.microscraper.client.interfaces.JSONInterfaceArray;
import net.microscraper.client.interfaces.JSONInterfaceException;
import net.microscraper.client.interfaces.JSONInterfaceObject;
import net.microscraper.client.interfaces.URIInterface;
import net.microscraper.server.Ref;
import net.microscraper.server.resource.mixin.SpawnsScrapers;

/**
 * A {@link Find} that can connect to other {@link Scraper} through {@link #getPipes},
 * and is one-to-many (even if it only has one result.)
 * Its executions do not implement {@link Variables}, because {@link FindMany} can be one-to-many.
 * @see Find
 * @see SpawnsScrapers
 * @author john
 *
 */
public class FindMany extends Find implements SpawnsScrapers {	
	private final SpawnsScrapers hasPipes;
	
	/**
	 * The first of the parser's matches to export.
	 * This is 0-indexed, so <code>0</code> is the first match.
	 * @see #maxMatch
	 * @see FindOne#match
	 */
	public final int minMatch;
	
	/**
	 * The last of the parser's matches to export.
	 * Negative numbers count backwards, so <code>-1</code> is the last match.
	 * @see #minMatch
	 * @see FindOne#match
	 */
	public final int maxMatch;
	
	public FindMany(Find find, SpawnsScrapers hasPipes,
			int minMatch, int maxMatch) throws URIMustBeAbsoluteException {
		super(find);
		this.hasPipes = hasPipes;
		this.minMatch = minMatch;
		this.maxMatch = maxMatch;
	}
	
	public Ref[] getScrapers() {
		return hasPipes.getScrapers();
	}
	
	private static final String MIN_MATCH = "min";
	private static final String MAX_MATCH = "max";
	
	/**
	 * Deserialize an {@link FindMany} from a {@link JSONInterfaceObject}.
	 * @param location A {@link URIInterface} that identifies the root of this leaf's links.
	 * @param jsonObject Input {@link JSONInterfaceObject} object.
	 * @return An {@link FindMany} instance.
	 * @throws DeserializationException If this is not a valid JSON serialization of
	 * an {@link FindMany}.
	 */
	public static FindMany deserialize(URIInterface location, JSONInterfaceObject jsonObject)
				throws DeserializationException {
		try {
			Find find = (Find) Find.deserialize(jsonObject);
			
			SpawnsScrapers hasPipes = SpawnsScrapers.Deserializer.deserialize(jsonObject);
			
			int minMatch = jsonObject.getInt(MIN_MATCH);
			int maxMatch = jsonObject.getInt(MAX_MATCH);
			
			return new FindMany(find, hasPipes, minMatch, maxMatch);
		} catch(JSONInterfaceException e) {
			throw new DeserializationException(e, jsonObject);
		}
	}
	

	/**
	 * Deserialize an array of {@link FindMany}s from a {@link JSONInterfaceArray}.
	 * @param location A {@link URIINterface}.
	 * @param jsonInterface {@link JSONInterface} used to process JSON.
	 * @param jsonArray Input {@link JSONInterfaceArray} array.
	 * @return An array of {@link FindMany} instances.
	 * @throws DeserializationException If the array contains an invalid JSON serialization of
	 * a {@link FindMany}, or if the array is invalid.
	 */
	public static FindMany[] deserializeArray(JSONInterface jsonInterface,
					URIInterface location, JSONInterfaceArray jsonArray)
				throws DeserializationException {
		FindMany[] leaves = new FindMany[jsonArray.length()];
		for(int i = 0 ; i < jsonArray.length() ; i++ ) {
			try {
				leaves[i] = FindMany.deserialize(location, jsonArray.getJSONObject(i));
			} catch(JSONInterfaceException e) {
				throw new DeserializationException(e, jsonArray, i);
			}
		}
		return leaves;
	}
}
