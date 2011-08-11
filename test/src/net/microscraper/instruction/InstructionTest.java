package net.microscraper.instruction;

import static org.junit.Assert.*;

import java.awt.geom.NoninvertibleTransformException;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import net.microscraper.interfaces.json.JSONInterfaceObject;
import net.microscraper.interfaces.uri.URIInterface;

import org.junit.Test;

public class InstructionTest {
	
	@Mocked JSONInterfaceObject obj;
	@Tested Instruction instruction;

	@Test
	public void testGetLocation() throws Exception {
		final String locationString = "http://www.xyz.com/location";
		new NonStrictExpectations() {
			URIInterface location;
			{
				obj.getLocation(); result = location;
				location.toString(); result = locationString;
			}
		};
		instruction = new Instruction(obj);
		assertEquals(locationString, instruction.getLocation().toString());
	}

	@Test
	public void testGetNameDefaultIsNull() throws Exception {
		instruction = new Instruction(obj);
		assertNull(instruction.getName());
	}

	@Test
	public void testGetName() throws Exception {
		final String name = "jon doe";
		new NonStrictExpectations() {{
			obj.getString(Instruction.NAME); result = name;
			obj.has(Instruction.NAME); result = true;
		}};
		instruction = new Instruction(obj);
		assertEquals(name, instruction.getName().toString());
	}
	
	@Test
	public void testHasNoName() throws Exception {
		instruction = new Instruction(obj);
		assertFalse(instruction.hasName());
	}
	
	@Test
	public void testHasName() throws Exception {
		final String name = "jon doe";
		new NonStrictExpectations() {{
			obj.getString(Instruction.NAME); result = name;
			obj.has(Instruction.NAME); result = true;
		}};
		instruction = new Instruction(obj);
		assertTrue(instruction.hasName());
	}
}
