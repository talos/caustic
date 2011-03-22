package net.microscraper.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Hashtable;

import net.microscraper.client.deprecated.Gatherer;
import net.microscraper.client.deprecated.Information;
import net.microscraper.client.deprecated.Interpreter;
import net.microscraper.client.interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.interfaces.JSON;
import net.microscraper.database.schema.WebPage;

/**
 * Creates new Information objects ready for collection from a namespace & type.
 * @author john
 *
 */
public interface DataFactory {
	public abstract Data get(String creator, String name) throws Exception;
	
	public static class JSON implements DataFactory {
		private final String host;
		private final Browser browser;
		private final net.microscraper.client.interfaces.JSON j;
		public JSON(String _host, Browser _browser) {
			host = _host;
			browser = _browser;
		}
		
		public Data get(String creator, String name) throws IOException, JSONInterfaceException {
			creator = URLEncoder.encode(creator, "UTF-8");
			name = URLEncoder.encode(name, "UTF-8");
			
			String url = host + '/' + creator + '/' + name + "?format=json";
			String jsonResponse = browser.load(new WebPage(url));
			
			/* Obtain JSON object. */
			/*String jsonResponse;
			if(cache.containsKey(url) && useCache) {
				jsonResponse = (String) cache.get(url);
			} else {
				EntityInterface entity = httpInterface.attributesToEntity(url, null, null, null, null, null);
				InputStream inputStream = entity.getInputStream();
				
				byte[] buffer = new byte[512];
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				int readBytes = 0;
				
				while((readBytes = inputStream.read(buffer)) != -1) {
					content.write(buffer, 0, readBytes);
				}
				jsonResponse = content.toString();
				cache.put(url, jsonResponse);
			}*/
			
			/* Process JSON object. */
			JSONInterfaceTokener tokener = jsonInterface.getTokener(jsonResponse);
			JSONInterfaceObject object = tokener.nextValue();
			
			//JSONInterfaceArray publishesRaw = object.getJSONArray(PUBLISHES);
			JSONInterfaceObject defaultsRaw = object.getJSONObject(DEFAULTS);
			JSONInterfaceObject interpretersRaw = object.getJSONObject(INTERPRETERS);
			JSONInterfaceObject generatorsRaw = object.getJSONObject(GENERATORS);
			JSONInterfaceObject gatherersRaw = object.getJSONObject(GATHERERS);
			
			String[] publishes = new String[publishesRaw.length()];
			for(int i = 0; i < publishesRaw.length(); i++) {
				publishes[i] = publishesRaw.getString(i);
			}
			
			Hashtable defaults = new Hashtable(defaultsRaw.length(), 1);
			IteratorInterface iterator;
			iterator = defaultsRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				defaults.put(key, defaultsRaw.getString(key));
			}
			
			/* The client doesn't differentiate between 'generators' and 'interpreters' */
			Interpreter[] interpreters =
				new Interpreter[interpretersRaw.length() + generatorsRaw.length()];
			iterator = interpretersRaw.keys();
			int i = 0;
			while(iterator.hasNext()) {
				String interpreterName = (String) iterator.next();
				JSONInterfaceObject interpreterRaw = interpretersRaw.getJSONObject(interpreterName);
				//PatternInterface pattern = null;
				if(interpreterRaw.has(REGEXES)) {
					//pattern = regexInterface.compile(interpreterRaw.getString(REGEX));
					
				}
				interpreters[i] = new Interpreter.ToField(
						interpreterRaw.getJSONArray(SOURCE_ATTRIBUTES).toArray(),
						pattern,
						interpreterRaw.getInt(MATCH_NUMBER),
						interpreterRaw.getString(TARGET_ATTRIBUTE),
						logger);
				i++;
			}
			iterator = generatorsRaw.keys();
			while(iterator.hasNext()) {
				String generatorName = (String) iterator.next();
				JSONInterfaceObject generatorRaw = generatorsRaw.getJSONObject(generatorName);
				PatternInterface pattern = null;
				if(generatorRaw.has(REGEX)) {
					pattern = regexInterface.compile(generatorRaw.getString(REGEX));
				}
				interpreters[i] = new Interpreter.ToInformation(
						this, generatorRaw.getJSONArray(SOURCE_ATTRIBUTES).toArray(),
						pattern,
						generatorRaw.getString(TARGET_AREA),
						generatorRaw.getString(TARGET_INFO),
						generatorRaw.getString(TARGET_ATTRIBUTE),
						logger);
				i++;
			}
			
			Gatherer[] gatherers = new Gatherer[gatherersRaw.length()];
			iterator = gatherersRaw.keys();
			i = 0;
			while(iterator.hasNext()) {
				String gathererName = (String) iterator.next();
				JSONInterfaceObject gathererRaw = gatherersRaw.getJSONObject(gathererName);
				gatherers[i] = gathererFromJSON(gathererName, gathererRaw);
				i++;
			}
			
			Information information = new Information(collector, area, info,
					count++, publishes, interpreters, gatherers,
					publisher, httpInterface.newCookieStore(), logger);
			information.putFields(defaults);
			return information;
		}
	}
}
