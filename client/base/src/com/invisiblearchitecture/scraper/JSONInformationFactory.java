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
	private final LogInterface logger;
	private final JSONInterface jsonInterface;
	private final Collector collector;
	private final Publisher publisher;
	private final RegexInterface regexInterface;
	private int count = 0;
	
	private static final String FIELDS_TO_PUBLISH = "fieldsToPublish";
	private static final String DEFAULT_FIELDS = "defaultFields";
	private static final String INTERPRETERS_TO_FIELDS = "interpretersToFields";
	private static final String INTERPRETERS_TO_INFORMATIONS = "informationsToInformations";
	private static final String GATHERERS = "gatherers";
	
	private static final String GATHERER_ID = "gathererId";
	
	private static final String SOURCE_FIELD = "sourceField";
	private static final String REGEX = "regex";
	private static final String NUMBER = "number";
	private static final String DESTINATION_FIELD = "destinationField";
	
	private static final String DESTINATION_NAMESPACE = "destinationNamespace";
	private static final String DESTINATION_INFORMATION_TYPE = "destinationInformationType";
	
	private static final String URLS = "urls";
	
	private static final String GETS = "gets";
	private static final String POSTS = "posts";
	private static final String COOKIES = "cookies";
	private static final String HEADERS = "headers";
	private static final String TERMINATORS = "terminators";
	private static final String PARENTS = "parents";
	
	public JSONInformationFactory(String reqUrl, HttpInterface httpInt,
			LogInterface log, RegexInterface regexInt,
			JSONInterface jsonInt, Collector collect,
			Publisher publish) {
		requestUrl = reqUrl;
		httpInterface = httpInt;
		logger = log;
		jsonInterface = jsonInt;
		collector = collect;
		publisher = publish;
		regexInterface = regexInt;
	}
	
	@Override
	public Information get(String namespace, String type) throws IOException {
		Hashtable gets = new Hashtable(2, 1);
		gets.put("namespace", namespace);
		gets.put("type", type);
		
		try {
			EntityInterface entity = 
				httpInterface.attributesToEntity(requestUrl,
					null, gets, null, null, null);
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
			
			JSONInterfaceArray fieldsToPublishRaw = object.getJSONArray(FIELDS_TO_PUBLISH);
			JSONInterfaceObject defaultFieldsRaw = object.getJSONObject(DEFAULT_FIELDS);
			JSONInterfaceArray interpretersToFieldsRaw = object.getJSONArray(INTERPRETERS_TO_FIELDS);
			JSONInterfaceArray interpretersToInformationsRaw = object.getJSONArray(INTERPRETERS_TO_INFORMATIONS);
			JSONInterfaceArray gatherersRaw = object.getJSONArray(GATHERERS);
			
			String[] fieldsToPublish = new String[fieldsToPublishRaw.length()];
			for(int i = 0; i < fieldsToPublishRaw.length(); i++) {
				fieldsToPublish[i] = fieldsToPublishRaw.getString(i);
			}
			
			Hashtable defaultFields = new Hashtable(defaultFieldsRaw.length(), 1);
			IteratorInterface iterator = defaultFieldsRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				defaultFields.put(key, defaultFieldsRaw.getString(key));
			}
			
			Interpreter[] interpreters =
				new Interpreter[interpretersToFieldsRaw.length() + interpretersToInformationsRaw.length()];
			for(int i = 0; i < interpretersToFieldsRaw.length(); i++) {
				JSONInterfaceObject interpreterRaw = interpretersToFieldsRaw.getJSONObject(i);
				PatternInterface pattern = null;
				if(interpreterRaw.has(REGEX)) {
					pattern = regexInterface.compile(interpreterRaw.getString(REGEX));
				}
				interpreters[i] = new Interpreter.ToField(
						interpreterRaw.getString(SOURCE_FIELD),
						pattern,
						interpreterRaw.getInt(NUMBER),
						interpreterRaw.getString(DESTINATION_FIELD));
			}
			for(int i = interpretersToFieldsRaw.length();
					i < interpretersToInformationsRaw.length() + interpretersToFieldsRaw.length();
					i++)  {
				JSONInterfaceObject interpreterRaw = interpretersToInformationsRaw.getJSONObject(i);
				PatternInterface pattern = null;
				if(interpreterRaw.has(REGEX)) {
					pattern = regexInterface.compile(interpreterRaw.getString(REGEX));
				}
				interpreters[i] = new Interpreter.ToInformation(
						this, interpreterRaw.getString(SOURCE_FIELD), pattern,
						interpreterRaw.getString(DESTINATION_NAMESPACE),
						interpreterRaw.getString(DESTINATION_INFORMATION_TYPE),
						interpreterRaw.getString(DESTINATION_FIELD));
			}
			
			Gatherer[] gatherers = new Gatherer[gatherersRaw.length()];
			for(int i = 0; i < gatherersRaw.length(); i++) {
				JSONInterfaceObject gathererRaw = gatherersRaw.getJSONObject(i);
				gatherers[i] = gathererFromJSON(gathererRaw);
			}
			
			Information information = new Information(collector, namespace, type,
					count++, fieldsToPublish, interpreters, gatherers,
					publisher, httpInterface.newCookieStore(), logger);
			information.putFields(defaultFields);
			return information;
		} catch(JSONInterfaceException e) {
		//	logger.e("Error parsing JSON in JSONInformationFactory", e);
			throw new IOException(e);
		}
	}
	
	private Gatherer gathererFromJSON(JSONInterfaceObject gathererRaw) throws JSONInterfaceException {

		Gatherer gatherer = new Gatherer(
				gathererRaw.getString(GATHERER_ID),
				httpInterface, regexInterface, logger);
		
		if(gathererRaw.isNull(URLS) == false) {
			JSONInterfaceArray urlsRaw = gathererRaw.getJSONArray(URLS);	
			for(int i = 0; i < urlsRaw.length(); i++) {
				gatherer.addUrl(urlsRaw.getString(i));
			}
		}
		
		if(gathererRaw.isNull(GETS) == false) {
			JSONInterfaceObject getsRaw = gathererRaw.getJSONObject(GETS);
			IteratorInterface iterator = getsRaw.keys();
			while(iterator.hasNext()) {
				String key = (String) iterator.next();
				String value = getsRaw.getString(key);
				gatherer.addGet(key, value);
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
		
		if(gathererRaw.isNull(TERMINATORS) == false) {
			JSONInterfaceArray terminatorsRaw = gathererRaw.getJSONArray(TERMINATORS);
			for(int i = 0; i < terminatorsRaw.length(); i++) {
				PatternInterface terminator = regexInterface.compile(terminatorsRaw.getString(i));
				gatherer.addTerminator(terminator);
			}
		}
		
		if(gathererRaw.isNull(PARENTS) == false) {
			JSONInterfaceArray parentsRaw = gathererRaw.getJSONArray(PARENTS);
			for(int i = 0; i < parentsRaw.length(); i++) {
				JSONInterfaceObject parentGathererRaw = parentsRaw.getJSONObject(i);
				gatherer.addParentGatherer(gathererFromJSON(parentGathererRaw));
			}
		}
		
		return gatherer;
	}
}
