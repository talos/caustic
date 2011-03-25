package net.microscraper.database;

public class Reference {
	public static final String SEPARATOR = "/";
	
	public final String creator;
	public final String title;
	
	public Reference(String full_name) throws IllegalArgumentException {
		int sep_index = full_name.indexOf(SEPARATOR);
		if(sep_index == -1) {
			creator = null;
			title = full_name;
		} else {
			creator = full_name.substring(0, sep_index);
			title = full_name.substring(sep_index + 1);
		}
		check();
	}
	
	public Reference(String _creator, String _title) throws IllegalArgumentException {
		creator = _creator;
		title = _title;
		check();
	}

	private void check() throws IllegalArgumentException {
		if(title == null) {
			throw new IllegalArgumentException("Title of reference cannot be null.");
		}
	}
	
	public String toString() {
		if(creator != null) {
			return creator + SEPARATOR + title;
		} else {
			return title;
		}
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
	
	public static Reference blank() {
		return new Reference("", "");
	}
}
