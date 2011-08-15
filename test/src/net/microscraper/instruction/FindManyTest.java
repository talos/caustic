package net.microscraper.instruction;

import static org.junit.Assert.*;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.json.JSONObjectInterface;

import org.junit.Test;

public class FindManyTest {
	@Mocked JSONObjectInterface obj;
	@Tested FindMany findMany;
	
	private static final int NON_DEFAULT = 10;
	
}
