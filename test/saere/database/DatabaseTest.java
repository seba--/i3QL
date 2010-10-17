package saere.database;

import java.io.File;

import org.junit.runner.JUnitCore;

import saere.database.index.IteratorsTest;
import saere.database.index.LabelTest;
import saere.database.index.MatcherTest;
import saere.database.index.TermFlattenerTest;
import saere.database.index.TermStackTest;
import saere.database.predicate.Instr3Test;

/**
 * Starter for JUnit tests concering the {@link saere.database} package.
 * 
 * @author David Sullivan
 * @version 0.102, 10/14/2010
 */
public class DatabaseTest {
	
	public static final String DATA_PATH = "test" + File.separator + "data";
	
	public static void main(String[] args) {
		JUnitCore.runClasses(
			IteratorsTest.class,
			MatcherTest.class, 
			LabelTest.class,
			TermStackTest.class,
			Instr3Test.class,
			TermFlattenerTest.class
		);
	}
}
