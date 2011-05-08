package net.microscraper.client;


public class Reference {
	public static final String SEPARATOR = "/";
	
	public final Class klass;
	public final String creator;
	public final String title;
	
	/*
	public Reference(String threePartReference) {
		String[] split = Utils.split(threePartReference, SEPARATOR);
		this.klass = Model.get(split[0]);
		this.creator = split[1];
		this.title = split[2];
	}
	*/
	
	public Reference(Class klass, String fullName) throws IllegalArgumentException {
		this.klass = klass;
		int sep_index = fullName.indexOf(SEPARATOR);
		this.creator = fullName.substring(0, sep_index);
		this.title = fullName.substring(sep_index + 1);
	}
	
	public Reference(Class klass, String creator, String title) {
		this.klass = klass;
		this.creator = creator;
		this.title = title;
	}
	
	public String toString() {
		String creator_string = (creator == null) ? "" : creator;
		return klass.toString() + SEPARATOR + creator_string + SEPARATOR + title;
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
	
	public static Reference[] fromArray(Class resourceClass, String[] strings) {
		Reference[] references = new Reference[strings.length];
		for(int i = 0; i < strings.length; i ++) {
			references[i] = new Reference(resourceClass, strings[i]);
		}
		return references;
	}
}
