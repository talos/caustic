package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.interfaces.json.JSONInterfaceObject;

import org.junit.Test;

public class PageTest {

	@Mocked JSONInterfaceObject obj;
	@Tested Page page;
	
	@Test
	public void testGetMethodDefault() throws Exception {
		page = new Page(obj);
		assertEquals(Page.DEFAULT_METHOD, page.getMethod());
	}
	
	@Test
	public void testGetMethodGet() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Page.METHOD); result = "get";
			obj.has(Page.METHOD); result = true;
		}};
		page = new Page(obj);
		assertEquals(Page.Method.GET, page.getMethod());
	}

	@Test
	public void testGetMethodPost() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Page.METHOD); result = "post";
			obj.has(Page.METHOD); result = true;
		}};
		page = new Page(obj);
		assertEquals(Page.Method.POST, page.getMethod());
	}
	
	@Test
	public void testGetMethodHead() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Page.METHOD); result = "head";
			obj.has(Page.METHOD); result = true;
		}};
		page = new Page(obj);
		assertEquals(Page.Method.HEAD, page.getMethod());
	}
	

	@Test(expected=DeserializationException.class)
	public void testGetMethodInvalid() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Page.METHOD); result = "not a method";
			obj.has(Page.METHOD); result = true;
		}};
		page = new Page(obj);
	}
	
	@Test
	public void testGetCookiesDefault() throws Exception {
		page = new Page(obj);
		assertArrayEquals(Page.DEFAULT_COOKIES, page.getCookies());
	}

	@Test
	public void testGetHeadersDefault() throws Exception {
		page = new Page(obj);
		assertArrayEquals(Page.DEFAULT_HEADERS, page.getHeaders());
	}

	@Test
	public void testGetPreloadDefault() throws Exception {
		page = new Page(obj);
		assertArrayEquals(Page.DEFAULT_PRELOAD, page.getPreload());
	}

	@Test
	public void testGetStopBecauseDefault() throws Exception {
		page = new Page(obj);
		assertArrayEquals(Page.DEFAULT_STOP_BECAUSE, page.getStopBecause());
	}

	@Test
	public void testGetPostsDefault() throws Exception {
		page = new Page(obj);
		assertArrayEquals(Page.DEFAULT_POSTS, page.getPosts());
	}

}
