package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import mockit.Mocked;
import mockit.Tested;
import net.microscraper.console.IntUUIDFactory;
import net.microscraper.console.UUIDFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.sun.javadoc.Type;

@RunWith(Parameterized.class)
public class DatabaseViewTest {
	@Mocked static private WritableConnection writableConnection;
	private final Database db;
	private DatabaseView view;
	
	public DatabaseViewTest(Database db) throws Exception {
		this.db = db;
	}
	
	@Parameters
	public static List<Database[]> implementations() {
		return Arrays.asList(new Database[][] {
				{ new HashtableDatabase() },
				{ new SingleTableDatabase(new HashtableDatabase(), writableConnection, new IntUUIDFactory())  },
				{ new MultiTableDatabase(new HashtableDatabase(), ioConnection ) }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		db.open();
		view = db.newView();
	}

	@Test
	public void testSpawnChildWithNameOnlyStoresNothing() throws Exception {
		String name = randomString();
		DatabaseView child = view.spawnChild(name);

		assertNull(view.get(name));
		assertNull(child.get(name));
	}

	@Test
	public void testSpawnChildWithNameAndValueStoresOnlyInChild() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(name, value);
		
		assertNull(view.get(name));
		assertEquals(value, child.get(name));
	}
	
	@Test
	public void testStoreStoresInView() throws Exception {
		String name = randomString();
		String value = randomString();
		
		view.put(name, value);
		assertEquals(value, view.get(name));
	}
	

	@Test
	public void testStoreToParentAccessibleToChildren() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		view.put(name, value);
		assertEquals(value, child.get(name));
	}
	

	@Test
	public void testStoreToChildNotAccessibleToParent() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		child.put(name, value);
		assertNull(value, view.get(name));
	}
	
	@Test
	public void testPutOverwrites() throws Exception {
		
		String name = randomString();
		String value = randomString();
		String value2 = randomString();
		
		view.put(name, value);
		view.put(name, value2);
		
		assertEquals(value2, view.get(name));
		assertNotSame(value, view.get(name));
	}
}
