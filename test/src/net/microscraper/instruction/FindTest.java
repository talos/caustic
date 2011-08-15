package net.microscraper.instruction;

import static net.microscraper.instruction.Find.*;
import static net.microscraper.test.TestUtils.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import net.microscraper.client.Browser;
import net.microscraper.database.Database;
import net.microscraper.json.JSONObjectInterface;
import net.microscraper.regexp.Pattern;
import net.microscraper.regexp.RegexpCompiler;
import net.microscraper.util.Variables;

import org.junit.Before;
import org.junit.Test;

public class FindTest {	
	@Mocked JSONObjectInterface obj;
	@Mocked Database database;
	@Mocked Browser browser;
	@Mocked RegexpCompiler compiler;
	@Mocked Variables variables;
	@Mocked Result source;
		
	@Before
	public void setup() throws Exception {
		/*new NonStrictExpectations() {{
			onInstance(obj).getString(URL); result = url;
		}};*/
	}
	
	@Test(expected=DeserializationException.class)
	public void testIsNotAPage() throws Exception {
		new Page(obj);
	}
	
	@Test(expected=DeserializationException.class)
	public void testNeedsAPatternToDeserialize() throws Exception {
		new Find(obj);
	}
	
	@Test
	public void testNeedsOnlyAPatternToDeserialize() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Regexp.PATTERN); result = randomString();
		}};
		new Find(obj);
	}
	
	@Test(expected=DeserializationException.class)
	public void testDefiningMaxAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Regexp.PATTERN); result = randomString();
			obj.has(MAX_MATCH); result = true;
			obj.has(MATCH); result = true;
		}};
		new Find(obj);
	}

	@Test(expected=DeserializationException.class)
	public void testDefiningMinAndSingleMatchPreventsDeserialization() throws Exception {
		new NonStrictExpectations() {{
			obj.getString(Regexp.PATTERN); result = randomString();
			obj.has(MIN_MATCH); result = true;
			obj.has(MATCH); result = true;
		}};
		new Find(obj);
	}
	
	@Test
	public void testDefaultsToFirstMatch(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		final String patternString = randomString();
		
		new NonStrictExpectations() {{
				source.getValue(); result = stringSource;
				obj.getString(Regexp.PATTERN); result = patternString;
				compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		new Find(obj).execute(compiler, browser, variables, source, database);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, FIRST_MATCH, FIRST_MATCH);
		}};
	}

	@Test(expected = DeserializationException.class)
	public void testWontDeserializeImpossiblePositiveMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = randomInt() + 1;
		final int max = randomInt(min);
		new NonStrictExpectations() {{
			obj.has(MIN_MATCH); result = true;
			obj.has(MAX_MATCH); result = true;
			obj.getInt(MIN_MATCH); result = min;
			obj.getInt(MAX_MATCH); result = max;
		}};
		
		new Find(obj);
	}
	

	@Test(expected = DeserializationException.class)
	public void testWontDeserializeImpossibleNegativeMatchRange(@Mocked final Pattern pattern) throws Exception {
		final int min = 0 - (randomInt() + 1);
		final int max = 0 - (randomInt(0 - min));
		new NonStrictExpectations() {{
			obj.has(MIN_MATCH); result = true;
			obj.has(MAX_MATCH); result = true;
			obj.getInt(MIN_MATCH); result = min;
			obj.getInt(MAX_MATCH); result = max;
		}};
		
		new Find(obj);
	}
	
	@Test
	public void testMatchAllDefaultsToFullRange(@Mocked final Pattern pattern) throws Exception {
		final String stringSource = randomString();
		final String patternString = randomString();
		
		new NonStrictExpectations() {{
				source.getValue(); result = stringSource;
				obj.getString(Regexp.PATTERN); result = patternString;
				obj.has(MATCH); result = true;
				obj.getString(MATCH); result = MATCH_ALL_VALUE;
				compiler.compile(patternString, anyBoolean, anyBoolean, anyBoolean); result = pattern;
		}};
		
		new Find(obj).execute(compiler, browser, variables, source, database);
		
		new Verifications() {{
			pattern.match(stringSource, anyString, FIRST_MATCH, LAST_MATCH);
		}};
	}
	
	@Test(expected = DeserializationException.class)
	public void testMatchAndMinWontDeserialize(@Mocked final Pattern pattern) throws Exception {
		new NonStrictExpectations() {{
				obj.has(MATCH); result = true;
				obj.has(MIN_MATCH); result = true;
		}};
		new Find(obj);
	}
	
	@Test(expected = DeserializationException.class)
	public void testMatchAndMaxWontDeserialize(@Mocked final Pattern pattern) throws Exception {
		new NonStrictExpectations() {{
				obj.has(MATCH); result = true;
				obj.has(MAX_MATCH); result = true;
		}};
		new Find(obj);
	}
}
