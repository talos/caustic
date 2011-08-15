package net.microscraper.util;

import static net.microscraper.test.TestUtils.randomInt;
import static net.microscraper.test.TestUtils.randomString;
import static org.junit.Assert.*;

import java.util.Vector;

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
}
