package net.microscraper.database.schema;

import java.util.Vector;

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

public class Scraper extends AbstractResource {
	
	public String getName() {
		return ref().title;
	}
	
	public Result[] getResults(AbstractResult caller)
			throws FatalExecutionException {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected Result.Success execute(AbstractResult caller, AbstractResource source)
			throws TemplateException, ResourceNotFoundException, InterruptedException, MissingVariable, NoMatches {
		Result[] source_results = source.getValues(caller);
		for(int i = 0 ; i < source_results.length ; i ++) {
			if(source_results[i].successful) {
				String source_string = ((Result.Success) source_results[i]).value;
				
				// Match against all if match_number is null.
				Integer match_number;
				try {
					match_number = new Integer(attribute_get(MATCH_NUMBER));
				} catch(NumberFormatException e) {
					match_number = null;
				}
				Result pattern_result = new Regexp(attribute_get(REGEXP)).execute(caller)[0];
				if(pattern_result.successful) {
					Pattern pattern = Client.context().regexp.compile(((Result.Success) pattern_result).value);
					Vector results = new Vector();
					for(int i = 0 ; i < input_strings.size() ; i ++) {
						String input = (String) input_strings.elementAt(i);
						String[] matches;
						if(match_number == null) {
							matches = pattern.allMatches(input);
						} else {
							matches = new String[] { pattern.match(input, match_number.intValue()) };
						}
						for(int j = 0 ; j < matches.length ; j ++) {
							results.addElement(new Result.Success(caller, this, this.ref().title, matches[j]));
						}
					}
					Result[] results_ary = new Result[results.size()];
					results.copyInto(results_ary);
					return results_ary;
				} else {
					throw new MissingVariable(((Result.Premature) pattern_result));
				}
			}
		}
		
		AbstractResource[] web_pages = relationship(WEB_PAGES);
		AbstractResource[] source_scrapers = relationship(SOURCE_SCRAPERS);
		Vector input_strings = new Vector();
		//Vector premature_results = new Vector();
		for(int i = 0; i < web_pages.length; i++) {
			Result r = web_pages[i].getValue(caller)[0];
			if(r.successful) {
				input_strings.addElement(((Result.Success) r).value);
			}/* else {
				premature_results.addElement(
						new Result.Premature(caller, this, new MissingVariable((Result.Premature) r)));
			}*/
		}
		for(int i = 0; i < source_scrapers.length; i++) {
			Result[] source_results = source_scrapers[i].getValue(caller);
			for(int j = 0 ; j < source_results.length ; j++) {
				if(source_results[i].successful) {
					input_strings.addElement(((Result.Success) source_results[j]).value);					
				}/* else {
					premature_results.addElement(
							new Result.Premature(caller, this, new MissingVariable((Result.Premature) source_results[i])));
				}*/
			}
		}
		
		Result[] results;
		try {
			results = processInput(input_strings, caller);
		} catch(MissingVariable e) {
			results = new Result[] { new Result.Premature(caller, this, e) };
		} catch(NoMatches e) {
			results = new Result[] { new Result.Failure(caller, this, e) };
		}
		//Utils.arrayIntoVector(processed_results, premature_results);
		//Result[] results = new Result[premature_results.size()];
		//premature_results.copyInto(results);
		return results;
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