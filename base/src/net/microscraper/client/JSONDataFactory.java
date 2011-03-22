package net.microscraper.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Hashtable;

import net.microscraper.client.JSONInterface.IteratorInterface;
import net.microscraper.client.JSONInterface.JSONInterfaceArray;
import net.microscraper.client.JSONInterface.JSONInterfaceException;
import net.microscraper.client.JSONInterface.JSONInterfaceObject;
import net.microscraper.client.JSONInterface.JSONInterfaceTokener;
import net.microscraper.client.deprecated.EntityInterface;
import net.microscraper.client.deprecated.Gatherer;
import net.microscraper.client.deprecated.HttpInterface;
import net.microscraper.client.deprecated.Information;
import net.microscraper.client.deprecated.Interpreter;
import net.microscraper.client.deprecated.Publisher;
import net.microscraper.client.interfaces.LogInterface;


public class JSONDataFactory implements DataFactory {
	private final HttpInterface httpInterface;
	private final String requestUrl;
	private final String requestCreator;
	private final LogInterface logger;
	private final JSONInterface jsonInterface;
	private final Collector collector;
	private final Publisher publisher;
	private final RegexInterface regexInterface;
	private int count = 0;
	
	private static final String PUBLISHES = "publishes";
	private static final String DEFAULTS = "defaults";
	private static final String INTERPRETERS = "interpreters";
	private static final String GENERATORS = "generators";
	private static final String GATHERERS = "gatherers";
	
	private static final String SOURCE_DATAS = "source_datas";
	private static final String REGEXES = "regexes";
	private static final String MATCH_NUMBER = "match_number";
	private static final String TARGET_DATAS = "target_datas";
	private static final String TERMINATE_ON_COMPLETE = "terminate_on_complete";
	
	private static final String URLS = "urls";
	private static final String POSTS = "posts";
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	
	private final Hashtable cache = new Hashtable();
	private final boolean useCache;
	
	/**
	 * 
	 * @param reqUrl
	 * @param reqCreator
	 * @param httpInt
	 * @param log
	 * @param regexInt
	 * @param jsonInt
	 * @param collect
	 * @param publish
	 * @param useCache Whether the factory should save JSON information objects based off of area/info pairs.
	 * 	Should be false in development, true in production.
	 */
	public JSONDataFactory(String reqUrl, String reqCreator,
			HttpInterface httpInt,
			LogInterface log, RegexInterface regexInt,
			Json jsonInt, Collector collect,
			Publisher publish, boolean n_useCache) {
		requestUrl = reqUrl;
		requestCreator = reqCreator;
		httpInterface = httpInt;
		logger = log;
		jsonInterface = jsonInt;
		collector = collect;
		publisher = publish;
		regexInterface = regexInt;
		useCache = n_useCache;
	}
	
	public Information get(String creator, String name) throws IOException, JSONInterfaceException {
		creator = URLEncoder.encode(creator, "UTF-8");
		name = URLEncoder.encode(name, "UTF-8");
		
		String url = requestUrl + '/' + creator + '/' + name + "?format=json";
		
		/* Obtain JSON object. */
		String jsonResponse;
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
		}
		
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
			/* catch(JSONInterfaceException e) {
		//	logger.e("Error parsing JSON in JSONInformationFactory", e);
			throw new IOException(e);
		}*/
	}
	
	private Gatherer gathererFromJSON(String gatherer_name, JSONInterfaceObject gathererRaw) throws JSONInterfaceException {

		Gatherer gatherer = new Gatherer(gatherer_name,
				httpInterface, regexInterface, logger);
		
		if(gathererRaw.isNull(URLS) == false) {
			JSONInterfaceArray urlsRaw = gathererRaw.getJSONArray(URLS);
			for(int i = 0; i < urlsRaw.length(); i++) {
				gatherer.addUrl(urlsRaw.getString(i));
			}
		}
		
		if(gathererRaw.isNull(POSTS) == false) {
			JSONInterfaceObject postsRaw = gathererRaw.getJSONObject(POSTS);
			IteratorInterface iterator = postsRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = postsRaw.getString(key);
				gatherer.addPost(key, value);
			}
		}
		
		if(gathererRaw.isNull(COOKIES) == false) {
			JSONInterfaceObject cookiesRaw = gathererRaw.getJSONObject(COOKIES);
			IteratorInterface iterator = cookiesRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = cookiesRaw.getString(key);
				gatherer.addCookie(key, value);
			}
		}
		
		if(gathererRaw.isNull(HEADERS) == false) {
			JSONInterfaceObject headersRaw = gathererRaw.getJSONObject(HEADERS);
			IteratorInterface iterator = headersRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = headersRaw.getString(key);
				gatherer.addHeader(key, value);
			}
		}
		
		/*if(gathererRaw.isNull(TERMINATORS) == false) {
			JSONInterfaceArray terminatorsRaw = gathererRaw.getJSONArray(TERMINATORS);
			for(int i = 0; i < terminatorsRaw.length(); i++) {
				PatternInterface terminator = regexInterface.compile(terminatorsRaw.getString(i));
				gatherer.addTerminator(terminator);
			}
		}*/
		
		return gatherer;
	}
}
