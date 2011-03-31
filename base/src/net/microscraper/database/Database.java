package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.Iterator;
import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.schema.*;

public class Database {
	//private final AbstractModel data_model;
	private static final ModelDefinition[] models = new ModelDefinition[] {
		new Data.Model(), new Default.Model(), new Regexp.Model(), new Scraper.Model(),
		new WebPage.Model(), new AbstractHeader.Cookie.Model(), new AbstractHeader.Header.Model(),
		new AbstractHeader.Post.Model()
	};
	
	private Hashtable resources = new Hashtable();
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Database(Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		for(int i = 0; i < models.length; i++) {
			if(json_obj.has(models[i].key())) {
				//models[i].inflate(json_obj.getJSONObject(models[i].key()));
				inflateModel(json_obj.getJSONObject(models[i].key()), models[i]);
			}
		}
	}
	public Resource get(String model_key, Reference reference) {
		return (Resource) modelResources(model_key).get(reference);
	}
	public Resource[] get(String model_key) {
		Hashtable model_resources = modelResources(model_key);
		Resource[] resources = new Resource[model_resources.size()];
		Utils.hashtableValues(model_resources, resources);
		return resources;
	}
	private Hashtable modelResources(String model_key) {
		return (Hashtable) resources.get(model_key);
	}
	/*
	public Data[] datas() throws PrematureRevivalException {
		Resource[] resources = data_model.all();
		Data[] datas = new Data[resources.length];
		for(int i = 0; i < resources.length; i ++) {
			datas[i] = new Data(resources[i]);
		}
		return datas;
	}
	*/
	private void inflateModel(Interfaces.JSON.Object json_obj, ModelDefinition model) throws JSONInterfaceException {
		if(!resources.containsKey(model.key()))
			resources.put(model.key(), new Hashtable());
		Hashtable model_resources = (Hashtable) resources.get(model.key());
		Iterator keys = json_obj.keys();
		while(keys.hasNext()) {
			String key = (String) keys.next();
			Resource resource = Resource.inflate(this, model, new Reference(key), json_obj.getJSONObject(key));
			model_resources.put(resource.ref, resource);
		}
	}
	/*
	public class Model implements AbstractModel {
		private final Hashtable resources = new Hashtable();
		private final AbstractModel base_model;
		
		public String key() {
			return base_model.key();
		}
		public String[] attributes() {
			return base_model.attributes();
		}
		public Relationship[] relationships() {
			return base_model.relationships();
		}
		
		public Model (AbstractModel _base_model, JSON.Object json_obj) throws JSONInterfaceException {
			base_model = _base_model;
			JSON.Iterator i = json_obj.keys();
			while(i.hasNext()) {
				String key = (String) i.next();
				Reference ref = new Reference(key);
				resources.put(ref, Resource.inflate(this, ref, json_obj.getJSONObject(key)) );
			}
		}
		
		public Resource get(Reference ref) throws PrematureRevivalException {
			return ((Resource) resources.get(ref));
		}
	}*/
}
