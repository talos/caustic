package net.microscraper.database;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.schema.*;

public class Database {
	private AbstractModel[] models = {
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
			AbstractModel model = models[i].initialize(this);
			if(json_obj.has(model.key)) {
				models[i].inflate(json_obj.getJSONObject(model.key));
			}
		}
	}
}
