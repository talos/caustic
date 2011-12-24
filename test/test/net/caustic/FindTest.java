package net.caustic;

import static org.junit.Assert.*;

import java.util.Arrays;

import mockit.NonStrict;
import net.caustic.Find;
import net.caustic.Response;
import net.caustic.regexp.JavaUtilRegexpCompiler;
import net.caustic.regexp.RegexpCompiler;
import net.caustic.util.Encoder;
import net.caustic.util.JavaNetEncoder;
import net.caustic.util.StringMap;

import org.junit.Before;
import org.junit.Test;

public class FindTest  {
	private RegexpCompiler compiler;
	@NonStrict StringMap tags;
	
	@Before
	public void setUp() throws Exception {
		compiler = new JavaUtilRegexpCompiler(new JavaNetEncoder(Encoder.UTF_8));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindWithoutSourceThrowsIllegalArgument() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("pattern"), compiler.newTemplate("$0"), 0, -1, false, false, false,
				new String[] {});
		
		find.execute("id", null, tags);
	}
	
	@Test
	public void testMissingVariablesToCompileTemplatePassedUp() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("{{foo}}"),
				compiler.newTemplate("{{bar}} {{baz}}"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "input string", tags);
		String[] missingTags = response.missingTags;

		assertNotNull(missingTags);
		
		assertTrue(Arrays.asList(missingTags).contains("foo"));
		assertTrue(Arrays.asList(missingTags).contains("bar"));
		assertTrue(Arrays.asList(missingTags).contains("baz"));
	}
	
	@Test
	public void testNoMatchesIsFailure() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("humanity"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "late capitalism", tags);
		
		assertNull(response.missingTags);
		assertNotNull(response.failedBecause);
		assertTrue(response.failedBecause.contains("humanity"));
	}

	@Test
	public void testOneMatch() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("needle"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "haystack haystack haystack needle haystack haystack", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(1, response.values.length);
	}
	
	@Test
	public void testManyMatches() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "roses red, violets blue", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(4, response.values.length);
		assertEquals("roses", response.values[0]);
		assertEquals("red", response.values[1]);
		assertEquals("violets", response.values[2]);
		assertEquals("blue", response.values[3]);
	}
	
	@Test
	public void testOneMatchFirst() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 0, 0, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(1, response.values.length);
		assertEquals("the", response.values[0]);
	}
	
	@Test
	public void testOneMatchLast() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -1, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(1, response.values.length);
		assertEquals("only", response.values[0]);
	}
	
	@Test
	public void testOneMatchMiddle() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 1, 1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(1, response.values.length);
		assertEquals("one", response.values[0]);
	}

	@Test
	public void testOneMatchCountBackwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -2, -2, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(1, response.values.length);
		assertEquals("and", response.values[0]);
	}

	@Test
	public void testManyMatchCountForwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 1, 2, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(2, response.values.length);
		assertEquals("one", response.values[0]);
		assertEquals("and", response.values[1]);
	}
	
	@Test
	public void testManyMatchCountBackwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -3, -2, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(2, response.values.length);
		assertEquals("one", response.values[0]);
		assertEquals("and", response.values[1]);
	}
	

	@Test
	public void testManyMatchCountMixed() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -3, 2, false, false, false, new String[] {});
		
		Response response = find.execute("id", "the one and only", tags);
		
		assertNull(response.missingTags);
		assertNull(response.failedBecause);
		
		assertNotNull(response.values);
		assertEquals(2, response.values.length);
		assertEquals("one", response.values[0]);
		assertEquals("and", response.values[1]);
	}
}
