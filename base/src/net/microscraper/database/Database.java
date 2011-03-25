package net.microscraper.database;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.schema.*;

public class Database {
	public final AbstractModel[] models = {
		new Data.Model(), new Default.Model(), new Regexp.Model(), new Scraper.Model(),
		new WebPage.Model(), new AbstractHeader.Cookie.Model(), new AbstractHeader.Header.Model(),
		new AbstractHeader.Post.Model()
	};
	//private final Hashtable collections = new Hashtable();
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Database(Interfaces.JSON.Object json_obj) throws JSONInterfaceException {		
		// This looks for our specified models and creates 'dead' resources.
		for(int i = 0; i < models.length; i++) {
			AbstractModel model = models[i].initialize(this);
			if(json_obj.has(model.key)) {
				models[i].inflate(json_obj.getJSONObject(model.key));
				//collections.put(model, new Collection(model, json_obj.getJSONObject(model.key)));
			}
		}
		// This hunts back through our models and revives the resources, since all
		// references should be satsify-able.
		//for(int i = 0; i < models.length; i++) {
			/*try {
				DefaultResource[] resources = get(MODELS[i]).all();
				for(int j = 0 ; j < resources.length; j++) {
					resources[j].revive(this);
				}
			} catch (DatabaseException e) {
				throw new InstantiationError();
			}*/
		//}
	}
	/*
	public Resource get(AbstractModel model, Reference ref)
					throws ModelNotFoundException, ResourceNotFoundException {
		return get(model).get(ref);
	}
	*/
	/*
	public Collection get(AbstractModel model) throws ModelNotFoundException {
		try {
			return (Collection) collections.get(model);
		} catch(NullPointerException e) {
			throw new ModelNotFoundException();
		}
	}
	*/
}
