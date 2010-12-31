package com.invisiblearchitecture.scraper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import com.invisiblearchitecture.scraper.JSONInterface.IteratorInterface;
import com.invisiblearchitecture.scraper.JSONInterface.JSONInterfaceArray;
import com.invisiblearchitecture.scraper.JSONInterface.JSONInterfaceException;
import com.invisiblearchitecture.scraper.JSONInterface.JSONInterfaceObject;
import com.invisiblearchitecture.scraper.JSONInterface.JSONInterfaceTokener;

public class JSONInformationFactory implements InformationFactory {
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
	
	//private static final String GATHERER_ID = "gathererId";
	
	private static final String SOURCE_ATTRIBUTE = "source_attribute";
	private static final String REGEX = "regex";
	private static final String MATCH_NUMBER = "match_number";
	private static final String TARGET_ATTRIBUTE = "destination_field";
	
	private static final String TARGET_AREAS = "target_area";
	private static final String TARGET_TYPES = "target_type";
	
	private static final String URL = "url";
	private static final String POSTS = "posts";
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	//private static final String TERMINATORS = "terminators";
	
	public JSONInformationFactory(String reqUrl, String reqCreator,
			HttpInterface httpInt,
			LogInterface log, RegexInterface regexInt,
			JSONInterface jsonInt, Collector collect,
			Publisher publish) {
		requestUrl = reqUrl;
		requestCreator = reqCreator;
		httpInterface = httpInt;
		logger = log;
		jsonInterface = jsonInt;
		collector = collect;
		publisher = publish;
		regexInterface = regexInt;
	}
	
	@Override
	public Information get(String area, String type) throws IOException {
		try {
			EntityInterface entity = 
				httpInterface.attributesToEntity(requestUrl + '/' + requestCreator + '/' + area + '/' + type,
					null, null, null, null, null);
			InputStream inputStream = entity.getInputStream();
			
			byte[] buffer = new byte[512];
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			//int readBytes;
			
			//while((readBytes = inputStream.read(buffer)) != -1) {
			while(inputStream.read(buffer) != -1) {
				content.write(buffer);
				//content.write(sBuffer, 0, readBytes);
			}
			JSONInterfaceTokener tokener = jsonInterface.getTokener(content.toString());
			JSONInterfaceObject object = tokener.nextValue();
			
			JSONInterfaceArray publishesRaw = object.getJSONArray(PUBLISHES);
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
				PatternInterface pattern = null;
				if(interpreterRaw.has(REGEX)) {
					pattern = regexInterface.compile(interpreterRaw.getString(REGEX));
				}
				interpreters[i] = new Interpreter.ToField(
						interpreterRaw.getString(SOURCE_ATTRIBUTE),
						pattern,
						interpreterRaw.getInt(MATCH_NUMBER),
						interpreterRaw.getString(TARGET_ATTRIBUTE));
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
				String[] target_areas = new String[generatorRaw.getJSONArray(TARGET_AREAS).length()];
				for(int j = 0; j < target_areas.length; j++) {
					target_areas[j] = generatorRaw.getJSONArray(TARGET_AREAS).getString(j);
				}
				String[] target_types = new String[generatorRaw.getJSONArray(TARGET_TYPES).length()];
				for(int j = 0; j < target_types.length; j++) {
					target_types[j] = generatorRaw.getJSONArray(TARGET_TYPES).getString(j);
				}
				// TODO: the target_areas/target_types system is designed wrong at the backend level.
				String targets[][] = new String[1][2];
				targets[0][0] = target_areas[0];
				targets[0][1] = target_types[0];
				interpreters[i] = new Interpreter.ToInformation(
						this, generatorRaw.getString(SOURCE_ATTRIBUTE), pattern,
						targets,
						generatorRaw.getString(TARGET_ATTRIBUTE));
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
			
			Information information = new Information(collector, area, type,
					count++, publishes, interpreters, gatherers,
					publisher, httpInterface.newCookieStore(), logger);
			information.putFields(defaults);
			return information;
		} catch(JSONInterfaceException e) {
		//	logger.e("Error parsing JSON in JSONInformationFactory", e);
			throw new IOException(e);
		}
	}
	
	private Gatherer gathererFromJSON(String gatherer_name, JSONInterfaceObject gathererRaw) throws JSONInterfaceException {

		Gatherer gatherer = new Gatherer(gatherer_name,
				httpInterface, regexInterface, logger);
		
		gatherer.addUrl(gathererRaw.getString(URL));
		
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
