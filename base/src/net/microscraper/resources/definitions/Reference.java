package net.microscraper.resources.definitions;


public class Reference {
	private final String ref;
	public Reference(String ref) {
		this.ref = ref;
	}
	
	public String toString() {
		return ref.toString();
	}
	
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof Reference))
			return false;
		return this.toString().equals(obj.toString());
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
