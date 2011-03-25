package net.microscraper.database;

public class Relationship {
	String key;
	AbstractModel model;
	public Relationship(String _key, AbstractModel _model) {
		key = _key;
		model = _model;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof AbstractModel))
			return false;
		return key.equals(((AbstractModel) obj).key);
	}
	
	public int hashCode() {
		return key.hashCode();
	}
}
