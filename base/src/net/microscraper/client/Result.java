package net.microscraper.client;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.ResultSet.Variables;
import net.microscraper.database.Reference;
import net.microscraper.database.schema.Scraper;

public class Result {
	private final Reference scraper_ref;
	private final boolean branch;
	private final Result source;
	private final String value;
	
	private final Vector children = new Vector();

	private Result(Result _source, Scraper _scraper, String _value) {
		source = _source;
		source.addChild(this);
		scraper_ref = _scraper.ref;
		branch = _scraper.branch;
		value = _value;
	}
	protected void addChild(Result result) {
		children.addElement(result);
	}
	public Result[] children() {
		Result[] children_ary = new Result[children.size()];
		children.copyInto(children_ary);
		return children_ary;
	}
	
	public boolean isRoot() {
		if(source == null)
			return true;
		if(source.branch == true)
			return true;
		return false;
	}
	
	/**
	 * Find the root Result of this Result.  Returns the branch itself if no branches intervene
	 * between this Result's value and the initial scraper execution.
	 * @return
	 */
	public Result root() {
		Result test = this;
		while(test.isRoot() == false) {
			test = test.source;
		}
		return test;
	}
	
	/**
	 * Obtain variables specific to this branch of results.  Searches to the deepest non-branch, then pulls
	 * values from every non-branch descendant.
	 * @return
	 */
	public Variables variables() {
		Result root = root();
		Variables variables = new Variables();
		for(int i = 0; i < root.children().length; i++) {
			
		}
		return variables;
	}
}