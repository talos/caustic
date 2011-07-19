package net.microscraper.client.impl;

import static org.junit.Assert.*;

import net.microscraper.client.Log;
import net.microscraper.client.interfaces.NetInterface;
import net.microscraper.client.interfaces.URIInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link JavaNetURI}.
 * @author realest
 *
 */
//TODO: This is a test of {@link URIInterface}, should be handled
// as such instead of as a test of {@link JavaNetURI}.
public class JavaNetURITest {

	/**
	 * Test {@link NetInterface}, created/destroyed in {@link #setUp} and
	 * {@link #tearDown}.
	 */
	//private NetInterface netInterface;
	
	// Text fixtures
	private URIInterface absFixture;
	private URIInterface relFixture;
	private URIInterface jsonFixture;
	
	@Before
	public void setUp() throws Exception {
		NetInterface netInterface = new JavaNetInterface(
				new JavaNetBrowser(
						new Log(), 10000));
		absFixture = netInterface.makeURI(
				"scheme:/domain/dir1/dir2/file.ext#frag");
		relFixture = netInterface.makeURI(
				"../../path/to/file.html");
		jsonFixture = netInterface.makeURI(
				"scheme:/domain/dir1/dir2/file.json#/frag");
	}

	@After
	public void tearDown() throws Exception {
		absFixture = null;
		relFixture = null;
		jsonFixture =null;
	}

	@Test
	public void testIsAbsolute() {
		assertTrue(absFixture.isAbsolute());
		assertFalse(relFixture.isAbsolute());
		assertTrue(absFixture.isAbsolute());
	}

	@Test
	public void testResolveStringAbsolute() {
		assertEquals("scheme:/domain/dir1/dir2/page.html",
				absFixture.resolve("page.html").toString());
	}
	
	@Test
	public void testResolveStringRelative() {
		assertEquals("../../path/",
				relFixture.resolve("../").toString());
	}

	@Test
	public void testResolveURIInterface() {
		assertEquals("scheme:/domain/path/to/file.html",
				absFixture.resolve(relFixture).toString());
	}

	@Test
	public void testGetSchemeAbsolute() {
		assertEquals("scheme", absFixture.getScheme());
	}
	
	@Test
	public void testGetSchemeRelative() {
		assertNull(relFixture.getScheme());
	}

	@Test
	public void testGetSchemeSpecificPartAbsolute() {
		assertEquals("/domain/dir1/dir2/file.ext",
				absFixture.getSchemeSpecificPart());
	}
	
	@Test
	public void testGetSchemeSpecificPartRelative() {
		assertEquals("../../path/to/file.html",
				relFixture.getSchemeSpecificPart());
	}

	@Test
	public void testGetFragmentAbsolute() {
		assertEquals("frag", absFixture.getFragment());
	}
	
	@Test
	public void testGetFragmentRelative() {
		assertNull(relFixture.getFragment());
	}

	@Test
	public void testResolveJSONFragmentStringJSON() throws Exception {
		assertEquals("scheme:/domain/dir1/dir2/file.json#/frag/key",
				jsonFixture.resolveJSONFragment("key").toString());
	}
	
	@Test
	public void testResolveJSONFragmentStringRelative() throws Exception {
		assertEquals("../../path/to/file.html#/key",
				relFixture.resolveJSONFragment("key").toString());
	}

	@Test
	public void testResolveJSONFragmentIntJSON() throws Exception {
		assertEquals("scheme:/domain/dir1/dir2/file.json#/frag/2",
				jsonFixture.resolveJSONFragment(2).toString());
	}
	
	@Test
	public void testResolveJSONFragmentIntRelative() throws Exception {
		assertEquals("../../path/to/file.html#/2",
				relFixture.resolveJSONFragment(2).toString());	}

/*	@Test
	public void testToString() {
		fail("Not yet implemented");
	}*/

}
