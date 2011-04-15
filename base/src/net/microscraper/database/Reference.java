package net.microscraper.database;

public class Reference {
	public static final String SEPARATOR = "/";
	
	public final Model model;
	public final String creator;
	public final String title;
	
	public Reference(Model model, String full_name) throws IllegalArgumentException {
		this.model = model;
		int sep_index = full_name.indexOf(SEPARATOR);
		if(sep_index == -1) {
			creator = null;
			title = full_name;
		} else {
			creator = full_name.substring(0, sep_index);
			title = full_name.substring(sep_index + 1);
		}
		if(title == null) {
			throw new IllegalArgumentException("Title of reference cannot be null.");
		}
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
	public static Reference blank(AbstractResource resource) {
		return new Reference(Model.get(resource.getClass()), "");
	}
}
