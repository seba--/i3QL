package saere.database.index.map;

import java.io.File;

import org.junit.Test;

import saere.Term;
import saere.database.DatabaseTest;
import saere.database.Factbase;

public class MapInspectionTest {

	private static final String TEST_FILE = DatabaseTest.DATA_PATH + File.separator + "opal-0.5.0.jar";
	private static final Factbase FACTS = Factbase.getInstance();
	
	@Test
	public void testTrie() {
		FACTS.read(TEST_FILE);
		MapTrieBuilder builder = new MapTrieBuilder();
		MapTrie root = new MapTrie();
		for (Term fact : FACTS.getFacts()) {
			builder.insert(fact, root);
		}
		MapTrieInspector inspector = new MapTrieInspector();
		inspector.inspect(root, builder);
	}
}
