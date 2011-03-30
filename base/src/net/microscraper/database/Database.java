package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Utils;
import net.microscraper.client.Interfaces.JSON;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.AbstractModel.Relationships;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.schema.*;

public class Database {
	//private final AbstractModel data_model;
	private static final AbstractModel[] _abstract_models = new AbstractModel[] {
		new Data.Model(), new Default.Model(), new Regexp.Model(), new Scraper.Model(),
		new WebPage.Model(), new AbstractHeader.Cookie.Model(), new AbstractHeader.Header.Model(),
		new AbstractHeader.Post.Model()
	};
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Database(Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		for(int i = 0; i < models.length; i++) {
			if(json_obj.has(models[i].key())) {
				//models[i].inflate(json_obj.getJSONObject(models[i].key()));
			}
		}
	}
	
	public Data[] datas() throws PrematureRevivalException {
		Resource[] resources = data_model.all();
		Data[] datas = new Data[resources.length];
		for(int i = 0; i < resources.length; i ++) {
			datas[i] = new Data(resources[i]);
		}
		return datas;
	}

	public class Model implements AbstractModel {
		private final Hashtable resources = new Hashtable();
		private final AbstractModel base_model;
		
		/*private Model(AbstractModel _base_model) {
			base_model = _base_model;
		}
		*/
		public String key() {
			return base_model.key();
		}
		public String[] attributes() {
			return base_model.attributes();
		}
		public Relationship[] relationships() {
			return base_model.relationships();
		}
		
		/**
		 * Fill a model with resources pulled from a JSON object.
		 * @param json_obj
		 * @throws JSONInterfaceException
		 */
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
		/*
		public Resource[] all() throws PrematureRevivalException {
			Reference[] references = new Reference[resources.size()];
			Resource[] resources_ary = new Resource[resources.size()];
			Utils.hashtableKeys(resources, references);
			for(int i = 0; i < references.length; i++) {
				resources_ary[i] = get(references[i]);
			}
			return resources_ary;
		}
		*/
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(!(obj instanceof AbstractModel))
				return false;
			return key().equals(((AbstractModel) obj).key());
		}
		
		public int hashCode() {
			return key().hashCode();
		}
	}
}
