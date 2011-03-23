package net.microscraper.database.schema;

public class Reference {
	public static final String SEPARATOR = "/";
	
	public final String creator;
	public final String title;
	
	public Reference(String full_name) {
		creator = full_name.substring(0, full_name.indexOf(SEPARATOR));
		title = full_name.substring(full_name.indexOf(SEPARATOR) + 1);
	}
	
	public Reference(String _creator, String _title) {
		creator = _creator;
		title = _title;
	}
	
	public String toString() {
		return creator + SEPARATOR + title;
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Reference))
			return false;
		return this.toString() == obj.toString();
	}
	
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public static Reference[] fromArray(String[] strings) {
		Reference[] references = new Reference[strings.length];
		for(int i = 0; i < strings.length; i ++) {
			references[i] = new Reference(strings[i]);
		}
		return references;
	}
}
