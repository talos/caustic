package net.microscraper.mustache;

import net.microscraper.client.DeserializationException;
import net.microscraper.json.JsonIterator;
import net.microscraper.json.JsonObject;
import net.microscraper.json.JsonException;
import net.microscraper.util.BasicNameValuePair;
import net.microscraper.util.NameValuePair;
import net.microscraper.util.Variables;


/**
 * A name-value pair with Mustache substitutions done for both name and value.
 * @author john
 *
 */
public class MustacheNameValuePair {
	public final MustacheTemplate name;
	public final MustacheTemplate value;
		
	private MustacheNameValuePair(NameValuePair nameValuePair) throws MustacheCompilationException {
		this.name = MustacheTemplate.compile(nameValuePair.getName());
		this.value = MustacheTemplate.compile(nameValuePair.getValue());
	}
	
	/**
	 * Compile a {@link MustacheNameValuePair} from a {@link NameValuePair}.
	 * @param nameValuePair The {@link NameValuePair} to convert into a {@link MustacheNameValuePair}.
	 * @return A {@link MustacheNameValuePair}.
	 * @throws MustacheCompilationException If <code>nameValuePair</code> cannot be turned into a
	 * {@link MustacheTemplate}.
	 */
	/*public static MustacheNameValuePair compile(NameValuePair nameValuePair)
			throws MustacheCompilationException {
		return new MustacheNameValuePair(nameValuePair);
	}*/
	
	
	public MustacheNameValuePair(MustacheTemplate name, MustacheTemplate value) {
		this.name = name;
		this.value = value;
	}
	
	
	public static NameValuePair[] compile(MustacheNameValuePair[] nameValuePairs,
				Variables variables) {
		NameValuePair[] encodedNameValuePairs = 
			new NameValuePair[nameValuePairs.length];
		for(int i = 0; i < nameValuePairs.length ; i ++) {
			encodedNameValuePairs[i] = nameValuePairs[i].compile(variables);
		}
		return encodedNameValuePairs;
	}

}
