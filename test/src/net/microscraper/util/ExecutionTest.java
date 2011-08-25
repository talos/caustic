package net.microscraper.util;

import static net.microscraper.util.TestUtils.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExecutionTest {
	
	@Test
	public void testIsSuccessful() {
		Object obj = new Object();
		Execution execution = Execution.success(obj);
		assertTrue(execution.isSuccessful());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testMissingVariableThrowsIllegalArgOnZeroLengthMissingVariables() {
		Execution.missingVariables(new String[] {});
	}
	
	@Test
	public void testMissingVariablesIsNotSuccessful() {
		Execution execution = Execution.missingVariables(new String[] { randomString() });
		assertFalse(execution.isSuccessful());
	}

	@Test
	public void testGetSubstituted() {
		Object obj = new Object();
		Execution execution = Execution.success(obj);
		assertEquals(obj, execution.getExecuted());
	}

	@Test
	public void testGetMissingVariables() {
		String[] missingVariables = new String[] { randomString(), randomString() };
		Execution execution = Execution.missingVariables(missingVariables);
		assertArrayEquals(missingVariables, execution.getMissingVariables());
	}
	
	@Test
	public void testCombineOneMissingVariablesIsMissingVariables() {
		Execution execution1 = Execution.success(new Object());
		Execution execution2 = Execution.missingVariables(new String[] { randomString() });
		Execution combined = Execution.combine(new Execution[] { execution1, execution2 });
		assertFalse(combined.isSuccessful());
	}
	
	@Test
	public void testCombineAllSuccessfulIsSuccessful() {
		Execution execution1 = Execution.success(new Object());
		Execution execution2 = Execution.success(new Object());
		Execution combined = Execution.combine(new Execution[] { execution1, execution2 });
		assertTrue(combined.isSuccessful());
	}
	
	@Test
	public void testCombineAllSuccessfulCombinesSubstitutedObjects() {
		Object obj1 = new Object();
		Object obj2 = new Object();
		Execution execution1 = Execution.success(obj1);
		Execution execution2 = Execution.success(obj2);
		Execution combined = Execution.combine(new Execution[] { execution1, execution2 });
		
		Object[] combinedSubstituted = (Object[]) combined.getExecuted();
		List<Object> list = Arrays.asList(combinedSubstituted);
		assertTrue(list.contains(obj1));
		assertTrue(list.contains(obj2));
		assertEquals(2, combinedSubstituted.length);
	}

	@Test
	public void testCombineMissingVariablesCombinesMissingVariables() {
		String[] missingVariables1 = new String[] { randomString(3) };
		String[] missingVariables2 = new String[] { randomString(4) };
		Execution execution1 = Execution.missingVariables(missingVariables1);
		Execution execution2 = Execution.missingVariables(missingVariables2);
		Execution combined = Execution.combine(new Execution[] { execution1, execution2 });
		
		String[] combinedMissing = combined.getMissingVariables();
		List<String> list = Arrays.asList(combinedMissing);
		List<String> missingList1 = Arrays.asList(missingVariables1);
		List<String> missingList2 = Arrays.asList(missingVariables2);
		assertTrue(list.containsAll(missingList1));
		assertTrue(list.containsAll(missingList2));
		assertEquals("Should not merge different missing variables.", 2, combinedMissing.length);
	}
	
	@Test
	public void testCombineMissingVariablesCombinesMissingVariablesWithoutDuplicates() {
		String sharedString = randomString();
		String[] missingVariables1 = new String[] { sharedString, randomString(), randomString() };
		String[] missingVariables2 = new String[] { sharedString, randomString(), randomString() };
		Execution execution1 = Execution.success(new Object());
		Execution execution2 = Execution.missingVariables(missingVariables1);
		Execution execution3 = Execution.missingVariables(missingVariables2);
		Execution combined = Execution.combine(new Execution[] { execution1, execution2, execution3 });
		
		String[] combinedMissing = combined.getMissingVariables();
		List<String> list = Arrays.asList(combinedMissing);
		List<String> missingList1 = Arrays.asList(missingVariables1);
		List<String> missingList2 = Arrays.asList(missingVariables2);
		assertTrue(list.containsAll(missingList1));
		assertTrue(list.containsAll(missingList2));
		assertEquals("Should not count sharedString twice.", 5, combinedMissing.length);
	}
}
