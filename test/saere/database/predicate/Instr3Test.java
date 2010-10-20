package saere.database.predicate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.Database;
import saere.database.DatabaseTermFactory;
import saere.database.DatabaseTest;
import saere.database.Factbase;
import saere.database.ListDatabase;
import saere.database.Stopwatch;
import saere.database.TrieDatabase;
import saere.database.Utils;
import saere.database.index.ComplexTrieBuilder;
import saere.database.index.IteratorsTest;
import saere.database.index.RecursiveTermFlattener;
import saere.database.index.ShallowTermFlattener;
import saere.database.index.SimpleTrieBuilder;
import saere.database.index.TermFlattener;
import saere.database.index.Trie;
import saere.database.index.TrieBuilder;
import saere.database.profiling.KeyWriter;
import saere.meta.GenericCompoundTerm;

@SuppressWarnings("all")
public class Instr3Test {
	
	private static final TermFlattener SHALLOW = new ShallowTermFlattener();
	private static final TermFlattener RECURSIVE = new RecursiveTermFlattener();
	private static final TrieBuilder SIMPLE = new SimpleTrieBuilder();
	private static final TrieBuilder COMPLEX = new ComplexTrieBuilder();
	
	private static final Factbase FACTS = Factbase.getInstance();
	private static final Database LIST_DB = ListDatabase.getInstance();
	private static final Database TRIE_DB = TrieDatabase.getInstance();
	
	private static final Term RETURN = new GenericCompoundTerm(StringAtom.StringAtom("return"), new Term[] { new Variable() }); // return(X)
	private static final Term INVOKE = new GenericCompoundTerm(StringAtom.StringAtom("invoke"), new Term[] { new Variable() }); // invoke(X)
	private static final Term M_1 = StringAtom.StringAtom("m_1");
	private static final Term M_X = StringAtom.StringAtom("m_4339");
	private static final Term M_5 = StringAtom.StringAtom("m_5");
	private static final Term I0 = IntegerAtom.IntegerAtom(0);
	private static final Term I1 = IntegerAtom.IntegerAtom(1);
	private static final Term I7 = IntegerAtom.IntegerAtom(7);
	private static final Term X = new Variable();
	private static final Term Y = new Variable();
	private static final Term Z = new Variable();
	
	private static final Term[][] QUERIES = new Term[][] {
		new Term[] { X, Y, Z},			// instr(X, Y, Z)
		new Term[] { M_X, Y, Z},		// instr(m_?, Y, Z)
		new Term[] { X, I0, Z },		// instr(X, 0, Z)
		new Term[] { X, I0, RETURN },	// instr(X, 0, return) -- getter?
		new Term[] { X, Y, RETURN },	// instr(X, Y, return)
		new Term[] { X, Y, INVOKE }		// instr(X, Y, invoke)
	};
	
	private static final DatabasePredicate INSTR3 = new Instr3();
	
	private static OutputStream keysOut;
	
	/**
	 * The test file for this {@link IteratorsTest}. It must be a JAR, ZIP or CLASS 
	 * file. The larger the file, the longer will the tests of this 
	 * {@link IteratorsTest} take.
	 */
	private static String testFile = DatabaseTest.DATA_PATH + File.separator + "HelloWorld.class";
	
	/**
	 * Sets the {@link #testFile}.
	 * 
	 * @param testFile
	 * @see #testFile
	 */
	public static void testFile(String testFile) {
		Instr3Test.testFile = testFile;
	}
	
	/**
	 * Gets the {@link #testFile}.
	 * 
	 * @return The test file for this instance.
	 * @see #testFile
	 */
	public static String testFile() {
		return testFile;
	}
	
	@BeforeClass
	public static void prepare() {
		Stopwatch sw = new Stopwatch();
		try {
			keysOut = new FileOutputStream("C:/Users/Leaf/Desktop/keys.txt");
			KeyWriter.getInstance().setOutputStream(keysOut);
		} catch (FileNotFoundException e) {
			// ignore
		}
		FACTS.drop();
		FACTS.read(testFile);
		sw.printElapsed("Reading the facts from " + testFile);
	}
	
	@AfterClass
	public static void destroy() {
		if (keysOut != null)
			try {
				keysOut.close();
			} catch (IOException e) {
				// ignore
			}
	}
	
	private void fillListDB() {
		Stopwatch sw = new Stopwatch();
		LIST_DB.drop();
		LIST_DB.fill();
		sw.printElapsed("Filling the list-based database with " + FACTS.getFacts().size() + " facts");
	}
	
	private void fillTrieDB() {
		Stopwatch sw = new Stopwatch();
		TRIE_DB.drop();
		TRIE_DB.fill();
		String flattener = (Trie.getTermFlattener() instanceof ShallowTermFlattener) ? "shallow" : "recursive";
		String inserter = (Trie.getTermInserter() instanceof SimpleTrieBuilder) ? "simple" : "complex";
		sw.printElapsed("Filling the trie-based database (" + flattener + ", " + inserter + ") with " + FACTS.getFacts().size() + " facts");
	}
	
	//@Test
	public void testComplexShallow() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(COMPLEX);
		executeQueries();
		
		System.out.println();
		INSTR3.useLists();
		System.out.print("USING LISTS: ");
		Utils.queryNoPrint(INSTR3, QUERIES[1]);
		
		INSTR3.useTries();
		System.out.print("USING TRIES: ");
		Utils.queryNoPrint(INSTR3, QUERIES[1]);
	}
	
	//@Test
	public void testSimpleShallow() {
		Trie.setTermFlattener(SHALLOW);
		Trie.setTermInserter(SIMPLE);
		executeQueries();
		
		System.out.println();
		INSTR3.useLists();
		System.out.print("USING LISTS: ");
		Utils.queryNoPrint(INSTR3, QUERIES[1]);
		
		INSTR3.useTries();
		System.out.print("USING TRIES: ");
		Utils.queryNoPrint(INSTR3, QUERIES[1]);
	}
	
	@Test
	public void test1() {
		fillTrieDB();
		INSTR3.useTries();
		Utils.query(INSTR3, new Term[] { X, Y, Z });
		
		Term[] query = new Term[] { StringAtom.StringAtom("instr"), X, Y, RETURN };
		Iterator<Term> iter = ((TrieDatabase) TRIE_DB).getRoot().iterator(query);
		int counter = 0;
		Stopwatch sw = new Stopwatch();
		while (iter.hasNext()) {
			iter.next();
			counter++;
		}
		sw.printElapsed("Iteration over " + counter + " facts");
	}
	
	private void executeQueries() {
		fillListDB();
		fillTrieDB();
		
		for (int i = 0; i < 10; i++)
			for (Term[] query : QUERIES) {
				System.out.println();
				INSTR3.useLists();
				System.out.print("USING LISTS: ");
				Utils.queryNoPrint(INSTR3, query);
				
				INSTR3.useTries();
				System.out.print("USING TRIES: ");
				Utils.queryNoPrint(INSTR3, query);
				System.out.println("---");
			}
	}
}
