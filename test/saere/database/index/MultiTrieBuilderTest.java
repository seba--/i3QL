package saere.database.index;

import static saere.database.DatabaseTermFactory.*;

import org.junit.Test;

import saere.Term;
import saere.database.index.multi.MultiTrieBuilder;

public final class MultiTrieBuilderTest {
	
	@Test
	public void test1() {
		TrieBuilder builder = new MultiTrieBuilder(50);
		Trie multiRoot = Trie.root();
		
		for (Term fact : Facts.ALL) {
			builder.insert(fact, multiRoot);
		}
		
		System.out.println("DONE");
	}
	
	public static class Facts {
		
		/** f(a(d,e),b). */
		public static final Term T0 = ct("f", ct("a", sa("d"), sa("e")), sa("b"));
		
		/** f(a,b). */
		public static final Term T1 = ct("f", sa("a"), sa("b"));
		
		/** f(a(d),b). */
		public static final Term T2 = ct("f", ct("a", sa("d")), sa("b"));
		
		/** f(a(d,g),b). */
		public static final Term T3 = ct("f", ct("a", sa("d"), sa("g")), sa("b"));
		
		/** f(a(d)). */
		public static final Term T4 = ct("f", ct("a", sa("d")));
		
		public static final Term[] ALL = { T0, T1, T2, T3, T4 }; 
	}
}
