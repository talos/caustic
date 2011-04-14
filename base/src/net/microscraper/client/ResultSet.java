package net.microscraper.client;

import java.util.Vector;

import net.microscraper.client.Publisher.PublisherException;
import net.microscraper.client.Variables;
import net.microscraper.database.Reference;

public class ResultSet {
	private final Publisher publisher;
	private final Vector children = new Vector();
	private int count = 0;

	public ResultSet(Publisher _publisher) {
		publisher = _publisher;
	}
	
	public void addOneToOne(Reference scraper_ref, String value) {
		Result child = new Result(this, scraper_ref, value, false);
		children.addElement(child);
		tryPublish(child);
	}
	public void addOneToMany(Reference scraper_ref, String[] values) {
		for(int i = 0; i < values.length; i ++) {
			Result child = new Result(this, scraper_ref, values[i], true);
			children.addElement(child);
			tryPublish(child);
		}
	}
	
	private void tryPublish(Result result) {
		try {
			publisher.publish(result);
		} catch (PublisherException e) {
			Client.context().log.e(e);
		} 
	}
	
	public Result[] children() {
		Result[] children_ary = new Result[children.size()];
		children.copyInto(children_ary);
		return children_ary;
	}
	
	public Result[] oneToOneDescendents() {
		Result[] children = children();
		Vector oneToOneDescendents = new Vector();
		for(int i = 0 ; i < children.length ; i ++) {
			if(!children[i].isOneToMany()) {
				oneToOneDescendents.addElement(children[i]);
				Utils.arrayIntoVector(children[i].oneToOneDescendents(), oneToOneDescendents);
			}
		}
		Result[] oneToOneDescendents_ary = new Result[oneToOneDescendents.size()];
		oneToOneDescendents.copyInto(oneToOneDescendents_ary);
		return oneToOneDescendents_ary;
	}
	
	public Result[] descendents() {
		Result[] children = children();
		Vector descendents = new Vector();
		for(int i = 0 ; i < children.length ; i ++) {
			descendents.addElement(children[i]);
			Utils.arrayIntoVector(children[i].descendents(), descendents);
		}
		Result[] oneToOneDescendents_ary = new Result[descendents.size()];
		descendents.copyInto(oneToOneDescendents_ary);
		return oneToOneDescendents_ary;
	}
	
	public int size() {
		int size = 1;
		Result[] children = children();
		for(int i = 0; i < children.length; i++) {
			size += children[i].size();
		}
		return size;
	}
	public Variables variables() {
		Variables variables = new Variables();
		Result[] desc = oneToOneDescendents();
		for(int i = 0; i < desc.length; i++) {
			variables.put(desc[i].ref, desc[i].value);
			//variables.merge(desc[i].value);
		}
		return variables;
	}
	
	/*
	 * Are there any results for scraping a ref within this scope?
	 */
	public boolean contains(Reference ref) {
		Result[] descendents = descendents();
		for(int i = 0; i < descendents.length; i++) {
			if(descendents[i].ref.equals(ref))
				return true;
		}
		return false;
	}
	
	/*
	 * Get all the string results scraping a particular ref within this scope.
	 */
	public String[] get(Reference ref) {
		/*Vector results = new Vector();
		Result[] descendents = descendents();
		for(int i = 0; i < descendents.length; i++) {
			if(descendents[i].ref.equals(ref))
				results.addElement(descendents[i]);
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;*/
		Vector results = new Vector();
		Result[] descendents = descendents();
		for(int i = 0; i < descendents.length; i++) {
			if(descendents[i].ref.equals(ref))
				results.addElement(descendents[i].value);
		}
		String[] result_values_ary = new String[results.size()];
		results.copyInto(result_values_ary);
		return result_values_ary;
	}
	
	public boolean isOneToMany() {
		return true;
	}
	public int num() {
		return 0;
	}
	/*
	public class Result extends ResultSet {
		public final Reference ref;
		private final boolean one_to_many;
		public final ResultSet caller;
		private final int num;
		public final String value;
		
		
		private Result(ResultSet _caller, Reference _scraper_ref, String _value, boolean _one_to_many) {
			super(publisher);
			
			count = count + 1;
			
			num = count;
			caller = _caller;
			
			ref = _scraper_ref;
			one_to_many = _one_to_many;
			value = _value;
		}
		
		public boolean isOneToMany() {
			return one_to_many;
		}
		public int num() {
			return num;
		}
	}
	*/
}