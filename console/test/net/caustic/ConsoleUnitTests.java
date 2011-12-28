package net.caustic;

import net.caustic.console.ConsoleOptionsTest;
import net.caustic.console.InputTest;
import net.caustic.http.MainClassUnitTest;
import net.caustic.util.MapUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConsoleOptionsTest.class,
	InputTest.class,
	MapUtilsTest.class,
	MainClassUnitTest.class
})
public class ConsoleUnitTests {

}
