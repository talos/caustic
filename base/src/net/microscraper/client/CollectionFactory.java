package net.microscraper.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;

import net.microscraper.client.interfaces.JSON.Iterator;
import net.microscraper.client.interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.interfaces.JSON.Object;
import net.microscraper.database.schema.Cookie;
import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.Header;
import net.microscraper.database.schema.Post;
import net.microscraper.database.schema.Reference;
import net.microscraper.database.schema.Regexp;
import net.microscraper.database.schema.Scraper;
import net.microscraper.database.schema.WebPage;

/**
 * @author john
 *
 */
public interface CollectionFactory {
	public abstract Collection get(String location) throws Exception;
	
	public static class JSON implements CollectionFactory {
		
		private final Browser browser;
		private final net.microscraper.client.interfaces.JSON j;
		private final Hashtable cache = new Hashtable();
		
		public JSON(Browser _browser,
				net.microscraper.client.interfaces.JSON _json_interface) {
			browser = _browser;
			j = _json_interface;
		}
		
		public Collection get(String location)
				throws IOException, InterruptedException, JSONInterfaceException {
			if(!cache.containsKey(location)) {
				//TODO: is cache necessary here? Browser could manage this, too.
				//String creator_enc = URLEncoder.encode(creator, "UTF-8");
				//String title_enc = URLEncoder.encode(title, "UTF-8");
				
				String response = browser.load(new WebPage(location));
				
				cache.put(location, inflate(j.getTokener(response).nextValue()));
			}
			return (Collection) cache.get(location);
		}
		
		public Collection inflate(Object json_obj) throws JSONInterfaceException {
			Iterator i;
			Object raw;
			
			Hashtable posts = new Hashtable();
			if(json_obj.has(Post.RESOURCE)) {
				raw = json_obj.getJSONObject(Post.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String ref = (String) i.next();
					Object post = raw.getJSONObject(ref);
					posts.put(new Reference(ref), new Post(post.getString(Post.NAME), post.getString(Post.VALUE)));
				}
			}
			
			Hashtable cookies = new Hashtable();
			if(json_obj.has(Cookie.RESOURCE)) {
				raw = json_obj.getJSONObject(Cookie.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String ref = (String) i.next();
					Object cookie = raw.getJSONObject(ref);
					cookies.put(new Reference(ref), new Cookie(cookie.getString(Cookie.NAME), cookie.getString(Cookie.VALUE)));
				}
			}
			
			Hashtable headers = new Hashtable();
			if(json_obj.has(Header.RESOURCE)) {
				raw = json_obj.getJSONObject(Header.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String ref = (String) i.next();
					Object header = raw.getJSONObject(ref);
					headers.put(new Reference(ref), new Header(header.getString(Header.NAME), header.getString(Header.VALUE)));
				}
			}

			Hashtable regexps = new Hashtable();
			if(json_obj.has(Regexp.RESOURCE)) {
				raw = json_obj.getJSONObject(Regexp.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String ref = (String) i.next();
					Object regexp = raw.getJSONObject(ref);
					regexps.put(new Reference(ref), new Regexp(regexp.getString(Regexp.REGEXP)));
				}
			}
			
			Hashtable web_pages = new Hashtable();
			if(json_obj.has(WebPage.RESOURCE)) {
				raw = json_obj.getJSONObject(WebPage.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String ref = (String) i.next();
					Object web_page = raw.getJSONObject(ref);

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
					Object scraper = raw.getJSONObject(ref);
					
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
					Object _default = raw.getJSONObject(ref);
					
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
					Object data = raw.getJSONObject(ref);
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
			
			return new Collection(datas, posts, cookies, headers, regexps, web_pages, defaults);
		}
	}
}
