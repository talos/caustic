package net.microscraper.database;

import static net.microscraper.util.TestUtils.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import net.microscraper.database.csv.CSVConnection;
import net.microscraper.database.sql.JDBCSqliteConnection;
import net.microscraper.uuid.IntUUIDFactory;
import net.microscraper.uuid.JavaUtilUUIDFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class DatabaseViewTest {
	private static final char DELIMITER = ',';
	private final Database db;
	private DatabaseView view;
	
	public DatabaseViewTest(Database db) throws Exception {
		this.db = db;
		db.open();
	}
	
	@Parameters
	public static List<Database[]> implementations() {
		return Arrays.asList(new Database[][] {
				{ new NonPersistedDatabase(CSVConnection.toSystemOut(DELIMITER),
						new IntUUIDFactory() )  },
				{ new SingleTableDatabase(JDBCSqliteConnection.inMemory(Database.SCOPE_COLUMN_NAME),
						new IntUUIDFactory() )  },
				{ new MultiTableDatabase(JDBCSqliteConnection.inMemory(Database.SCOPE_COLUMN_NAME),
						new IntUUIDFactory() )  }
		});
	}
	
	@Before
	public void setUp() throws Exception {
		//db.open();
		view = db.newView();
	}
	
	@After
	public void tearDown() throws Exception {
		//db.close();
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
		
		System.out.println("spawn child:");
		DatabaseView child = view.spawnChild(name, value);
		
		System.out.println("get parent should not have value: ");
		assertNull("Parent should not have value.", view.get(name));
		
		System.out.println("get child should have value");
		String childValue = child.get(name);
		assertEquals("Child does not have correct value", value, childValue);
	}
	
	@Test
	public void testStoreStoresInView() throws Exception {
		String name = randomString();
		String value = randomString();
		
		view.put(name, value);
		assertEquals("View does not have correct value.", value, view.get(name));
	}
	

	@Test
	public void testStoreToParentAccessibleToChildren() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		view.put(name, value);
		assertEquals("Child should have access to parent value.", value, child.get(name));
	}
	

	@Test
	public void testStoreToChildNotAccessibleToParent() throws Exception {
		String name = randomString();
		String value = randomString();
		DatabaseView child = view.spawnChild(randomString());
		
		child.put(name, value);
		assertNull("Parent should not have access to child value.", view.get(name));
	}
	
	@Test
	public void testPutOverwrites() throws Exception {
		
		String name = randomString();
		String value = randomString();
		String value2 = randomString();
		
		view.put(name, value);
		view.put(name, value2);
		
		assertEquals("Overwritten value not present.", value2, view.get(name));
		assertNotSame("Old value still present.", value, view.get(name));
	}
}
