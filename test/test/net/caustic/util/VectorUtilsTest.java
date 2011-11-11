package net.caustic.util;

import static net.caustic.util.TestUtils.randomInt;
import static net.caustic.util.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.Vector;

import net.caustic.util.VectorUtils;

import org.junit.Test;

public class VectorUtilsTest {
	private static final int repetitions = 200;
	private static final int strLen = 100;

	@Test
	public void testVectorIntoVector() {
		for(int i = 0 ; i < repetitions ; i ++) {
			int vector1length = randomInt(repetitions);
			int vector2length = randomInt(repetitions);
			
			Vector<String> vector1 = new Vector<String>();
			Vector<String> vector2 = new Vector<String>();
			
			for(int j = 0 ; j < vector1length ; j ++) {
				vector1.add(randomString(strLen));
			}
			for(int j = 0 ; j < vector2length ; j ++) {
				vector2.add(randomString(strLen));
			}
			
			Vector<String> vector2copy = new Vector<String>();
			vector2copy.addAll(vector2);
			
			VectorUtils.vectorIntoVector(vector1, vector2);
			
			assertEquals("Combined vector not same size as components.", vector1length + vector2length, vector2.size());
			assertTrue("Combined vector does not contain all original elements.", vector2.containsAll(vector2copy));
			assertTrue("Combined vector does not contain all added elements.", vector2.containsAll(vector1));
		}
	}

	@Test
	public void testArrayIntoVector() {
		for(int i = 0 ; i < repetitions ; i ++) {
			int vectorLength = randomInt(repetitions);
			int arrayLength = randomInt(repetitions);
			
			Vector<String> vector = new Vector<String>();
			String[] array = new String[arrayLength];
			
			for(int j = 0 ; j < vectorLength ; j ++) {
				vector.add(randomString(strLen));
			}
			for(int j = 0 ; j < arrayLength ; j ++) {
				array[j] = randomString(strLen);
			}
			
			Vector<String> vectorCopy = new Vector<String>();
			vectorCopy.addAll(vector);
			
			VectorUtils.arrayIntoVector(array, vector);
			
			assertEquals("Combined vector not same size as components.", vectorLength + arrayLength, vector.size());
			assertTrue("Combined vector does not contain all original vector elements.", vector.containsAll(vectorCopy));
			for(int j = 0 ; j < arrayLength ; j ++) {
				assertTrue("Combined vector missing array element.", vector.contains(array[j]));
			}
		}
	}
	
	@Test
	public void testHaveSameElementsInDifferentOrder() {
		String alpha = randomString();
		String beta = randomString();
		String gaga = randomString();
		
		Vector<String> vector1 = new Vector<String>();
		vector1.add(alpha);
		vector1.add(beta);
		vector1.add(gaga);

		Vector<String> vector2 = new Vector<String>();
		vector2.add(gaga);
		vector2.add(alpha);
		vector2.add(beta);
		
		assertTrue("Same elements in different order, should return true.",
				VectorUtils.haveSameElements(vector1, vector2));
	}

	@Test
	public void testOneVectorEncompassedByOther() {
		String alpha = randomString();
		String beta = randomString();
		String gaga = randomString();
		
		Vector<String> vector1 = new Vector<String>();
		vector1.add(alpha);
		vector1.add(beta);
		
		Vector<String> vector2 = new Vector<String>();
		vector2.add(alpha);
		vector2.add(gaga);
		
		assertFalse("Share some but not all elements, should return false.",
				VectorUtils.haveSameElements(vector1, vector2));
	}
	
	@Test
	public void testOneVectorExclusiveOfOther() {
		String alpha = randomString();
		String beta = randomString();
		String gaga = randomString();
		
		Vector<String> vector1 = new Vector<String>();
		vector1.add(alpha);
		vector1.add(beta);
		
		Vector<String> vector2 = new Vector<String>();
		vector2.add(gaga);
		
		assertFalse("Share no elements, should return false.",
				VectorUtils.haveSameElements(vector1, vector2));
	}
}
