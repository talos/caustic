package net.microscraper.database;

import net.microscraper.database.DatabaseException.PrematureRevivalException;

public interface Collection {
	public AbstractModel model();
	public Resource get(Reference ref) throws PrematureRevivalException;
	public Resource[] all() throws PrematureRevivalException;
	/*public final AbstractModel model;
	private final Hashtable resources = new Hashtable();
	*/
	/**
	 * Fill a collection with resources pulled via reference from a Database.
	 * @param _model
	 * @param references
	 * @param db
	 * @throws ResourceNotFoundException 
	 * @throws ModelNotFoundException 
	 */
	/*
	public Collection(AbstractModel _model, Reference[] references, Database db)
				throws ModelNotFoundException, ResourceNotFoundException {
		model = _model;
		for(int i = 0; i < references.length; i++) {
			Reference ref = references[i];
			resources.put(ref, db.get(model, ref));
		}
	}
	
	public Resource get(Reference ref) throws ResourceNotFoundException, PrematureRevivalException {
		try {
			return ((Resource) resources.get(ref)).revive();
		} catch(NullPointerException e) {
			throw new ResourceNotFoundException();
		}
	}
	
	public Resource[] all() {
		Resource[] resources_ary = new Resource[resources.size()];
		Utils.hashtableValues(resources, resources_ary);
		return resources_ary;
	}
	*/
}
