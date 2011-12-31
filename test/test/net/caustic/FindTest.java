package net.caustic;

import static org.junit.Assert.*;

import java.util.Arrays;

import mockit.NonStrict;
import net.caustic.Find;
import net.caustic.Response;
import net.caustic.Response.DoneFind;
import net.caustic.Response.MissingTags;
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
		
		assertEquals(Response.MISSING_TAGS, response.getStatus());
		MissingTags missingTags = (MissingTags) response;
		assertEquals(3, missingTags.getMissingTags().length);
		assertTrue(Arrays.asList("foo", "bar", "baz")
					.containsAll(Arrays.asList(missingTags.getMissingTags())));
	}
	
	@Test
	public void testNoMatchesIsFailure() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("humanity"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		Response response = find.execute("id", "late capitalism", tags);
		
		assertEquals(Response.FAILED, response.getStatus());
		assertTrue(((Response.Failed) response).getReason().contains("humanity"));
	}

	@Test
	public void testOneMatch() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("needle"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "haystack haystack haystack needle haystack haystack", tags);
		
		assertArrayEquals(new String[] { "needle" }, response.getValues());
	}
	
	@Test
	public void testManyMatches() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 0, -1, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "roses red, violets blue", tags);
		assertArrayEquals(new String[] { "roses", "red", "violets", "blue" }, response.getValues());
	}
	
	@Test
	public void testOneMatchFirst() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 0, 0, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "the" }, response.getValues());
	}
	
	@Test
	public void testOneMatchLast() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -1, -1, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "only" }, response.getValues());
	}
	
	@Test
	public void testOneMatchMiddle() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 1, 1, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "one" }, response.getValues());
	}

	@Test
	public void testOneMatchCountBackwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -2, -2, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "and" }, response.getValues());
	}

	@Test
	public void testManyMatchCountForwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), 1, 2, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "one", "and" }, response.getValues());
	}
	
	@Test
	public void testManyMatchCountBackwards() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -3, -2, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "one", "and" }, response.getValues());
	}
	

	@Test
	public void testManyMatchCountMixed() throws Exception {
		Find find = new Find("description", "uri", compiler, compiler.newTemplate("name"),
				compiler.newTemplate("\\w+"),
				compiler.newTemplate("$0"), -3, 2, false, false, false, new String[] {});
		
		DoneFind response = (DoneFind) find.execute("id", "the one and only", tags);
		
		assertArrayEquals(new String[] { "one", "and" }, response.getValues());
	}
}
