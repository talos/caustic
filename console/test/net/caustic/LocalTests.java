package net.caustic;

import net.caustic.console.ConsoleOptionsTest;
import net.caustic.console.InputTest;
import net.caustic.database.DatabaseTest;
import net.caustic.database.SQLConnectionTest;
import net.caustic.util.MapUtilsTest;
import net.caustic.util.ScopeFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConsoleOptionsTest.class,
	InputTest.class,
	DatabaseTest.class,
	MapUtilsTest.class,
	ScopeFactoryTest.class,
	SQLConnectionTest.class,
	DatabaseTest.class
})
public class LocalTests {

}
