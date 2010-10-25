package saere.database;

import java.io.File;

import org.junit.runner.JUnitCore;

import saere.database.index.IteratorsTest;
import saere.database.index.MatcherTest;
import saere.database.index.TermFlattenerTest;
import saere.database.index.TermStackTest;

/**
 * Starter for JUnit tests concering the {@link saere.database} package.
 * 
 * @author David Sullivan
 * @version 0.103, 10/23/2010
 */
public class DatabaseTest {
	
	public static final String DATA_PATH = "test" + File.separator + "data";
	
	public static void main(String[] args) {
		JUnitCore.runClasses(
			IteratorsTest.class,
			MatcherTest.class,
			TermStackTest.class,
			TermFlattenerTest.class
		);
	}
}
