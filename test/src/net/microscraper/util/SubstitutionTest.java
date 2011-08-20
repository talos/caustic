package net.microscraper.util;

import static net.microscraper.test.TestUtils.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.junit.Test;

public class SubstitutionTest {
	
	@Test
	public void testIsSuccessful() {
		Object obj = new Object();
		Execution sub = Execution.success(obj);
		assertTrue(sub.isSuccessful());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFailThrowsIllegalArgOnZeroLengthMissingVariables() {
		Execution.fail(new String[] {});
	}
	
	@Test
	public void testIsNotSuccessful() {
		Execution sub = Execution.fail(new String[] { randomString() });
		assertFalse(sub.isSuccessful());
	}

	@Test
	public void testGetSubstituted() {
		Object obj = new Object();
		Execution sub = Execution.success(obj);
		assertEquals(obj, sub.getExecuted());
	}

	@Test
	public void testGetMissingVariables() {
		String[] missingVariables = new String[] { randomString(), randomString() };
		Execution sub = Execution.fail(missingVariables);
		assertArrayEquals(missingVariables, sub.getMissingVariables());
	}
	
	@Test
	public void testCombineOneUnsuccessfulIsUnsuccessful() {
		Execution sub1 = Execution.success(new Object());
		Execution sub2 = Execution.fail(new String[] { randomString() });
		Execution combined = Execution.combine(new Execution[] { sub1, sub2 });
		assertFalse(combined.isSuccessful());
	}
	
	@Test
	public void testCombineAllSuccessfulIsSuccessful() {
		Execution sub1 = Execution.success(new Object());
		Execution sub2 = Execution.success(new Object());
		Execution combined = Execution.combine(new Execution[] { sub1, sub2 });
		assertTrue(combined.isSuccessful());
	}
	
	@Test
	public void testCombineAllSuccessfulCombinesSubstitutedObjects() {
		Object obj1 = new Object();
		Object obj2 = new Object();
		Execution sub1 = Execution.success(obj1);
		Execution sub2 = Execution.success(obj2);
		Execution combined = Execution.combine(new Execution[] { sub1, sub2 });
		
		Object[] combinedSubstituted = (Object[]) combined.getExecuted();
		List<Object> list = Arrays.asList(combinedSubstituted);
		assertTrue(list.contains(obj1));
		assertTrue(list.contains(obj2));
		assertEquals(2, combinedSubstituted.length);
	}
	
	@Test
	public void testCombineUnsuccessfulCombinesMissingVariables() {
		String sharedString = randomString();
		String[] missingVariables1 = new String[] { sharedString, randomString(), randomString() };
		String[] missingVariables2 = new String[] { sharedString, randomString(), randomString() };
		Execution sub1 = Execution.success(new Object());
		Execution sub2 = Execution.fail(missingVariables1);
		Execution sub3 = Execution.fail(missingVariables2);
		Execution combined = Execution.combine(new Execution[] { sub1, sub2, sub3 });
		
		String[] combinedMissing = combined.getMissingVariables();
		List<String> list = Arrays.asList(combinedMissing);
		List<String> missingList1 = Arrays.asList(missingVariables1);
		List<String> missingList2 = Arrays.asList(missingVariables2);
		assertTrue(list.containsAll(missingList1));
		assertTrue(list.containsAll(missingList2));
		assertEquals("Should not count sharedString twice.", 5, combinedMissing.length);
	}
	
	@Test
	public void testArraySubAllContainsSubbedObjects(final @Mocked Substitutable sub1,
			final @Mocked Substitutable sub2,
			final @Mocked Substitutable sub3,
			final @Mocked Variables variables) {
		final Object obj1 = new Object();
		final Object obj2 = new Object();
		final Object obj3 = new Object();
		
		new NonStrictExpectations() {{
			sub1.sub(variables); result = Execution.success(obj1);
			sub2.sub(variables); result = Execution.success(obj2);
			sub3.sub(variables); result = Execution.success(obj3);
		}};
		
		Substitutable[] substitutables =
				new Substitutable[] { sub1, sub2, sub3 };
		Execution combined = Execution.arraySub(substitutables, variables);
		
		Object[] combinedObject = (Object[]) combined.getExecuted();
		assertEquals(3, combinedObject.length);
		List<Object> list = Arrays.asList(combinedObject);
		assertTrue(list.contains(obj1));
		assertTrue(list.contains(obj2));
		assertTrue(list.contains(obj3));
	}

	@Test
	public void testArraySubAllHasAllMissingVariables(final @Mocked Substitutable sub1,
			final @Mocked Substitutable sub2,
			final @Mocked Substitutable sub3,
			final @Mocked Variables variables) {
		String sharedString = randomString();
		final Object obj1 = new Object();
		final String[] missingVariables1 = new String[] { sharedString, randomString(), randomString() };
		final String[] missingVariables2 = new String[] { sharedString, randomString(), randomString() };
		
		new NonStrictExpectations() {{
			sub1.sub(variables); result = Execution.success(obj1);
			sub2.sub(variables); result = Execution.fail(missingVariables1);
			sub3.sub(variables); result = Execution.fail(missingVariables2);
		}};
		
		Substitutable[] substitutables =
				new Substitutable[] { sub1, sub2, sub3 };
		Execution combined = Execution.arraySub(substitutables, variables);
		
		String[] combinedMissing = combined.getMissingVariables();
		assertEquals("Should not double count sharedString", 5, combinedMissing.length);
		
		List<String> list = Arrays.asList(combinedMissing);
		assertTrue(list.containsAll(Arrays.asList(missingVariables1)));
		assertTrue(list.containsAll(Arrays.asList(missingVariables2)));
	}
}
