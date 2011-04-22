package net.microscraper.database;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.database.Database.ResourceNotFoundException;

public class Relationship {
	public static class Relationships {
		private final Hashtable relationships = new Hashtable();

		public void put(RelationshipDefinition def, Reference reference) {
			Vector references;
			if(relationships.containsKey(def)) {
				references = (Vector) relationships.get(def);
			} else {
				references = new Vector();
				relationships.put(def, references);
			}
			references.addElement(reference);
		}
		
		public Resource[] get(RelationshipDefinition def) throws ResourceNotFoundException {
			Vector references = (Vector) relationships.get(def);
			Resource[] resources = new Resource[references.size()];
			for(int i = 0; i < references.size() ; i ++) {
				resources[i] = Client.db.get((Reference) references.elementAt(i));
			}
			return resources;
		}
		
		public int getSize(RelationshipDefinition def) {
			return ((Vector) relationships.get(def)).size();
		}
	}
	
	public static class RelationshipDefinition {
		public final String key;
		public final Model target_model;
		public RelationshipDefinition(String key, Class klass) {
			this.key = key;
			this.target_model = Model.get(klass);
		}
	}
}