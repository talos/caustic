package net.caustic;

import net.caustic.console.ConsoleOptionsTest;
import net.caustic.console.InputTest;
import net.caustic.database.DatabaseTest;
import net.caustic.database.sql.SQLConnectionTest;
import net.caustic.util.MapUtilsTest;
import net.caustic.util.UUIDFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConsoleOptionsTest.class,
	InputTest.class,
	DatabaseTest.class,
	MapUtilsTest.class,
	UUIDFactoryTest.class,
	SQLConnectionTest.class,
	DatabaseTest.class
})
public class LocalTests {

}
