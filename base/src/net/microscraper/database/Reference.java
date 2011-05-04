package net.microscraper.database;

import net.microscraper.client.Utils;

public class Reference {
	public static final String SEPARATOR = "/";
	
	public final Model model;
	public final String creator;
	public final String title;
	
	public Reference(String threePartReference) {
		String[] split = Utils.split(threePartReference, SEPARATOR);
		this.model = Model.get(split[0]);
		this.creator = split[1];
		this.title = split[2];
	}
	
	public Reference(Model model, String fullName) throws IllegalArgumentException {
		this.model = model;
		int sep_index = fullName.indexOf(SEPARATOR);
		this.creator = fullName.substring(0, sep_index);
		this.title = fullName.substring(sep_index + 1);
	}
	
	public Reference(Model model, String creator, String title) {
		this.model = model;
		this.creator = creator;
		this.title = title;
	}
	
	public String toString() {
		String creator_string = (creator == null) ? "" : creator;
		return model + SEPARATOR + creator_string + SEPARATOR + title;
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
	
	public static Reference[] fromArray(String model_name, String[] strings) {
		Reference[] references = new Reference[strings.length];
		for(int i = 0; i < strings.length; i ++) {
			references[i] = new Reference(Model.get(model_name), strings[i]);
		}
		return references;
	}
}
