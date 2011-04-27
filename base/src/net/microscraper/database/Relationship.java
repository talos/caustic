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
		
		private Vector getVector(RelationshipDefinition def) {
			if(!relationships.containsKey(def))
				return null;
			return (Vector) relationships.get(def);
		}
		
		public Resource[] get(RelationshipDefinition def) throws ResourceNotFoundException {
			Vector references = getVector(def);
			if(references == null) {
				return new Resource[] {};
			} else {
				Resource[] resources = new Resource[references.size()];
				for(int i = 0; i < references.size() ; i ++) {
					resources[i] = Client.db.get((Reference) references.elementAt(i));
				}
				return resources;
			}
		}
		
		public int getSize(RelationshipDefinition def) {
			Vector references = getVector(def);
			if(references == null) {
				return 0;
			} else {
				return references.size();
			}
		}
	}
	
	public static class RelationshipDefinition {
		public final String key;
		public final Model target_model;
		public RelationshipDefinition(String key, Class klass) {
			this.key = key;
			this.target_model = Model.get(klass);
		}
		public int hashCode() {
			return key.hashCode();
		}
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(! (obj instanceof RelationshipDefinition))
				return false;
			return obj.hashCode() == hashCode();
		}
	}
}