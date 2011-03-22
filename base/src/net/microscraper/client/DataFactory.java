package net.microscraper.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Hashtable;

import net.microscraper.client.interfaces.JSON.Iterator;
import net.microscraper.client.interfaces.JSON.JSONInterfaceException;
import net.microscraper.client.interfaces.JSON.Object;
import net.microscraper.client.interfaces.JSON.Tokener;
import net.microscraper.database.schema.Cookie;
import net.microscraper.database.schema.Data;
import net.microscraper.database.schema.Default;
import net.microscraper.database.schema.Header;
import net.microscraper.database.schema.Post;
import net.microscraper.database.schema.Regexp;
import net.microscraper.database.schema.Scraper;
import net.microscraper.database.schema.WebPage;

/**
 * @author john
 *
 */
public interface DataFactory {
	public abstract Data get(String creator, String name) throws Exception;
	
	public static class JSON implements DataFactory {
		
		private final Hashtable datas = new Hashtable();
		private final Hashtable posts = new Hashtable();
		private final Hashtable cookies = new Hashtable();
		private final Hashtable headers = new Hashtable();
		private final Hashtable regexps = new Hashtable();
		private final Hashtable web_pages = new Hashtable();
		private final Hashtable defaults = new Hashtable();
		
		private final String url;
		private final Browser browser;
		private final net.microscraper.client.interfaces.JSON j;
		
		public JSON(String _url, Browser _browser,
				net.microscraper.client.interfaces.JSON _json_interface) {
			url = _url;
			browser = _browser;
			j = _json_interface;
		}
		
		public Data get(String creator, String title)
				throws IOException, InterruptedException, JSONInterfaceException {
			String full_name = Data.fullName(creator, title);
			if(!datas.containsKey(full_name)) {
				String creator_enc = URLEncoder.encode(creator, "UTF-8");
				String title_enc = URLEncoder.encode(title, "UTF-8");
				
				String location = url + '/' + Data.fullName(creator_enc, title_enc) + "?format=json";
				String response = browser.load(new WebPage(location));
				
				inflate(j.getTokener(response).nextValue());
			}
			return (Data) datas.get(full_name);
		}
		
		public void inflate(Object json_obj) throws JSONInterfaceException {
			Iterator i;
			Object raw;
			
			if(json_obj.has(Post.RESOURCE)) {
				raw = json_obj.getJSONObject(Post.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object post = raw.getJSONObject(title);
					posts.put(title, new Post(post.getString(Post.NAME), post.getString(Post.VALUE)));
				}
			}
			
			if(json_obj.has(Cookie.RESOURCE)) {
				raw = json_obj.getJSONObject(Cookie.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object cookie = raw.getJSONObject(title);
					cookies.put(title, new Cookie(cookie.getString(Cookie.NAME), cookie.getString(Cookie.VALUE)));
				}
			}
			
			if(json_obj.has(Header.RESOURCE)) {
				raw = json_obj.getJSONObject(Header.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object header = raw.getJSONObject(title);
					headers.put(title, new Header(header.getString(Header.NAME), header.getString(Header.VALUE)));
				}
			}
			
			if(json_obj.has(Regexp.RESOURCE)) {
				raw = json_obj.getJSONObject(Regexp.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object regexp = raw.getJSONObject(title);
					headers.put(title, new Regexp(regexp.getString(Regexp.REGEXP)));
				}
			}
			
			if(json_obj.has(WebPage.RESOURCE)) {
				raw = json_obj.getJSONObject(WebPage.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object web_page = raw.getJSONObject(title);
					String[] post_titles = web_page.getJSONArray(WebPage.POSTS).toArray();
					String[] header_titles = web_page.getJSONArray(WebPage.HEADERS).toArray();
					String[] cookie_titles = web_page.getJSONArray(WebPage.COOKIES).toArray();
					String[] regexp_titles = web_page.getJSONArray(WebPage.TERMINATES).toArray();
					Post[] web_page_posts = new Post[post_titles.length];
					Header[] web_page_headers = new Header[header_titles.length];
					Cookie[] web_page_cookies = new Cookie[cookie_titles.length];
					Regexp[] web_page_regexps = new Regexp[regexp_titles.length];
					Utils.selectHashtableValuesIntoArray(posts, post_titles, web_page_posts);
					Utils.selectHashtableValuesIntoArray(headers, header_titles, web_page_headers);
					Utils.selectHashtableValuesIntoArray(cookies, cookie_titles, web_page_cookies);
					Utils.selectHashtableValuesIntoArray(regexps, regexp_titles, web_page_regexps);
					
					web_pages.put(title, new WebPage(
						web_page.getString(WebPage.URL),
						web_page_posts, web_page_headers, web_page_cookies, web_page_regexps)
					);
				}
			}
			
			Hashtable scrapers = new Hashtable();
			if(json_obj.has(Scraper.RESOURCE)) {
				raw = json_obj.getJSONObject(Scraper.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object scraper = raw.getJSONObject(title);
					String[] web_page_titles = scraper.getJSONArray(Scraper.WEB_PAGES).toArray();
					String[] source_scraper_titles = scraper.getJSONArray(Scraper.SOURCE_SCRAPERS).toArray();
					WebPage[] scraper_web_pages = new WebPage[web_page_titles.length];
					Scraper[] source_scrapers = new Scraper[source_scraper_titles.length];
					Utils.selectHashtableValuesIntoArray(web_pages, web_page_titles, scraper_web_pages);
					Utils.selectHashtableValuesIntoArray(scrapers, source_scraper_titles, source_scrapers);
					
					scrapers.put(title, new Scraper(
							(Regexp) regexps.get(scraper.getString(Scraper.REGEXP)),
							Integer.parseInt(scraper.getString(Scraper.MATCH_NUMBER)),
							scraper_web_pages, source_scrapers)
					);
				}
			}
			
			if(json_obj.has(Default.RESOURCE)) {
				raw = json_obj.getJSONObject(Default.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object _default = raw.getJSONObject(title);
					
					defaults.put(title, new Default(_default.getString(Default.VALUE)));
				}
			}
			
			if(json_obj.has(Data.RESOURCE)) {
				raw = json_obj.getJSONObject(Data.RESOURCE);
				i = raw.keys();
				while(i.hasNext()) {
					String title = (String) i.next();
					Object data = raw.getJSONObject(title);
					String[] data_default_titles = data.getJSONArray(Data.DEFAULTS).toArray();
					String[] data_scraper_titles = data.getJSONArray(Data.SCRAPERS).toArray();
					Default[] data_defaults = new Default[data_default_titles.length];
					Scraper[] data_scrapers = new Scraper[data_scraper_titles.length];
					Utils.selectHashtableValuesIntoArray(defaults, data_default_titles, data_defaults);
					Utils.selectHashtableValuesIntoArray(scrapers, data_scraper_titles, data_scrapers);
					
					datas.put(title, new Data(data_scrapers, data_defaults));
				}
			}
		}
	}
}
