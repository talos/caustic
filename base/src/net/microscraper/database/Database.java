package net.microscraper.database;

import java.util.Hashtable;

import net.microscraper.client.Interfaces;
import net.microscraper.client.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.DatabaseException.ModelNotFoundException;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.schema.*;
import net.microscraper.database.schema.AbstractHeader.*;

public class Database {
	public static final AbstractModel[] MODELS = {
		new Data(), new Default(), new Regexp(), new Scraper(), new WebPage(),
		new Cookie(), new Header(), new Post()
	};
	
	private final Hashtable collections = new Hashtable();
	
	/**
	 * Inflate a new, functioning database from a JSON object.
	 * @param json_obj
	 * @throws JSONInterfaceException
	 */
	public Database(Interfaces.JSON.Object json_obj) throws JSONInterfaceException {
		// This looks for our specified models and creates 'dead' resources.
		for(int i = 0; i < MODELS.length; i++) {
			AbstractModel model = MODELS[i];
			if(json_obj.has(model.key)) {
				collections.put(model, new Collection(model, json_obj.getJSONObject(model.key)));
			}
		}
		// This hunts back through our models and revives the resources, since all
		// references should be satsify-able.
		for(int i = 0; i < MODELS.length; i++) {
			try {
				AbstractResource[] resources = get(MODELS[i]).all();
				for(int j = 0 ; j < resources.length; j++) {
					resources[j].revive(this);
				}
			} catch (DatabaseException e) {
				throw new InstantiationError();
			}
		}
	}
	
	public AbstractResource get(AbstractModel model, Reference ref)
					throws ModelNotFoundException, ResourceNotFoundException {
		return get(model).get(ref);
	}
	
	public Collection get(AbstractModel model) throws ModelNotFoundException {
		try {
			return (Collection) collections.get(model);
		} catch(NullPointerException e) {
			throw new ModelNotFoundException();
		}
	}
}
