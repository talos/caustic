package net.microscraper.database.schema;

import java.util.Vector;

import net.microscraper.client.Browser.BrowserException;
import net.microscraper.client.Client;
import net.microscraper.client.Interfaces.Regexp.NoMatches;
import net.microscraper.client.Interfaces.Regexp.Pattern;
import net.microscraper.client.Mustache.MissingVariable;
import net.microscraper.client.Mustache.TemplateException;
import net.microscraper.database.AbstractResource;
import net.microscraper.database.AbstractResult;
import net.microscraper.database.DatabaseException.ResourceNotFoundException;
import net.microscraper.database.ModelDefinition;
import net.microscraper.database.RelationshipDefinition;
import net.microscraper.database.Result;
import net.microscraper.database.Result.Premature;

public class Scraper extends AbstractResource {
	public Result[] execute(AbstractResult caller)
					throws TemplateException, ResourceNotFoundException, InterruptedException {
		AbstractResource[] web_pages = relationship(WEB_PAGES);
		AbstractResource[] source_scrapers = relationship(SOURCE_SCRAPERS);
		Vector input_strings = new Vector();
		for(int i = 0; i < web_pages.length; i++) {
			try {
				Result web_page_result = web_pages[i].getValue(caller)[0];
				//input_strings.addElement(.value;
			} catch(MissingVariable e) {
				
			}
		}
		for(int i = 0; i < source_scrapers.length; i++) {
			try {
				Result[] source_results = source_scrapers[i].getValue(caller);
				for(int j = 0 ; j < source_results.length ; j++) {
					input_strings.addElement(source_results[j].value);
				}
			} catch (MissingVariable e) {
				// Missing a variable, skip this Scraper
				Client.context().log.i(e.getMessage());
			}
		}
		return processInput(input_strings, caller);
	}
	
	private Result[] processInput(Vector input_strings, AbstractResult caller) throws TemplateException, MissingVariable {
		Integer match_number;
		try {
			match_number = new Integer(attribute_get(MATCH_NUMBER));
		} catch(NumberFormatException e) {
			match_number = null;
		}
		Pattern pattern = Client.context().regexp.compile(
				new Regexp(attribute_get(REGEXP)).execute(caller)[0].value);
		Vector results = new Vector();
		for(int i = 0 ; i < input_strings.size() ; i ++) {
			String input = (String) input_strings.elementAt(i);
			try {
				String[] matches;
				if(match_number == null) {
					matches = pattern.allMatches(input);
				} else {
					matches = new String[] { pattern.match(input, match_number.intValue()) };
				}
				for(int j = 0 ; j < matches.length ; j ++) {
					results.addElement(new Result.Success(caller, this, this.ref().title, matches[j]));
				}
			} catch(NoMatches e) {
				results.addElement(new Result.Failure(caller, this, e));
			}
		}
		Result[] results_ary = new Result[results.size()];
		results.copyInto(results_ary);
		return results_ary;
	}
	public boolean isVariable() {
		return true;
	}
	public boolean branchesResults() {
		if(attribute_get(MATCH_NUMBER) == null) 
			return true;
		return false;
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