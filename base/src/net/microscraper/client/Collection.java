package net.microscraper.client;

import java.util.Hashtable;

import net.microscraper.client.interfaces.Interfaces.JSON;
import net.microscraper.client.interfaces.Interfaces.JSON.JSONInterfaceException;
import net.microscraper.database.schema.Cookie;
import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.Header;
import net.microscraper.database.schema.Post;
import net.microscraper.database.schema.Reference;
import net.microscraper.database.schema.Regexp;
import net.microscraper.database.schema.Scraper;
import net.microscraper.database.schema.WebPage;

public class Collection {

	public final Hashtable datas;
	public final Hashtable posts;
	public final Hashtable cookies;
	public final Hashtable headers;
	public final Hashtable regexps;
	public final Hashtable web_pages;
	public final Hashtable scrapers;
	public final Hashtable defaults;
	
	public Collection(Hashtable _datas, Hashtable _posts, Hashtable _cookies,
			Hashtable _headers, Hashtable _regexps, Hashtable _web_pages,
			Hashtable _scrapers, Hashtable _defaults) {
		datas = _datas;
		posts = _posts;
		cookies = _cookies;
		headers = _headers;
		regexps = _regexps;
		web_pages = _web_pages;
		scrapers = _scrapers;
		defaults = _defaults;
	}
	
	public Collection inflateJSON(JSON.Object json_obj) throws JSONInterfaceException {
		JSON.Iterator i;
		JSON.Object raw;
		
		Hashtable posts = new Hashtable();
		if(json_obj.has(Post.RESOURCE)) {
			raw = json_obj.getJSONObject(Post.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object post = raw.getJSONObject(ref);
				posts.put(new Reference(ref), new Post(post.getString(Post.NAME), post.getString(Post.VALUE)));
			}
		}
		
		Hashtable cookies = new Hashtable();
		if(json_obj.has(Cookie.RESOURCE)) {
			raw = json_obj.getJSONObject(Cookie.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object cookie = raw.getJSONObject(ref);
				cookies.put(new Reference(ref), new Cookie(cookie.getString(Cookie.NAME), cookie.getString(Cookie.VALUE)));
			}
		}
		
		Hashtable headers = new Hashtable();
		if(json_obj.has(Header.RESOURCE)) {
			raw = json_obj.getJSONObject(Header.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object header = raw.getJSONObject(ref);
				headers.put(new Reference(ref), new Header(header.getString(Header.NAME), header.getString(Header.VALUE)));
			}
		}

		Hashtable regexps = new Hashtable();
		if(json_obj.has(Regexp.RESOURCE)) {
			raw = json_obj.getJSONObject(Regexp.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object regexp = raw.getJSONObject(ref);
				regexps.put(new Reference(ref), new Regexp(regexp.getString(Regexp.REGEXP)));
			}
		}
		
		Hashtable web_pages = new Hashtable();
		if(json_obj.has(WebPage.RESOURCE)) {
			raw = json_obj.getJSONObject(WebPage.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object web_page = raw.getJSONObject(ref);

				web_pages.put(new Reference(ref), new WebPage(
					web_page.getString(WebPage.URL),
					Reference.fromArray(web_page.getJSONArray(WebPage.POSTS).toArray()),
					Reference.fromArray(web_page.getJSONArray(WebPage.HEADERS).toArray()),
					Reference.fromArray(web_page.getJSONArray(WebPage.COOKIES).toArray()),
					Reference.fromArray(web_page.getJSONArray(WebPage.TERMINATES).toArray())
				));
			}
		}
		
		Hashtable scrapers = new Hashtable();
		if(json_obj.has(Scraper.RESOURCE)) {
			raw = json_obj.getJSONObject(Scraper.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object scraper = raw.getJSONObject(ref);
				
				scrapers.put(new Reference(ref), new Scraper(
					scraper.getString(Scraper.REGEXP),
					Integer.parseInt(scraper.getString(Scraper.MATCH_NUMBER)),
					Reference.fromArray(scraper.getJSONArray(Scraper.WEB_PAGES).toArray()),
					Reference.fromArray(scraper.getJSONArray(Scraper.SOURCE_SCRAPERS).toArray())
				));
			}
		}
		
		Hashtable defaults = new Hashtable();
		if(json_obj.has(Default.RESOURCE)) {
			raw = json_obj.getJSONObject(Default.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object _default = raw.getJSONObject(ref);
				
				defaults.put(new Reference(ref), new Default(
					_default.getString(Default.VALUE),
					Reference.fromArray(_default.getJSONArray(Default.SUBSTITUTES_FOR).toArray())
				));
			}
		}
		
		Hashtable datas = new Hashtable();
		if(json_obj.has(Data.RESOURCE)) {
			raw = json_obj.getJSONObject(Data.RESOURCE);
			i = raw.keys();
			while(i.hasNext()) {
				String ref = (String) i.next();
				JSON.Object data = raw.getJSONObject(ref);
				String[] data_default_titles = data.getJSONArray(Data.DEFAULTS).toArray();
				String[] data_scraper_titles = data.getJSONArray(Data.SCRAPERS).toArray();
				Default[] data_defaults = new Default[data_default_titles.length];
				Scraper[] data_scrapers = new Scraper[data_scraper_titles.length];
				Utils.selectHashtableValuesIntoArray(defaults, data_default_titles, data_defaults);
				Utils.selectHashtableValuesIntoArray(scrapers, data_scraper_titles, data_scrapers);
				
				datas.put(new Reference(ref), new Data(
					Reference.fromArray(data.getJSONArray(Data.SCRAPERS).toArray()),
					Reference.fromArray(data.getJSONArray(Data.DEFAULTS).toArray())
				));
			}
		}
		
		return new Collection(datas, posts, cookies, headers, regexps,
				web_pages, scrapers, defaults);
	}
}
