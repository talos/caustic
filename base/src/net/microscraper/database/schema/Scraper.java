package net.microscraper.database.schema;

import java.util.Hashtable;
import java.util.Vector;

import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.client.Utils;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;

public class Scraper extends AbstractResource {
	
	public String getName() {
		return ref().title;
	}
	
	private final Hashtable results_hash = new Hashtable();
	private final Hashtable retry_web_pages_for_caller = new Hashtable();
	private final Hashtable retry_source_scrapers_for_caller = new Hashtable();
	public Result[] getResults(AbstractResult caller) throws FatalExecutionException {
		Vector results;
		Vector retry_web_pages;
		Vector retry_source_scrapers;
		
		if(results_hash.containsKey(caller)) {
			results = (Vector) results_hash.get(caller);
			retry_web_pages = (Vector) retry_web_pages_for_caller.get(caller);
			retry_source_scrapers = (Vector) retry_source_scrapers_for_caller.get(caller);
			
			Vector input_strings = new Vector();
			for(int i = 0; i < retry_web_pages.size(); i ++) {
				WebPage web_page = (WebPage) retry_web_pages.elementAt(i);
				try {
					input_strings.addElement(web_page.getSuccess(caller).value);
					retry_web_pages.removeElementAt(i);
					i--;
				} catch(MissingVariable e) {
					retry_web_pages.addElement(web_page);
				} catch(Exception e) {
					throw new FatalExecutionException(e);
				}
			}
			
			for(int i = 0; i < retry_source_scrapers.size(); i ++) {
				Scraper scraper = (Scraper) retry_source_scrapers.elementAt(i);
				Result[] source_results = scraper.getResults(caller);
				boolean all_successful = true;
				String[] this_scrapers_input_strings = new String[source_results.length];
				for(int j = 0 ; j < source_results.length ; j++) {
					/*if(source_results[j].successful) {
						input_strings.addElement(((Result.Success) source_results[j]).value);
					} else {
						// TODO: only retry the individual string that failed?
						retry_source_scrapers.addElement(scraper);
					}*/
					this_scrapers_input_strings[j] = (((Result.Success) source_results[j]).value);
					if(!source_results[j].successful) {
						all_successful = false;
					}
				}
				if(all_successful) {
					Utils.arrayIntoVector(this_scrapers_input_strings, input_strings);
				}
			}
			
			String[] input_strings_ary = new String[input_strings.size()];
			input_strings.copyInto(input_strings_ary);
			
			Utils.arrayIntoVector(processStrings(caller, input_strings_ary), results);

		} else {
			results = new Vector();
			retry_web_pages = new Vector();
			retry_source_scrapers = new Vector();
			
			AbstractResource[] web_pages;
			AbstractResource[] source_scrapers;
			try {
				web_pages = relationship(WEB_PAGES);
				source_scrapers = relationship(SOURCE_SCRAPERS);
			} catch(ResourceNotFoundException e) {
				throw new FatalExecutionException(e);
			}
			
			Vector input_strings = new Vector();
			for(int i = 0; i < web_pages.length; i++) {
				WebPage web_page = (WebPage) web_pages[i];
				try {
					input_strings.addElement(web_page.getSuccess(caller).value);
				} catch(MissingVariable e) {
					retry_web_pages.addElement(web_page);
				} catch(Exception e) {
					throw new FatalExecutionException(e);
				}
			}
			
			for(int i = 0; i < source_scrapers.length; i++) {
				Scraper scraper = (Scraper) source_scrapers[i];
				Result[] source_results = scraper.getResults(caller);
				for(int j = 0 ; j < source_results.length ; j++) {
					if(source_results[i].successful) {
						input_strings.addElement(((Result.Success) source_results[j]).value);					
					} else {
						// TODO: only retry the individual string that failed?
						retry_source_scrapers.addElement(scraper);
					}
				}
			}
			
			String[] input_strings_ary = new String[input_strings.size()];
			input_strings.copyInto(input_strings_ary);
			
			Utils.arrayIntoVector(processStrings(caller, input_strings_ary), results);
			
			results_hash.put(caller, results);
			retry_web_pages_for_caller.put(caller, retry_web_pages);
			retry_source_scrapers_for_caller.put(caller, retry_source_scrapers);
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
	}
	
	private final Result[] processStrings(AbstractResult caller, String[] input_strings) throws FatalExecutionException {
		try {
			Integer match_number;
			try {
				match_number = new Integer(attribute_get(MATCH_NUMBER));
			} catch(NumberFormatException e) {
				match_number = null;
			}
			Result pattern_result = new Regexp(attribute_get(REGEXP)).getSuccess(caller);
			Pattern pattern = Client.context().regexp.compile(((Result.Success) pattern_result).value);
			Vector matches = new Vector();
			for(int i = 0 ; i < input_strings.length ; i ++) {
				String input = (String) input_strings[i];
				String[] matches_ary;
				if(match_number == null) {
					matches_ary = pattern.allMatches(input);
				} else {
					matches_ary = new String[] { pattern.match(input, match_number.intValue()) };
				}
				Utils.arrayIntoVector(matches_ary, matches);
			}
			Result.Success[] output = new Result.Success[matches.size()];
			
			for(int i = 0 ; i < matches.size() ; i ++) {
				output[i] = new Result.Success(caller, this, this.ref().title, (String) matches.elementAt(i));
			}
			return output;
		} catch(MissingVariable e) {
			return new Result[] { new Result.Premature(caller, this, e) };			
		} catch(NoMatches e) {
			return new Result[] { new Result.Failure(caller, this, e) };
		} catch(InterruptedException e) {
			throw new FatalExecutionException(e);
		} catch(ResourceNotFoundException e) {
			throw new FatalExecutionException(e);			
		} catch(TemplateException e) {
			throw new FatalExecutionException(e);
		}
	}
	
	public boolean isVariable() {
		return true;
	}
	protected boolean branchesResults() throws FatalExecutionException {
		try {
			if(attribute_get(MATCH_NUMBER) == null || relationship(WEB_PAGES).length + relationship(SOURCE_SCRAPERS).length > 1)
				return true;
			return false;
		} catch(ResourceNotFoundException e) {
			throw new FatalExecutionException(e);
		}
	}
	private static final String REGEXP = "regexp";
	private static final String MATCH_NUMBER = "match_number";
	private static final RelationshipDefinition WEB_PAGES =
		new RelationshipDefinition( "web_pages", WebPage.class );
	private static final RelationshipDefinition SOURCE_SCRAPERS =
		new RelationshipDefinition( "source_scrapers", Scraper.class);
	
	public ModelDefinition definition() {
		return new ModelDefinition() {
			public String[] attributes() {
				return new String[] { REGEXP, MATCH_NUMBER };
			}
			public RelationshipDefinition[] relationships() {
				return new RelationshipDefinition[] {
					WEB_PAGES, SOURCE_SCRAPERS
				};
			}
		};
	}

}