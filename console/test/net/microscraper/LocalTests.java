package net.microscraper;

import net.microscraper.console.ConsoleOptionsTest;
import net.microscraper.console.InputTest;
import net.microscraper.database.DatabaseViewTest;
import net.microscraper.database.sql.SQLConnectionTest;
import net.microscraper.util.MapUtilsTest;
import net.microscraper.util.UUIDFactoryTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	ConsoleOptionsTest.class,
	InputTest.class,
	DatabaseViewTest.class,
	MapUtilsTest.class,
	UUIDFactoryTest.class,
	SQLConnectionTest.class
})
public class LocalTests {

}
