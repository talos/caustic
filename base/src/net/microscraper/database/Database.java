package net.microscraper.database;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.PrematureRevivalException;
import net.microscraper.database.schema.*;

public class Database {
	private final AbstractModel data_model;
	private final AbstractModel[] models;
	//private final Logger logger;
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Database(Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		data_model = new Data.Model();
		models = new AbstractModel[] {
			data_model, new Default.Model(), new Regexp.Model(), new Scraper.Model(),
			new WebPage.Model(), new AbstractHeader.Cookie.Model(), new AbstractHeader.Header.Model(),
			new AbstractHeader.Post.Model()
		};
		for(int i = 0; i < models.length; i++) {
			if(json_obj.has(models[i].key)) {
				models[i].inflate(json_obj.getJSONObject(models[i].key));
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
}
